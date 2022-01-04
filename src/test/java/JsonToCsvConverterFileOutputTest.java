import cz.inventi.jsontocsvconverter.JsonToCsvConverter;
import cz.inventi.jsontocsvconverter.model.CsvDefinition;
import cz.inventi.jsontocsvconverter.model.CsvField;
import cz.inventi.jsontocsvconverter.model.Field;
import cz.inventi.jsontocsvconverter.model.csvdefinitions.FileCsvDefinition;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.*;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;


@TestInstance(TestInstance.Lifecycle.PER_CLASS) // needed for using @AfterAll
@Log4j2
public class JsonToCsvConverterFileOutputTest {

  private static final String TEST_RESOURCES_INPUT_FOLDER = "src/test/resources/input";
  private static final String TEST_RESOURCES_OUTPUT_FOLDER = "src/test/resources/output";

  private static final String TEST_OUTPUT_FOLDER = "output";
  private final JsonToCsvConverter jsonToCsvConverter = new JsonToCsvConverter();

  @BeforeEach
  @AfterAll
  void clean() throws IOException {
    FileUtils.deleteDirectory(new File(TEST_OUTPUT_FOLDER));
  }

  @Test
  void convertJsonFile__emptyObject__allFieldsAreOptional() throws IOException {
    runTest("test1-input.json", "test1.1-output.csv", 1,
            List.of(
                    new CsvField("NAME", "name", false),
                    new CsvField("VERSION", "version", false),
                    new CsvField("DATE", "date", false)
            )
    );
  }

  @Test
  void convertJsonFile__emptyObject__someFieldsAreRequired() throws IOException {
    runTest("test1-input.json", "test1.2-output.csv", 0,
            List.of(
                    new CsvField("NAME", "name", true),
                    new CsvField("VERSION", "version", false),
                    new CsvField("DATE", "date", false)
            )
    );
  }

  @Test
  void convertJsonFile__plainObject__allFieldsAreOptional() throws IOException {
    runTest("test2-input.json", "test2-output.csv", 1,
            List.of(
                    new CsvField("NAME", "name", false),
                    new CsvField("VERSION", "version", false),
                    new CsvField("DATE", "date", false)
            )
    );
  }

  @Test
  void convertJsonFile__withNestedObject__allFieldsAreOptional() throws IOException {
    runTest("test3-input.json", "test3-output.csv", 1,
            List.of(
                    new CsvField("NAME", "name", false),
                    new CsvField("VERSION", "version", false),
                    new CsvField("DATE", "date", false),
                    new CsvField("OPTION", "options.advanced", false)
            )
    );
  }

  @Test
  void convertJsonFile__withArrayOfStrings__allFieldsAreOptional() throws IOException {
    runTest("test4-input.json", "test4-output.csv", 3,
            List.of(
                    new CsvField("NAME", "name", false),
                    new CsvField("VERSION", "version", false),
                    new CsvField("DATE", "date", false),
                    new CsvField("EMAIL", "emails[*]", false)
            )
    );
  }


  @Test
  void convertJsonFile__withArrayOfObjects__allFieldsAreOptional() throws IOException {
    runTest("test5-input.json", "test5.1-output.csv", 3,
            List.of(
                    new CsvField("NAME", "name", false),
                    new CsvField("VERSION", "version", false),
                    new CsvField("DATE", "date", false),
                    new CsvField("EMAIL ADDRESS", "emails[*].address", false),
                    new CsvField("EMAIL ACTIVE", "emails[*].active", false)
            )
    );
  }

  @Test
  void convertJsonFile__withArrayOfObjects__someFieldsAreRequired() throws IOException {
    runTest("test5-input.json", "test5.2-output.csv", 2,
            List.of(
                    new CsvField("NAME", "name", false),
                    new CsvField("VERSION", "version", false),
                    new CsvField("DATE", "date", false),
                    new CsvField("EMAIL ADDRESS", "emails[*].address", false),
                    new CsvField("EMAIL ACTIVE", "emails[*].active", true)
            )
    );
  }

  @Test
  void convertJsonFile__withNestedArrayOfObjects__allFieldsAreOptional() throws IOException {
    runTest("test6-input.json", "test6-output.csv", 3,
            List.of(
                    new CsvField("NAME", "name", false),
                    new CsvField("VERSION", "version", false),
                    new CsvField("DATE", "date", false),
                    new CsvField("EMAIL ADDRESS", "contacts.emails[*].address", false),
                    new CsvField("EMAIL ACTIVE", "contacts.emails[*].active", false)
            )
    );
  }

  @Test
  @Disabled // TODO this case will be covered later
  void convertJsonFile__withTwoArraysOnTheSameLevel__allFieldsAreOptional() throws IOException {
    // JSON contains objects array + strings array
    runTest("test7-input.json", "test7-output.csv", 1,
            List.of(
                    new CsvField("NAME", "name", false),
                    new CsvField("VERSION", "version", false),
                    new CsvField("DATE", "date", false),
                    new CsvField("EMAIL ADDRESS", "contacts.emails[*].address", false),
                    new CsvField("EMAIL ACTIVE", "contacts.emails[*].active", false),
                    new CsvField("PHONE", "contacts.phones[*]", false)
            )
    );
  }

