package cz.inventi.utils;

import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Contains utils methods for work with Strings.
 */
public class StringUtils {

  /**
   * Gets values from string by regex.
   *
   * @param text text
   * @param regex regex
   * @return all matches of regex in text.
   */
  public static List<String> findAllValuesByRegex(String text, String regex) {
   return Pattern.compile("(?=(" + regex + "))")
           .matcher(text)
           .results()
           .map(r -> r.group(2))
           .collect(Collectors.toList());
  }
}
