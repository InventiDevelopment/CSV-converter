package cz.inventi.model;

import java.util.Collection;

/**
 * Definition of target CSV file
 */
public interface CsvDefinition {
  /**
   * @return Name of this definition
   */
  String getName();

  /**
   * @return target CSV filename
   */
  String getFileName();

  /**
   * @return CSV file encoding
   */
  default String getEncoding() {
    return "UTF8";
  }

  /**
   * @return CSV file column delimiter
   */
  default String getColumnDelimiter() {
    return ";";
  }

  /**
   * @return CSV file text encapsulator
   */
  default String getTextEncapsulator() {
    return "\"";
  }

  /**
   * @return CSV file reecord delimiter
   */
  default String getRecordDelimiter() {
    // "&#13;&#10;"
    return "\r\n";
  }

  /**
   * @return collection of Fields defining what properties from source JSON should be converted to target CSV
   */
  Collection<Field> getFields();

  /**
   * @param jsonPath json path
   * @return field with matched json path
   */
  Field getFieldByJsonPath(String jsonPath);

}
