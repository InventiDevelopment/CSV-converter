package cz.inventi.model;

import lombok.AllArgsConstructor;
import lombok.Value;

/**
 * Implementation of {@link Field} for CSV.
 * Represents one column definition of CSV.
 */
@Value
@AllArgsConstructor
public class CsvField implements Field {
  /**
   * Name of related column in output CSV
   */
  String name;
  String jsonPath;
  boolean required;
}
