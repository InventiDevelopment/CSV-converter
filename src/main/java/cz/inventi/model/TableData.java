package cz.inventi.model;

import java.util.Collection;

public interface TableData {

  String getName();

  String getFileName();

  default String getColumnDelimiter() {
    return ";";
  }

  default String getTextEncapsulator() {
    return "\"";
  }

  default String getRecordDelimiter() {
    // "&#13;&#10;"
    return "\r\n";
  }

  Collection<Field> getFields();

  Field getFieldByJsonPath(String jsonPath);

}
