package cz.inventi.jsontocsvconverter.model;

import java.util.Collection;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;

import lombok.Value;

/**
 * Default implementation of {@link CsvDefinition}.
 */
@Value
public class DefaultCsvDefinition implements CsvDefinition {
  public static final String EMPTY_JSON_PATH = "EMPTY_JSON_PATH";

  String name;
  String fileName;
  Collection<Field> fields;
  Map<String, Field> fieldsByJsonPath;

  public DefaultCsvDefinition(String name, String fileName, Collection<Field> fields) {
    this.name = name;
    this.fileName = fileName;
    this.fields = fields;
    this.fieldsByJsonPath = parseFieldsByJsonPath(fields);
  }

  @Override
  public Field getFieldByJsonPath(String jsonPath) {
    return fieldsByJsonPath.get(jsonPath);
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
