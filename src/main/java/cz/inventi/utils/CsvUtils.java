package cz.inventi.utils;

import cz.inventi.model.Field;
import cz.inventi.model.CsvDefinition;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.io.FilenameUtils;
import org.supercsv.io.CsvListWriter;
import org.supercsv.io.ICsvListWriter;
import org.supercsv.prefs.CsvPreference;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

@Log4j2
public class CsvUtils {

  public static void createCsvFile(CsvDefinition metadata, String target)
          throws IOException {
    Path targetPath = Paths.get(target, FilenameUtils.getName(metadata.getFileName()));

    char encapsulationChar = metadata.getTextEncapsulator().charAt(0);
    char delimiterChar = metadata.getColumnDelimiter().charAt(0);
    String recordDelimiter = metadata.getRecordDelimiter();

    // TODO DISCUSS why we need here a different writer than in writeCsvRow?
    try (ICsvListWriter listWriter = new CsvListWriter(
            new FileWriter(targetPath.toString(), StandardCharsets.UTF_8),
            new CsvPreference.Builder(encapsulationChar, delimiterChar, recordDelimiter).build()
    )) {
      String[] header = metadata.getFields().stream().map(Field::getName).toArray(String[]::new);
      listWriter.writeHeader(header);
    }
    log.trace("CSV file {} was created (now only with header)", target);
  }

  /**
   * Writes row as rowValues separated by columnDelimiter to CSV file defined by filename.
   *
   * @param filename filename of file which will be updated
   * @param rowValues values of new row to be written
   * @param columnDelimiter delimiter between cell values in row
   * @throws IOException when some I/O problem occurred
   */
  public static void writeCsvRow(String filename, List<String> rowValues, String columnDelimiter) throws IOException {
    if (rowValues.isEmpty()) {
      log.trace("Empty rowValues are ignored. Use list of empty strings to write empty row");
      return;
    }

    try (BufferedWriter writer = new BufferedWriter(new FileWriter(filename, StandardCharsets.UTF_8, true))) {
      writer.write(String.join(columnDelimiter, rowValues));
      writer.newLine();
    }
  }
}
