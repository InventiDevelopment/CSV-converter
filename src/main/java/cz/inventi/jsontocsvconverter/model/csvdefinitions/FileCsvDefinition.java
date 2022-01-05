package cz.inventi.jsontocsvconverter.model.csvdefinitions;

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
public class FileCsvDefinition extends DefaultCsvDefinition {
  String fileName;

  public FileCsvDefinition(String name, String fileName, Collection<Field> fields) {
    super(name, fields);
    this.fileName = fileName;
  }

  public FileCsvDefinition(String name, String fileName, Collection<Field> fields, String columnDelimiter) {
    super(name, fields, columnDelimiter);
    this.fileName = fileName;
  }

}
