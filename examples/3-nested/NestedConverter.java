import cz.inventi.JsonToCsvConverter;
import cz.inventi.model.CsvDefinition;
import cz.inventi.model.CsvField;
import cz.inventi.model.DefaultCsvDefinition;
import cz.inventi.model.Field;

import java.io.IOException;
import java.util.List;

public class NestedConverter {

    private static final String INPUT_JSON = "./input.json";
    private static final String OUTPUT_CSV = "./output.csv";
    private static final List<Field> FIELDS = List.of(
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

    private final JsonToCsvConverter jsonToCsvConverter = new JsonToCsvConverter();

    public void runConversion(String inputFile, String outputFile, List<Field> fields) throws IOException {
        CsvDefinition csvDefinition = new DefaultCsvDefinition("Nested Example", outputFile, fields);
        jsonToCsvConverter.convert(inputFile, csvDefinition);
    }

    public static void main(String[] args) throws IOException {
        new NestedConverter().runConversion(INPUT_JSON, OUTPUT_CSV, FIELDS);
    }
}