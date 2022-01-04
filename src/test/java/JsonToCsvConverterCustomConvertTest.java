import cz.inventi.jsontocsvconverter.JsonToCsvConverter;
import cz.inventi.jsontocsvconverter.model.CsvField;
import cz.inventi.jsontocsvconverter.model.Field;
import cz.inventi.jsontocsvconverter.model.csvdefinitions.OutputStreamCsvDefinition;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

@TestInstance(TestInstance.Lifecycle.PER_CLASS) // needed for using @AfterAll
@Log4j2
public class JsonToCsvConverterCustomConvertTest {

    private static final String TEST_RESOURCES_INPUT_FOLDER = "src/test/resources/input";

    private static final String TEST_OUTPUT_FOLDER = "output";
    private final JsonToCsvConverter jsonToCsvConverter = new JsonToCsvConverter();

    @BeforeEach
    @AfterAll
    void clean() throws IOException {
        FileUtils.deleteDirectory(new File(TEST_OUTPUT_FOLDER));
    }

    @Test
    void convertJsonFile__doubleNameField() throws IOException {
        var inputJsonFilename = "test14-input.json";
        List<Field> fields =  List.of(
                new CsvField("NAME", "name", false, (field, value) -> Arrays.asList(value, value))
        );
        OutputStream testOutputStream = new ByteArrayOutputStream();

        OutputStreamCsvDefinition csvDefinition = new OutputStreamCsvDefinition("Test Convert " + inputJsonFilename, testOutputStream, fields);

        convertJsonToCsv(inputJsonFilename, csvDefinition);

        List<List<String>> output = ConverterTestUtil.outputStreamToListOfLists(csvDefinition.getOutputStream(), csvDefinition.getColumnDelimiter());
        assertEquals(1, output.get(0).size()); // header
        assertEquals(2, output.get(1).size());
    }

    private void convertJsonToCsv(String source, OutputStreamCsvDefinition csvDefinition) throws IOException {
        jsonToCsvConverter.convert(TEST_RESOURCES_INPUT_FOLDER + "/" + source, csvDefinition);
    }
}
