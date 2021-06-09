package cz.inventi;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.supercsv.io.CsvListWriter;
import org.supercsv.io.ICsvListWriter;
import org.supercsv.prefs.CsvPreference;

import com.jayway.jsonpath.Configuration;
import com.jayway.jsonpath.DocumentContext;
import com.jayway.jsonpath.PathNotFoundException;

import cz.inventi.model.CsvField;
import cz.inventi.model.CsvRow;
import cz.inventi.model.DefaultTableData;
import cz.inventi.model.Field;
import cz.inventi.model.JsonPath;
import cz.inventi.model.TableData;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@SuppressWarnings({ "PMD_PATH_TRAVERSAL_OUT" })
public class CsvConvertor {

  public static final String ARRAY_IDENTIFIER = "*";
  public static final String ARRAY_IDENTIFIER_WITH_BRACKETS = "[" + ARRAY_IDENTIFIER + "]";
  private static final String NUMBER_ARRAY_IDENTIFIER_WITH_BRACKETS_REGEX = "\\[(\\d+)]";
  private static final String ARRAY_IDENTIFIER_WITH_BRACKETS_REGEX = "\\[\\" + ARRAY_IDENTIFIER + "]";

  private static final Logger log = LogManager.getLogger(CsvConvertor.class);

  /**
   * Find all JSON path with array indexes for DSFinV-K file in param
   */
  public List<JsonPath> getAllJsonPath(TableData csvFile, DocumentContext context) {
    List<JsonPath> jsonPaths = new ArrayList<>();

    for (Field field : csvFile.getFields()) {
      parseJsonPathField(field.getJsonPath(), field.getJsonPath(), jsonPaths, context);
    }

    return jsonPaths;
  }

  public void convert(String source, String target, TableData csvDefinition) throws IOException {
    DocumentContext jsonContext = parseJsonFile(source);
    ensureTargetExists(target);
    convertTableMetaData(csvDefinition, jsonContext, target);
  }

  /**
   * Convert part of DFKA json to specified DSFinV-K CSV file
   */
  private void convertTableMetaData(TableData tableMetaData, DocumentContext jsonContext, String target)
      throws IOException {
    log.debug(String
        .format("Converting JSON file to CSV file %s (%s).", tableMetaData.getName(), tableMetaData.getFileName()));
    List<JsonPath> rowPaths = getAllJsonPath(tableMetaData, jsonContext);
    JsonPath head = new JsonPath().toBuilder().children(rowPaths).build();

    log.debug(String.format("Generating CSV file %s (%s).", tableMetaData.getName(), tableMetaData.getFileName()));
    generateFile(tableMetaData, head, jsonContext, target);
    log.info(String
        .format("CSV file %s (%s) was successfully created.", tableMetaData.getName(), tableMetaData.getFileName()));
  }

  /**
   * Parse JSON path.
   * If JSON path contains array, get count of array's items and call the method recursive with modified JSON path (array index instead of '*')
   * If JSON path doesn't contain array, save JSON path with array indexes
   */
  private void parseJsonPathField(final String originalJsonPath, String modifiedJsonPath, List<JsonPath> jsonPaths,
      DocumentContext context) {
    String arrayPathId = StringUtils.substringBeforeLast(originalJsonPath, ARRAY_IDENTIFIER_WITH_BRACKETS);
    JsonPath actualArrayPath = findJsonPathByArray(jsonPaths, arrayPathId);

    log.trace(String.format("Check if the jsonPath %s contains array with '*'", modifiedJsonPath));
    int count = StringUtils.countMatches(modifiedJsonPath, ARRAY_IDENTIFIER_WITH_BRACKETS);

    if (count > 0) {
      log.trace(String.format("jsonPath %s contains array, check how many items the array contains", modifiedJsonPath));
      String jsonPathWithoutArray = StringUtils.substringBefore(modifiedJsonPath, ARRAY_IDENTIFIER_WITH_BRACKETS);
      int arraySize;

      try {
        arraySize = context.read(String.format("%s.length()", jsonPathWithoutArray));
      } catch (PathNotFoundException e) {
        log.trace(String.format("No data was found for JSON path %s.", jsonPathWithoutArray));
        arraySize = 0;
      }

      if (actualArrayPath == null) {
        actualArrayPath = new JsonPath().toBuilder().arrayPath(arrayPathId).build();

        log.trace("Check if JSON path already exists in parsed list.");
        JsonPath parentPath = findJsonPathByArray(
            jsonPaths,
            StringUtils.substringBeforeLast(arrayPathId, ARRAY_IDENTIFIER_WITH_BRACKETS)
        );

        if (parentPath != null) {
          parentPath.getChildren().add(actualArrayPath);
        } else {
          jsonPaths.add(actualArrayPath);
        }
      }

      if (count == 1) {
        log.trace(String.format("JSON path %s contains exactly one array -> add array indexes for the JSON path.",
            modifiedJsonPath));
        List<Integer> arrayIndexes = findJsonPathIndexes(modifiedJsonPath);
        actualArrayPath.getArrayIndexes().put(arrayIndexes, arraySize);
      }

      if (arraySize == 0) {
        log.trace(String.format("Array %s is empty, set one value to iterate each subarray-items at least 1x.",
            jsonPathWithoutArray));
        arraySize = 1;
      }

      for (int i = 0; i < arraySize; i++) {
        log.trace("Call recursive method for each items from the array to get value with replaced array index.");
        String iterateJsonPath = modifiedJsonPath.replaceFirst("\\" + ARRAY_IDENTIFIER, i + "");
        parseJsonPathField(originalJsonPath, iterateJsonPath, jsonPaths, context);
      }
    } else if (findJsonPathByOriginalPath(jsonPaths, originalJsonPath) == null) {
      log.debug(String.format("JSON path %s doesn't exist in parsed list, add it.", originalJsonPath));
      JsonPath newPath = new JsonPath().toBuilder().jsonPath(originalJsonPath).build();

      if (actualArrayPath != null) {
        actualArrayPath.getChildren().add(newPath);
      } else if (originalJsonPath.equals(DefaultTableData.EMPTY_JSON_PATH)) {
        jsonPaths.add(0, newPath);
      } else {
        jsonPaths.add(newPath);
      }
    }
  }

