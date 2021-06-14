package cz.inventi.model;

/**
 * Type of {@link JsonPath}.
 */
public enum JsonPathType {
  /**
   * Used only for root Json path node
   */
  ROOT,
  /**
   * JSON property. Is used for all leaf nodes in JSON paths tree (e.g. organizations[*].users[*].id)
   */
  PROPERTY,
  /**
   * JSON array. Is used for all NON-leaf nodes in JSON paths tree (e.g. organizations[*].users)
   */
  ARRAY
}
