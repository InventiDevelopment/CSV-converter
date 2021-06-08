import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertIterableEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import cz.inventi.CsvConvertor;
import cz.inventi.model.CsvField;
import cz.inventi.model.DefaultTableData;
import cz.inventi.model.Field;
import cz.inventi.model.TableData;

public class CsvConverterTest {

  private static final String TEST_OUTPUT_FOLDER = "output";
  private CsvConvertor csvConvertor = new CsvConvertor();
  private static final Logger log = Logger.getLogger(CsvConverterTest.class);

  @BeforeEach
  void setUp() throws IOException {
    FileUtils.deleteDirectory(new File(TEST_OUTPUT_FOLDER));
  }

  @Test
  void convertJsonFile1Test() throws IOException {
    List<Field> fields = List.of(
        new CsvField("NAME", "name", false),
        new CsvField("VERSION", "version", false),
        new CsvField("DATE", "date", false),
        new CsvField("OPTION", "options.name", false),
        new CsvField("TENANT ID", "tenants[*].id", false),
        new CsvField("TENANT NAME", "tenants[*].name", false),
        new CsvField("ORGANIZATION ID", "tenants[*].organizations[*].id", false),
        new CsvField("ORGANIZATION NAME", "tenants[*].organizations[*].name", false),
        new CsvField("ORGANIZATION CREATED", "tenants[*].organizations[*].created", false),
        new CsvField("POS ID", "tenants[*].organizations[*].pos[*].id", false),
        new CsvField("POS TITLE", "tenants[*].organizations[*].pos[*].title", false)
    );
    TableData csvDefinition = new DefaultTableData("Test Convert", "test1-convert.csv", fields);

    List<List<String>> exportData = convertJsonToCsv("test1.json", csvDefinition);
    assertNotNull(exportData);
    assertEquals(5, exportData.size());
    assertTrue(exportData.stream().allMatch(row -> row.size() == csvDefinition.getFields().size()));
    assertEquals(exportData.get(0), csvDefinition.getFields().stream().map(Field::getName).collect(
        Collectors.toList()));

    assertIterableEquals(exportData.get(1), List.of("Field name","1.0.0","2021-07-05","Option #1 name","42e2190f-7fb5-4b19-97e5-8f6c90276167","First test tenant","53d3def7-d402-4d04-845e-b9da7c532dbd","First organization name","2021-06-07T12:25:12", "", ""));
    assertIterableEquals(exportData.get(2), List.of("Field name","1.0.0","2021-07-05","Option #1 name","42e2190f-7fb5-4b19-97e5-8f6c90276167","First test tenant","a1535974-5946-4d07-80da-1a55925bf912","Second organization name","2021-04-01T20:35:12","",""));
    assertIterableEquals(exportData.get(3), List.of("Field name","1.0.0","2021-07-05","Option #1 name","42e2190f-7fb5-4b19-97e5-8f6c90276167","First test tenant","69c5abb9-7a10-43ab-bee9-e5ac3d0f745b","Third organization name","2020-01-24T10:25:12","0d93fe40-bb90-4f17-b83f-78437754da89","POS title #1"));
    assertIterableEquals(exportData.get(4), List.of("Field name","1.0.0","2021-07-05","Option #1 name","42e2190f-7fb5-4b19-97e5-8f6c90276167","First test tenant","69c5abb9-7a10-43ab-bee9-e5ac3d0f745b","Third organization name","2020-01-24T10:25:12","56ae7217-2b4f-45c9-9d28-5ae9b9413338","POS title #2"));
  }

  private List<List<String>> convertJsonToCsv(String source, TableData csvDefinition) throws IOException {
    csvConvertor.convert("src/test/resources/" + source, TEST_OUTPUT_FOLDER, csvDefinition);
    return readGeneratedFile(csvDefinition);
  }

  private List<List<String>> readGeneratedFile(TableData csvFile) {
    List<String> files;
    try {
      files = FileUtils
          .readLines(Paths.get(TEST_OUTPUT_FOLDER, csvFile.getFileName()).toFile(), csvFile.getEncoding());
    } catch (IOException e) {
      log.error(String.format("File '%s' doesn't exist.", Paths.get(TEST_OUTPUT_FOLDER, csvFile.getFileName()).toString()));
      return null;
    }

    return files.stream().map(file -> {
      if (String.valueOf(file.charAt(file.length() - 1)).equals(csvFile.getColumnDelimiter())) {
        file += " ";
      }
      return Arrays.stream(file.split(csvFile.getColumnDelimiter())).map(f -> !f.equals(" ") ? f : "")
          .collect(Collectors.toList());
    }).collect(Collectors.toList());
  }
}
