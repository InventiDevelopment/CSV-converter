package cz.inventi.jsontocsvconverter.model.csvdefinitions;

import java.io.OutputStream;
import java.util.Collection;

import cz.inventi.jsontocsvconverter.model.CsvDefinition;
import cz.inventi.jsontocsvconverter.model.Field;
import lombok.EqualsAndHashCode;
import lombok.Value;

/**
 * Default implementation of {@link CsvDefinition}.
 */
@Value
@EqualsAndHashCode(callSuper = true)
public class OutputStreamCsvDefinition extends DefaultCsvDefinition {
  OutputStream outputStream;

  public OutputStreamCsvDefinition(String name, OutputStream outputStream, Collection<Field> fields) {
    super(name, fields);
    this.outputStream = outputStream;
  }

  public OutputStreamCsvDefinition(String name, OutputStream outputStream, Collection<Field> fields,
      String columnDelimiter) {
    super(name, fields, columnDelimiter);
    this.outputStream = outputStream;
  }

}
