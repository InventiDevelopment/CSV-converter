package cz.inventi.model;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class CsvField implements Field {

  private final String name;
  private String jsonPath;
  private boolean required;

}
