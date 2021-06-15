package cz.inventi.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import cz.inventi.Constants;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Value;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.StringUtils;

import static cz.inventi.Constants.ARRAY_IDENTIFIER_WITH_BRACKETS;
import static cz.inventi.Constants.NUMBER_ARRAY_IDENTIFIER_WITH_BRACKETS_REGEX;
import static cz.inventi.utils.StringUtils.findAllValuesByRegex;

/**
 * JSON path, node of JSON paths tree.
 * Contains parsed path from {@link Field} with children paths.
 * Tree of JSON paths is used to constructing separated records.
 */
@Value
@AllArgsConstructor
@Builder(toBuilder = true)
@Log4j2
public class JsonPath {

  /**
   * Type of JSON path
   */
  JsonPathType type;
  /**
   * JSON path
   */
  String path;
  /**
   * Children of JSON path (e.g. parent ARRAY 'organizations[*].users' -> child PROPERTY 'organizations[*].users[*].id')
   * Is used only for {@link JsonPathType#ARRAY}
   */
  List<JsonPath> children;
  /**
   * Map of jsonPath indexes -> arraySize (e.g. jsonPath is 'organizations[*].users[*].emails', map contains [0,1] -> 2,
   * it means that array organizations[0].users[1].emails has size 2).
   * Is used only for {@link JsonPathType#ARRAY}
   */
  Map<List<Integer>, Integer> arrayIndexes;

  public JsonPath(String path, JsonPathType type) {
    this.path = path;
    this.type = type;
    this.children = new ArrayList<>();
    this.arrayIndexes = new HashMap<>();
  }

  public JsonPath(JsonPathType type, List<JsonPath> children) {
    this.path = null;
    this.type = type;
    this.children = children;
    this.arrayIndexes = new HashMap<>();
  }

  /**
   * Finds existing JSON path, that is a descendant of this path, by jsonPath.
   *
   * @param jsonPath jsonPath of descendant JsonPath to be found
   *                 (e.g. 'contacts.emails[*].address' as property path or 'contacts.emails' as array path)
   * @return found descendant JsonPath. Returns null for null jsonPath
   */
  public JsonPath findDescendantJsonPath(String jsonPath) {
    if (jsonPath == null) {
      return null;
    }
    for (JsonPath path : children) {
      if (StringUtils.equals(path.getPath(), jsonPath)) {
        return path;
      }

      JsonPath tmp = path.findDescendantJsonPath(jsonPath);

      if (tmp != null) {
        return tmp;
      }
    }

    return null;
  }

  /**
   * Creates new JsonPath with jsonPathString and adds as descendant of this path (to subtree).
   * If needed JsonPath already exists, then returns existing (nothing new will be added).
   * <ol>
   *   <li>If parentPath is provided, adds new path as child of parentPath</li>
   *   <li>Otherwise adds it directly to children of this path</li>
   * </ol>
   *
   * @param jsonPathString json path
   * @param parentPath     optional parent path, if already known
   * @param type           type of json path
   * @return new added JsonPath or existing if already exists
   */
  public JsonPath addNewDescendantJsonPath(String jsonPathString, JsonPath parentPath, JsonPathType type) {
    JsonPath existing = findDescendantJsonPath(jsonPathString);
    if (existing != null) {
      log.debug("JSON path {} already exists in json paths tree", jsonPathString);
      return existing;
    }
    log.debug("JSON path {} doesn't exist in json paths tree, add it.", jsonPathString);
    JsonPath newPath = new JsonPath(jsonPathString, type);

    if (parentPath != null) {
      parentPath.getChildren().add(newPath);
    } else if (jsonPathString.equals(DefaultCsvDefinition.EMPTY_JSON_PATH)) {
      children.add(0, newPath);
    } else {
      children.add(newPath);
    }

    return newPath;
  }

  /**
   * Tries to find appropriate descendant parentPath
   * and calls {@link #addNewDescendantJsonPath(String, JsonPath, JsonPathType)}.
   *
   * @param jsonPathString json path
   * @param type           type of json path
   * @return new added JsonPath or existing if already exists
   */
  public JsonPath addNewDescendantJsonPath(String jsonPathString, JsonPathType type) {
    JsonPath parentPath = findDescendantJsonPath(getParentJsonPathString(jsonPathString));

    return addNewDescendantJsonPath(jsonPathString, parentPath, type);
  }

  /**
   * Finds all array indexes from JSON path.
   * e.g. JSON path = cash.registers[1].purchasers[3].values[4] => result is [1, 3, 4]
   *
   * @param jsonPath json path
   * @return list of array indexes
   */
  public static List<Integer> findJsonPathIndexes(String jsonPath) {
    List<String> valuesString = findAllValuesByRegex(jsonPath, NUMBER_ARRAY_IDENTIFIER_WITH_BRACKETS_REGEX);
    return valuesString.stream().map(Integer::parseInt).collect(Collectors.toList());
  }

  /**
   * Returns parentPath string for provided jsonPath string.
   * (e.g. organizations[*].users[*].id -> organizations[*].users)
   *
   * @param jsonPath json path (e.g. organizations[*].users[*].id)
   * @return parent path (e.g. organizations[*].users)
   */
  public static String getParentJsonPathString(String jsonPath) {
    return StringUtils.substringBeforeLast(jsonPath, ARRAY_IDENTIFIER_WITH_BRACKETS);
  }

  /**
   * @param jsonPath json path
   * @return counted matches of {@link Constants#ARRAY_IDENTIFIER_WITH_BRACKETS} in provided json path.
   */
  public static int countNestedArrays(String jsonPath) {
    log.trace("Check if the jsonPath {} contains array with '*'", jsonPath);
    return StringUtils.countMatches(jsonPath, ARRAY_IDENTIFIER_WITH_BRACKETS);
  }
}
