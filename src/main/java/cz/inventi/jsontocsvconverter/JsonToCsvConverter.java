package cz.inventi.jsontocsvconverter;

import com.jayway.jsonpath.DocumentContext;
import com.jayway.jsonpath.PathNotFoundException;
import cz.inventi.jsontocsvconverter.model.*;
import cz.inventi.jsontocsvconverter.model.csvdefinitions.FileCsvDefinition;
import cz.inventi.jsontocsvconverter.utils.CsvUtils;
import cz.inventi.jsontocsvconverter.utils.FileUtils;
import cz.inventi.jsontocsvconverter.utils.JsonUtils;
import cz.inventi.jsontocsvconverter.utils.ListUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Converts JSON file to CSV based on provided {@link CsvDefinition}.
 */
@RequiredArgsConstructor
@Log4j2
public class JsonToCsvConverter {

  /**
   * Convert source JSON file to new created CSV target file.
   *
   * @param source        source JSON filename
   * @param csvDefinition definition of target CSV format
   * @throws IOException when some I/O problem occurred
   */
  public void convert(String source, CsvDefinition csvDefinition) throws IOException {
    DocumentContext jsonContext = JsonUtils.parseJsonFile(source);
    convert(jsonContext, csvDefinition);
  }

  /**
   * Convert source JSON file to new created CSV target file.
   *
   * @param source        source JSON file
   * @param csvDefinition definition of target CSV format
   * @throws IOException when some I/O problem occurred
   */
  public void convert(File source, CsvDefinition csvDefinition) throws IOException {
    DocumentContext jsonContext = JsonUtils.parseJsonFile(source);
    convert(jsonContext, csvDefinition);
  }

  /**
   * Convert source JSON file to new created CSV target file.
   *
   * @param source        source JSON file
   * @param csvDefinition definition of target CSV format
   * @throws IOException when some I/O problem occurred
   */
  public void convert(InputStream source, CsvDefinition csvDefinition) throws IOException {
    DocumentContext jsonContext = JsonUtils.parseJsonFile(source);
    convert(jsonContext, csvDefinition);
  }

  private void convert(DocumentContext jsonContext, CsvDefinition csvDefinition) throws IOException {
    if (csvDefinition instanceof FileCsvDefinition) {
      String targetDirectory = Paths.get(((FileCsvDefinition) csvDefinition).getFileName()).getParent().toString();
      FileUtils.ensureTargetDirectoryExists(targetDirectory);
    }
    convertJsonToCsv(jsonContext, csvDefinition);
  }

  /**
   * Converts part of JSON to specified CSV file.
   *
   * @param jsonContext   source JSON context
   * @param csvDefinition definition of target CSV format
   * @throws IOException when some I/O problem occurred
   */
  private void convertJsonToCsv(DocumentContext jsonContext, CsvDefinition csvDefinition)
          throws IOException {
    log.debug("Converting JSON file to CSV file {}.", csvDefinition.getName());
    JsonPath root = getJsonPathsTree(jsonContext, csvDefinition);

    log.debug("Generating CSV file {}.", csvDefinition.getName());
    CsvUtils.createCsvFile(csvDefinition);

    log.trace("Start generating CSV file {} rows.", csvDefinition.getName());
    List<CsvCell> row = new ArrayList<>();
    for (JsonPath path : root.getChildren()) {
      generateRow(csvDefinition, path, row, new ArrayList<>(), jsonContext);
    }
    log.info("CSV file {} was successfully created.", csvDefinition.getName());
  }

  /**
   * Constructs tree of JSON paths, that should be converted according to TableData.
   *
   * @param jsonContext   loaded JSON context
   * @param csvDefinition definition of target CSV format
   * @return root of JSON paths tree
   */
  public JsonPath getJsonPathsTree(DocumentContext jsonContext, CsvDefinition csvDefinition) {
    List<JsonPath> jsonPaths = new ArrayList<>();

    JsonPath root = new JsonPath(JsonPathType.ROOT, jsonPaths);

    for (Field field : csvDefinition.getFields()) {
      // Note that more than one json paths can be added during one cycle
      parseJsonPathField(field.getJsonPath(), field.getJsonPath(), root, jsonContext);
    }

    return root;
  }