  /**
   * Generate CSV file.
   * 1) Create empty CSV file with DSFinV-K file header.
   * 2) Generate all rows.
   */
  public void generateFile(TableData metadata, JsonPath jsonPath, final DocumentContext context, String target)
      throws IOException {
    Path targetPath = Paths.get(target, FilenameUtils.getName(metadata.getFileName()));

    char encapsulation = metadata.getTextEncapsulator().charAt(0);
    char delimiterChar = metadata.getColumnDelimiter().charAt(0);
    String recordDelimiter = metadata.getRecordDelimiter();

    try (ICsvListWriter listWriter = new CsvListWriter(
        new FileWriter(targetPath.toString(), StandardCharsets.UTF_8),
        new CsvPreference.Builder(encapsulation, delimiterChar, recordDelimiter).build()
    )) {
      String[] header = metadata.getFields().stream().map(Field::getName).toArray(String[]::new);
      listWriter.writeHeader(header);
    }

    List<CsvRow> rows = new ArrayList<>();
    log.trace(String.format("Start generating CSV file %s (%s) rows.", metadata.getName(), metadata.getFileName()));
    for (JsonPath path : jsonPath.getChildren()) {
      generateRow(metadata, path, rows, new ArrayList<>(), -1, context, target);
    }
  }

  /**
   * Generate CSV file rows (DFKA -> DSFinV-K)
   * 1) Check if JSON path contains children, get number of array indexes and create one row for each object in array
   * 2) If JSON path doesn't contain children, save JSON path with defined array indexes (or without indexes if JSON path isn't array)
   * 3) When CSV file row is complete -> generate row with real DFKA values.
   */
  private void generateRow(TableData tableMetaData, JsonPath path, List<CsvRow> rows,
      List<Integer> indexes, final int actualIndex, final DocumentContext jsonContext, String target)
      throws IOException {
    if (path.getJsonPath() != null) {
      CsvRow row = new CsvRow(path.getJsonPath(), new ArrayList<>(indexes));

      if (actualIndex != -1) {
        row.getIndexes().add(actualIndex);
      }

      log.trace(
          String.format("Create row with JSON path %s and array indexes {}.", path.getJsonPath(), row.getIndexes()));
      rows.add(row);
    } else if (!path.getChildren().isEmpty()) {
      List<Integer> newIndexes = new ArrayList<>(indexes);

      if (findNumberOfItemsByRegex(path.getArrayPath(), ARRAY_IDENTIFIER_WITH_BRACKETS_REGEX) > 0) {
        newIndexes.add(actualIndex);
      }

      Integer countArrayObjects = path.getArrayIndexes().get(newIndexes);

      if (countArrayObjects == 0) {
        log.trace(String
            .format("Array for %s is empty, set one value to iterate the items at least 1x.", path.getJsonPath()));
        countArrayObjects = 1;
      }

      for (int y = 0; y < countArrayObjects; y++) {
        log.trace("Duplicating list the original collection is not modified and left intact.");

        List<CsvRow> newRows = new ArrayList<>(rows);
        for (JsonPath childPath : path.getChildren()) {
          generateRow(tableMetaData, childPath, newRows, newIndexes, y, jsonContext, target);
        }
      }
    }

    if (rows.size() == tableMetaData.getFields().size()) {
      log.trace(String.format("CSV row (%s) is complete -> generate CSV row with real values from JSON file.",
          tableMetaData.getName()));
      writeCsvRow(tableMetaData, rows, jsonContext, target);
    }
  }

