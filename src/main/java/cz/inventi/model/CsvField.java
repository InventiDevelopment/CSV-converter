package cz.inventi.model;

import lombok.AllArgsConstructor;
import lombok.Value;

/**
 * Implementation of {@link Field} for CSV.
 */
@Value
@AllArgsConstructor
public class CsvField implements Field {
  /**
   * Used as CSV column name
   */
  String name;
  String jsonPath;
  boolean required;
}
