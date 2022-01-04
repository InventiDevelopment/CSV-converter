package cz.inventi.jsontocsvconverter.model;

import java.util.List;
import java.util.function.BiFunction;

/**
 * Definition of one field of target file.
 */
public interface Field {
  /**
   * @return field name
   */
  String getName();

  /**
   * @return path to related JSON property, that should be converted to this field
   */
  String getJsonPath();

  /**
   * @return If value is required. If required is true, but value doesn't exist, then the whole row will be skipped
   */
  boolean isRequired();

  /**
   * @return Value from the JSON is changed to different value based on converter.
   */
  BiFunction<Field, String, List<String>> getCustomMapper();

}
