package cz.inventi.utils;

import cz.inventi.model.Field;
import cz.inventi.model.CsvDefinition;
import lombok.extern.log4j.Log4j2;
import org.supercsv.io.CsvListWriter;
import org.supercsv.io.ICsvListWriter;
import org.supercsv.prefs.CsvPreference;

import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.List;

/**
 * Contains utils methods for work with CSV files.
 */
@Log4j2
public class CsvUtils {

  /**
   * Creates CSV file based on csvDefinition and writes header.
   *
   * @param csvDefinition definition of target CSV format
   * @throws IOException when some problem during writing to CSV occurred
   */
  public static void createCsvFile(CsvDefinition csvDefinition) throws IOException {
    try (ICsvListWriter writer = getWriter(csvDefinition)) {
      String[] header = csvDefinition.getFields().stream().map(Field::getName).toArray(String[]::new);
      writer.writeHeader(header);
    }
    log.trace("CSV file {} was created (now only with header)", csvDefinition.getFileName());
  }

  /**
   * Writes row as rowValues separated by columnDelimiter to CSV file defined by filename.
   *
   * @param rowValues values of new row to be written
   * @throws IOException when some I/O problem occurred
   */
  public static void writeCsvRow(CsvDefinition csvDefinition, List<String> rowValues) throws IOException {
    if (rowValues.isEmpty()) {
      log.trace("Empty rowValues are ignored. Use list of empty strings to write empty row");
      return;
    }

    try (ICsvListWriter writer = getWriter(csvDefinition)) {
      writer.write(rowValues);
    }
  }

  /**
   * @param csvDefinition definition of target CSV format
   * @return CSV writer created based on csvDefinition
   * @throws IOException when some problem during writing to CSV occurred
   */
  public static ICsvListWriter getWriter(CsvDefinition csvDefinition) throws IOException {
    char encapsulationChar = csvDefinition.getTextEncapsulator().charAt(0);
    char delimiterChar = csvDefinition.getColumnDelimiter().charAt(0);
    String recordDelimiter = csvDefinition.getRecordDelimiter();

    return new CsvListWriter(
            new FileWriter(csvDefinition.getFileName(), Charset.forName(csvDefinition.getEncoding()), true),
            new CsvPreference.Builder(encapsulationChar, delimiterChar, recordDelimiter).build()
    );
  }
}
