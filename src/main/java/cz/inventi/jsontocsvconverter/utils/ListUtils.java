package cz.inventi.jsontocsvconverter.utils;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Contains utils methods for work with Lists.
 */
public class ListUtils {

  /**
   * @param originalList original list
   * @param newItems new items
   * @param <T> some type of list item
   * @return extended list with all items from original lists and newItems. Returns if originalList or newItems is null.
   */
  @SafeVarargs
  public static <T> List<T> extendedList(List<T> originalList, T ...newItems) {
    if (originalList == null || newItems == null) {
      return null;
    }
    return Stream.concat(originalList.stream(), Arrays.stream(newItems)).collect(Collectors.toList());
  }
}
