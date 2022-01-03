package cz.inventi.jsontocsvconverter.model.csvdefinitions;

import java.util.Collection;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;

import cz.inventi.jsontocsvconverter.model.CsvDefinition;
import cz.inventi.jsontocsvconverter.model.Field;
import lombok.Data;

/**
 * Default implementation of {@link CsvDefinition}.
 */
@Data
public class DefaultCsvDefinition implements CsvDefinition {
  public static final String EMPTY_JSON_PATH = "EMPTY_JSON_PATH";

  private String name;
  private Collection<Field> fields;
  private Map<String, Field> fieldsByJsonPath;
  private String columnDelimiter;

  public DefaultCsvDefinition(String name, Collection<Field> fields) {
    this(name, fields, ";");
  }

  public DefaultCsvDefinition(String name, Collection<Field> fields, String columnDelimiter) {
    this.name = name;
    this.fields = fields;
    this.fieldsByJsonPath = parseFieldsByJsonPath(fields);
    this.columnDelimiter = columnDelimiter;
  }

  @Override
  public Field getFieldByJsonPath(String jsonPath) {
    return fieldsByJsonPath.get(jsonPath);
  }

  @Override
  public String getColumnDelimiter() {
    return columnDelimiter;
  }

  /**
   * Creates map (jsonPath -> Field) from collection of Fields.
   *
   * @param fields original fields collection
   * @return map (jsonPath -> Field) from collection of Fields.
   */
  private Map<String, Field> parseFieldsByJsonPath(Collection<Field> fields) {
    return fields
               .stream()
               .filter(this::fieldNotEmpty)
               .collect(Collectors.toMap(Field::getJsonPath, Function.identity(), (f1, f2) -> f1));
  }

  /**
   * @param field field
   * @return true if json path is null, empty string or special constant, otherwise false
   */
  private boolean fieldNotEmpty(Field field) {
    String jsonPath = field.getJsonPath();
    return !(StringUtils.isEmpty(jsonPath) || jsonPath.equalsIgnoreCase(EMPTY_JSON_PATH));
  }
}
