package cz.inventi.model;

import java.util.Collection;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;

import lombok.Value;

@Value
public class DefaultTableData implements TableData {

  public static final String EMPTY_JSON_PATH = "EMPTY_JSON_PATH";

  private String name;
  private String fileName;
  private Collection<Field> fields;
  private Map<String, Field> fieldsByJsonPath;

  public DefaultTableData(String name, String fileName, Collection<Field> fields) {
    this.name = name;
    this.fileName = fileName;
    this.fields = fields;
    this.fieldsByJsonPath = parseFieldsByJsonPath(fields);
  }

  @Override
  public Field getFieldByJsonPath(String jsonPath) {
    return fieldsByJsonPath.get(jsonPath);
  }

  private Map<String, Field> parseFieldsByJsonPath(Collection<Field> fields) {
    return fields
               .stream()
               .filter(this::fieldNotEmpty)
               .collect(Collectors.toMap(Field::getJsonPath, Function.identity(), (f1, f2) -> f1));
  }

  private boolean fieldNotEmpty(Field field) {
    String jsonPath = field.getJsonPath();
    return !(StringUtils.isEmpty(jsonPath) || jsonPath.equalsIgnoreCase(EMPTY_JSON_PATH));
  }
}