  /**
   * Parses JSON path.
   *
   * <ol>
   *   <li>
   *    If modifiedJsonPath doesn't contain {@link JsonPath#ARRAY_IDENTIFIER_WITH_BRACKETS},
   *    then adds new PROPERTY json path to the tree.
   *   </li>
   *   <li>
   *     If modifiedJsonPath contains more than one {@link JsonPath#ARRAY_IDENTIFIER_WITH_BRACKETS},
   *     then checks the size of the least nested array and calls the method recursively with
   *     filled index to modifiedJsonPath
   *     (array index instead of '*', e.g. organizations[*].users[*].id -> organizations[0].users[*].id)
   *   </li>
   *   <li>
   *     If modifiedJsonPath contains exactly one {@link JsonPath#ARRAY_IDENTIFIER_WITH_BRACKETS},
   *     then adds new ARRAY json path to the tree (if not yer exists there) with array indexes for its parent path
   *     (e.g. for organizations[0].users[1].emails[*].address saves organizations[0].users[1].emails
   *     with indexes [0, 1] -> size of 'emails' array)
   *   </li>
   * </ol>
   *
   * @param originalJsonPath original json path (e.g. organizations[*].users[*].id)
   * @param modifiedJsonPath originalJsonPath with some filled array indexes
   *                         (e.g. organizations[0].users[*].id or organizations[0].users[0].id)
   * @param root             root of json paths tree (tree contains all parsed json paths)
   * @param context          JSON context
   */
  private void parseJsonPathField(final String originalJsonPath, String modifiedJsonPath, JsonPath root,
                                  DocumentContext context) {
    int nestedArraysNumber = JsonPath.countNestedArrays(modifiedJsonPath);

    if (nestedArraysNumber == 0) {
      root.addNewDescendantJsonPath(originalJsonPath, JsonPathType.PROPERTY);
    } else {
      log.trace("jsonPath {} contains array, check how many items the array contains", modifiedJsonPath);
      String modifiedArrayPathString = StringUtils.substringBefore(modifiedJsonPath, JsonPath.ARRAY_IDENTIFIER_WITH_BRACKETS);
      int arraySize = JsonUtils.getArraySize(modifiedArrayPathString, context);

      if (nestedArraysNumber == 1) {
        log.trace("JSON path {} contains exactly one array -> add array indexes for the JSON path.",
                modifiedJsonPath);

        // if parent path already exists in tree, then returns existing
        JsonPath parentPath = root.addNewDescendantJsonPath(JsonPath.getParentJsonPathString(originalJsonPath), JsonPathType.ARRAY);
        parentPath.getArrayIndexes().put(JsonPath.findJsonPathIndexes(modifiedJsonPath), arraySize);
      }

      // Iterate array
      if (arraySize == 0) {
        log.trace("Array {} is empty, set one value to iterate each subarray-items at least 1x.",
                modifiedArrayPathString);
        arraySize = 1;
      }
      for (int i = 0; i < arraySize; i++) {
        log.trace("Call recursive method for each items from the array to get value with replaced array index.");
        String iterateJsonPath = modifiedJsonPath.replaceFirst(JsonPath.ESCAPED_ARRAY_IDENTIFIER, i + "");
        parseJsonPathField(originalJsonPath, iterateJsonPath, root, context);
      }
    }
  }