  /**
   * Write one CSV row into CSV file.
   */
  private void writeCsvRow(TableData table, List<CsvRow> rows,
      final DocumentContext context, String target) throws IOException {
    String path = Paths.get(target, table.getFileName()).toString();
    List<String> currentLineValues = new ArrayList<>();

    for (CsvRow row : rows) {
      if (row.getJsonPath().equals(DefaultTableData.EMPTY_JSON_PATH)) {
        continue;
      }
      String originalJsonPath = row.getJsonPath();

      if (!row.getIndexes().isEmpty()) {
        CsvRow finalRow = row;
        finalRow.getIndexes().forEach(
            index -> finalRow.setJsonPath(finalRow.getJsonPath().replaceFirst("\\" + ARRAY_IDENTIFIER, index + ""))
        );
      }
      boolean isPropertyValueExist = writePropertyValue(currentLineValues, table, row, originalJsonPath, context);
      if (!isPropertyValueExist) {
        log.debug(
            String.format("For the property '%s' doesn't exist any value, skip the whole line.", row.getJsonPath()));
        return;
      }
    }
    try (BufferedWriter writer = new BufferedWriter(new FileWriter(path, StandardCharsets.UTF_8, true))) {
      for (int i = 0; i < currentLineValues.size(); i++) {
        writer.write(currentLineValues.get(i));
        if (i < currentLineValues.size() - 1) {
          writer.write(table.getColumnDelimiter());
        }
      }
      writer.newLine();
    }
  }

  /**
   * Get value from JSON context and put it into line collection.
   * If the property is somehow special (based on taxonomy version), the value does not need to be put into collection
   *
   * @return If the property value exists or is not required -> true. Otherwise the property value is required and doesn't exist -> false.
   */
  private boolean writePropertyValue(List<String> currentLineValues, TableData table,
      CsvRow row, String originalJsonPath, DocumentContext context) {
    String propertyValue = null;

    try {
      propertyValue = context.read(row.getJsonPath()).toString();
    } catch (PathNotFoundException | NullPointerException e) {
      log.trace(String.format("There is no value for JSON path %s.", row.getJsonPath()));
    }
    if (propertyValue == null) {
      Field currentField = table.getFieldByJsonPath(originalJsonPath);
      if (currentField != null && currentField.isRequired()) {
        return false;
      }
      currentLineValues.add(StringUtils.EMPTY);
      return true;
    }

    currentLineValues.add(propertyValue);
    return true;
  }

  /**
   * Find existing JSON path in collection by Array JSON path (e.g. 'cash_point_closing.transactions').
   */
  private JsonPath findJsonPathByArray(List<JsonPath> jsonPaths, String jsonPath) {
    for (JsonPath path : jsonPaths) {
      if (path.getArrayPath() != null && path.getArrayPath().equals(jsonPath)) {
        return path;
      }

      JsonPath tmp = findJsonPathByArray(path.getChildren(), jsonPath);

      if (tmp != null) {
        return tmp;
      }
    }

    return null;
  }

  /**
   * Find existing JSON path in collection by original JSON path (e.g. 'cash_point_closing.transactions[*].head.id').
   */
  private JsonPath findJsonPathByOriginalPath(List<JsonPath> jsonPaths, String jsonPath) {
    for (JsonPath path : jsonPaths) {
      if (path.getJsonPath() != null && path.getJsonPath().equals(jsonPath)) {
        return path;
      }

      JsonPath tmp = findJsonPathByOriginalPath(path.getChildren(), jsonPath);

      if (tmp != null) {
        return tmp;
      }
    }

    return null;
  }

  /**
   * Parse JSON file for searching by JSON path
   */
  public DocumentContext parseJsonFile(String filename) throws IOException {
    return com.jayway.jsonpath.JsonPath.using(Configuration.defaultConfiguration()).parse(readFile(filename));
  }

  /**
   * Find all array indexes from JSON path.
   * e.g. JSON path = cash.registers[1].purchasers[3].values[4] => result is [1, 3]
   */
  private List<Integer> findJsonPathIndexes(String jsonPath) {
    List<String> valuesString = findAllValuesByRegex(jsonPath, NUMBER_ARRAY_IDENTIFIER_WITH_BRACKETS_REGEX);
    return valuesString.stream().map(Integer::parseInt).collect(Collectors.toList());
  }

  /**
   * Get values from string by regex
   */
  private List<String> findAllValuesByRegex(String text, String regex) {
    List<String> matches = new ArrayList<>();
    Matcher m = Pattern.compile("(?=(" + regex + "))").matcher(text);

    while (m.find()) {
      matches.add(m.group(2));
    }

    return matches;
  }

  /**
   * Get number of items from string by regex
   */
  private int findNumberOfItemsByRegex(String text, String regex) {
    Matcher m = Pattern.compile("(?=(" + regex + "))").matcher(text);
    return (int) m.results().count();
  }

  private String readFile(String path) throws IOException {
    return FileUtils.readFileToString(new File(path), StandardCharsets.UTF_8);
  }

  public void ensureTargetExists(String target) throws IOException {
    File directory = new File(target);

    if (!directory.exists() && !directory.mkdirs()) {
      throw new IOException("The target directory doesn't exist and couldn't be created.");
    }
  }

}
