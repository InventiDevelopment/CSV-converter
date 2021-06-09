package cz.inventi.model;

import lombok.AllArgsConstructor;
import lombok.Data;

// Why is not immutable? Why we need setJsonPath() in Field interface
@Data
@AllArgsConstructor
public class CsvField implements Field {

  private final String name;
  private String jsonPath;
  private boolean required;

}
