package cz.inventi.model;

/**
 * Definition of needed JSON property, that should be written to target file.
 */
public interface Field {
  /**
   * @return field name that is used as column name
   */
  String getName();

  /**
   * @return json path of property, which value should be written to column
   */
  String getJsonPath();

  /**
   * @return If value is required. If required is true, but value doesn't exist, then the whole row will be skipped
   */
  boolean isRequired();

}