  @Test
  @Disabled // TODO this case will be covered later
  void convertJsonFile__withTwoArraysOnTheSameLevelForMultipleLevels() {
    //Test with more arrays on the same level - for multiple levels:
    //array: [
    //{nestedOne: [{nestedOne: [], nestedTwo:[]}], nestedTwo:[{nestedOne: [], nestedTwo:[]}]}
    //]
  }

  @Test
  void convertJsonFile__withArrayOfObjectsWithNestedArrays__allFieldsAreOptional() throws IOException {
    runTest("test8-input.json", "test8-output.csv", 6,
            List.of(
                    new CsvField("NAME", "name", false),
                    new CsvField("VERSION", "version", false),
                    new CsvField("DATE", "date", false),
                    new CsvField("ORG ID", "organizations[*].id", false),
                    new CsvField("ORG NAME", "organizations[*].name", false),
                    new CsvField("USER ID", "organizations[*].users[*].id", false),
                    new CsvField("USER NAME", "organizations[*].users[*].name", false)
            )
    );
  }

  @Test
  void convertJsonFile__withMoreNestedArrays__allFieldsAreOptional() throws IOException {
    runTest("test9-input.json", "test9-output.csv", 11,
            List.of(
                    new CsvField("NAME", "name", false),
                    new CsvField("VERSION", "version", false),
                    new CsvField("DATE", "date", false),
                    new CsvField("ORG ID", "organizations[*].id", false),
                    new CsvField("ORG NAME", "organizations[*].name", false),
                    new CsvField("USER ID", "organizations[*].users[*].id", false),
                    new CsvField("USER NAME", "organizations[*].users[*].name", false),
                    new CsvField("GROUP NAME", "organizations[*].users[*].groups[*].name", false),
                    new CsvField("PERMISSION NAME", "organizations[*].users[*].groups[*].permissions[*]", false)
            )
    );
  }

  @Test
  void convertJsonFile__complexStructure1__allFieldsAreOptional() throws IOException {
    runTest("test10-input.json", "test10-output.csv", 4, ConverterTestUtil.getComplexStructureTestFields());
  }

  @Test
  void convertJsonFile__complexStructure2__allFieldsAreOptional() throws IOException {
    runTest("test11-input.json", "test11-output.csv", 24, ConverterTestUtil.getComplexStructureTestFields());
  }

  @Test
  void convertJsonFile__complexStructure3__allFieldsAreOptional() throws IOException {
    runTest("test12-input.json", "test12-output.csv", 9, ConverterTestUtil.getComplexStructureTestFields());
  }

  @Test
  void convertJsonFile__complexStructure4__allFieldsAreOptional() throws IOException {
    runTest("test13-input.json", "test13-output.csv", 10, ConverterTestUtil.getComplexStructureTestFields());
  }

  private void runTest(String inputJsonFilename, String expectedCsvOutputFilename, int expectedNumberOfRowsExcludingHeader, List<Field> fields) throws IOException {
    String actualOutputFilename = Paths.get(TEST_OUTPUT_FOLDER, inputJsonFilename.replace("-input.json", "-convert.csv")).toString();

    FileCsvDefinition csvDefinition = new FileCsvDefinition("Test Convert " + inputJsonFilename, actualOutputFilename, fields);

    List<List<String>> actualCsvOutput = convertJsonToCsv(inputJsonFilename, csvDefinition);

    checkOutputFile(actualCsvOutput, expectedNumberOfRowsExcludingHeader, TEST_RESOURCES_OUTPUT_FOLDER + "/" + expectedCsvOutputFilename, csvDefinition);
  }

  private void checkOutputFile(List<List<String>> actualCsvOutput, int expectedNumberOfRowsExcludingHeader, String expectedCsvOutputFilename, CsvDefinition csvDefinition) {
    assertNotNull(actualCsvOutput);
    assertEquals(expectedNumberOfRowsExcludingHeader + 1, actualCsvOutput.size());
    assertTrue(actualCsvOutput.stream().allMatch(row -> row.size() == csvDefinition.getFields().size()));
    // check head
    assertEquals(actualCsvOutput.get(0), csvDefinition.getFields().stream().map(Field::getName).collect(
            Collectors.toList()));

    List<List<String>> expectedCsvOutput = readCsvFile(expectedCsvOutputFilename, csvDefinition.getEncoding(), csvDefinition.getColumnDelimiter());

    assertIterableEquals(expectedCsvOutput, actualCsvOutput);
  }

  private List<List<String>> convertJsonToCsv(String source, FileCsvDefinition csvDefinition) throws IOException {
    jsonToCsvConverter.convert(TEST_RESOURCES_INPUT_FOLDER + "/" + source, csvDefinition);
    return readGeneratedFile(csvDefinition);
  }

  private List<List<String>> readGeneratedFile(FileCsvDefinition csvFile) {
    return readCsvFile(csvFile.getFileName(), csvFile.getEncoding(), csvFile.getColumnDelimiter());
  }

  private List<List<String>> readCsvFile(String filename, String encoding, String columnDelimiter) {
    // TODO here can be used ICsvListReader from supercsv library
    List<String> lines;
    try {
      lines = FileUtils.readLines(new File(filename), encoding);
    } catch (IOException e) {
      log.error("File '{}' doesn't exist.", filename);
      return null;
    }

    return ConverterTestUtil.linesFromFileToListOfLists(lines, columnDelimiter);
  }
}
