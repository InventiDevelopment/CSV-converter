import cz.inventi.jsontocsvconverter.model.CsvField;
import cz.inventi.jsontocsvconverter.model.Field;
import org.apache.commons.io.FileUtils;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class ConverterTestUtil {

    public static List<List<String>> outputStreamToListOfLists(OutputStream outputStream, String columnDelimiter) {
        return outputStream.toString().lines().map(file -> {
            if (String.valueOf(file.charAt(file.length() - 1)).equals(columnDelimiter)) {
                file += " ";
            }
            return Arrays.stream(file.split(columnDelimiter)).map(f -> !f.equals(" ") ? f : "")
                    .collect(Collectors.toList());
        }).collect(Collectors.toList());
    }

    public static List<List<String>> linesFromFileToListOfLists(List<String> lines, String columnDelimiter) {
        return lines.stream().map(file -> {
            if (String.valueOf(file.charAt(file.length() - 1)).equals(columnDelimiter)) {
                file += " ";
            }
            return Arrays.stream(file.split(columnDelimiter)).map(f -> !f.equals(" ") ? f : "")
                    .collect(Collectors.toList());
        }).collect(Collectors.toList());
    }

    public static List<Field> getComplexStructureTestFields() {
        return List.of(
                new CsvField("NAME", "name", false, null),
                new CsvField("VERSION", "version", false, null),
                new CsvField("DATE", "date", false, null),
                new CsvField("OPTION", "options.name", false, null),
                new CsvField("TENANT ID", "tenants[*].id", false, null),
                new CsvField("TENANT NAME", "tenants[*].name", false, null),
                new CsvField("ORGANIZATION ID", "tenants[*].organizations[*].id", false, null),
                new CsvField("ORGANIZATION NAME", "tenants[*].organizations[*].name", false, null),
                new CsvField("ORGANIZATION CREATED", "tenants[*].organizations[*].created", false, null),
                new CsvField("POS ID", "tenants[*].organizations[*].pos[*].id", false, null),
                new CsvField("POS TITLE", "tenants[*].organizations[*].pos[*].title", false, null)
        );
    }

    public static InputStream inputStreamFromFile(String source) throws IOException {
        return new ByteArrayInputStream(FileUtils.readFileToByteArray(new File(source)));
    }
}
