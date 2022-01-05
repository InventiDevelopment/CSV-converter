package cz.inventi.jsontocsvconverter.model;

import lombok.AllArgsConstructor;
import lombok.Value;

import java.util.List;
import java.util.function.BiFunction;

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
  BiFunction<Field, String, List<String>> customMapper;

  public CsvField(String name, String jsonPath) {
    this(name, jsonPath, false, null);
  }

  public CsvField(String name, String jsonPath, boolean required) {
    this(name, jsonPath, required, null);
  }
}
