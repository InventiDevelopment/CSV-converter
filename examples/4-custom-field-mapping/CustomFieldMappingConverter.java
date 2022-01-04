import cz.inventi.JsonToCsvConverter;
import cz.inventi.model.CsvDefinition;
import cz.inventi.model.CsvField;
import cz.inventi.model.DefaultCsvDefinition;
import cz.inventi.model.Field;

import java.io.IOException;
import java.util.List;

public class CustomFieldMappingConverter {

    private static final String INPUT_JSON = "./input.json";
    private static final String OUTPUT_CSV = "./output.csv";
    private static final List<Field> FIELDS = List.of(
            new CsvField("NAME", "name", false, (field, value) -> Arrays.asList(value, value))
    );

    private final JsonToCsvConverter jsonToCsvConverter = new JsonToCsvConverter();

    public void runConversion(String inputFile, String outputFile, List<Field> fields) throws IOException {
        CsvDefinition csvDefinition = new DefaultCsvDefinition("Custom Mapping Example", outputFile, fields);
        jsonToCsvConverter.convert(inputFile, csvDefinition);
    }

    public static void main(String[] args) throws IOException {
        new NestedConverter().runConversion(INPUT_JSON, OUTPUT_CSV, FIELDS);
    }
}
