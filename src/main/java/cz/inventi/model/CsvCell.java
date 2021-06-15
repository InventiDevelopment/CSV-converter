package cz.inventi.model;

import java.util.List;

import cz.inventi.Constants;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Value;
import org.apache.commons.lang3.StringUtils;

import static cz.inventi.Constants.ESCAPED_ARRAY_IDENTIFIER;

/**
 * Defines where needed JSON property value (will be written to this CSV cell) should be searched (by path).
 */
@Value
@AllArgsConstructor
@Builder(toBuilder = true)
public class CsvCell {
  /**
   * JSON path (e.g. organizations[*].users[*].id), which value should be written to CSV cell.
   */
  String jsonPath;
  /**
   * Real indexes for {@link Constants#ARRAY_IDENTIFIER}s in jsonPath
   */
  List<Integer> indexes;
  /**
   * If value is required. If required is true, but value doesn't exist, then the whole row will be skipped
   */
  boolean required;

  /**
   * @return true if jsonPath is equal to special constant EMPTY_JSON_PATH, otherwise false
   */
  public boolean jsonPathIsEmpty() {
    return StringUtils.equals(jsonPath, DefaultCsvDefinition.EMPTY_JSON_PATH);
  }

  /**
   * @return jsonPath with filled indexes instead of [*] (e.g. organizations[*].users[*].id -> organizations[0].users[2].id)
   */
  public String getJsonPathWithFilledIndexes() {
    String result = jsonPath;
    for (int index : indexes) {
      result = result.replaceFirst(ESCAPED_ARRAY_IDENTIFIER, index + "");
    }
    return result;
  }
}