  /**
   * Generate CSV file rows
   * <ol>
   *   <li>
   *     If JSON path contains children, get number of array indexes and create one row for each object in array
   *   </li>
   *   <li>
   *     If JSON path doesn't contain children, save JSON path with defined array indexes (or without indexes if JSON path doesn't contain array)
   *     as CSV cell to current CSV row
   *   </li>
   *   <li>
   *     When current CSV row is complete -> write row with real values.
   *   </li>
   * </ol>
   * <p>
   * For leaf (PROPERTY type) json paths, that don't have any ARRAY parents, CSV cells will be permanently added to current row.
   * Other cells will be added to different row objects created during recursion.
   *
   * @param csvDefinition definition of target CSV format
   * @param path          processed json path
   * @param row           CSV row
   * @param indexes       current indexes of json path
   * @param jsonContext   source JSON context
   * @throws IOException when some I/O problem occurred
   */
  private void generateRow(CsvDefinition csvDefinition, JsonPath path, List<CsvCell> row,
                           List<Integer> indexes, final DocumentContext jsonContext) throws IOException {
    if (path.getChildren().isEmpty()) {
      log.trace("Create cell with JSON path {} and array indexes {}.", path.getPath(), indexes);

      Field currentField = csvDefinition.getFieldByJsonPath(path.getPath());
      row.add(new CsvCell(path.getPath(), indexes, currentField));

      if (row.size() == csvDefinition.getFields().size()) {
        writeFoundRow(row, csvDefinition, jsonContext);
      }
    } else {
      Integer arraySize = path.getArrayIndexes().get(indexes);

      if (arraySize == 0) {
        log.trace("Array for {} is empty, set one value to iterate the items at least 1x.", path.getPath());
        arraySize = 1;
      }

      for (int i = 0; i < arraySize; i++) {
        log.trace("Duplicating list the original collection is not modified and left intact.");

        List<Integer> newIndexes = ListUtils.extendedList(indexes, i);
        List<CsvCell> newRow = new ArrayList<>(row);

        for (JsonPath childPath : path.getChildren()) {
          generateRow(csvDefinition, childPath, newRow, newIndexes, jsonContext);
        }
      }
    }
  }


  /**
   * Writes CSV row with real values from JSON.
   *
   * @param row list of cells, defining where value should be found in JSON
   * @param csvDefinition definition of target CSV format
   * @param jsonContext source JSON context
   * @throws IOException when some problem during writing to CSV file occurred
   */
  private void writeFoundRow(List<CsvCell> row, CsvDefinition csvDefinition, DocumentContext jsonContext) throws IOException {
    log.trace("CSV row ({}) is complete -> generate CSV row with real values from JSON file.",
            csvDefinition.getName());
    List<String> values = obtainRowValuesFromJson(row, jsonContext);
    CsvUtils.writeCsvRow(csvDefinition, values);
  }

  /**
   * Obtains values for concrete CSV row from JSON.
   *
   * @param row     defines where the values for each cell should be searched in JSON
   * @param context source JSON context
   * @return values for concrete CSV row from JSON. Returns empty list, when row should be ignored
   * (if some required cell value doesn't exist)
   */
  private List<String> obtainRowValuesFromJson(List<CsvCell> row, final DocumentContext context) {
    List<String> values = new ArrayList<>();

    for (CsvCell cell : row) {
      if (cell.jsonPathIsEmpty()) {
        continue;
      }
      String jsonPathWithFilledIndexes = cell.getJsonPathWithFilledIndexes();

      String propertyValue = getPropertyValue(jsonPathWithFilledIndexes, context);
      List<String> convertedValues = null;
      if (cell.getCurrentField().getCustomMapper() != null) {
        convertedValues = cell.getCurrentField().getCustomMapper().apply(cell.getCurrentField(), propertyValue);
      }
      if (propertyValue == null && (convertedValues == null || convertedValues.size() == 0)) {
        if (cell.getCurrentField().isRequired()) {
          log.debug("For the property '{}' doesn't exist any value, skip the whole line.", jsonPathWithFilledIndexes);
          return Collections.emptyList();
        }
        propertyValue = StringUtils.EMPTY;
      }
      if (convertedValues != null && convertedValues.size() > 0) {
        values.addAll(convertedValues);
      } else {
        values.add(propertyValue);
      }
    }

    return values;
  }

  /**
   * Gets value of property defined by path from JSON context.
   *
   * @param jsonPath path of property, which value will be returned
   * @param context  JSON context
   * @return value of property defined by path from JSON context. Returns null if path doesn't exist
   */
  private String getPropertyValue(String jsonPath, DocumentContext context) {
    try {
      return context.read(jsonPath).toString();
    } catch (PathNotFoundException | NullPointerException e) {
      log.trace(String.format("There is no value for JSON path %s.", jsonPath));
      return null;
    }
  }
}
