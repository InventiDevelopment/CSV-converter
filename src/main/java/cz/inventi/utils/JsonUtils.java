package cz.inventi.utils;

import com.jayway.jsonpath.Configuration;
import com.jayway.jsonpath.DocumentContext;
import com.jayway.jsonpath.PathNotFoundException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;

import static cz.inventi.utils.FIleUtils.readFile;

/**
 * Contains util methods useful for work with JSON files.
 */
public class JsonUtils {
  private static final Logger log = LogManager.getLogger(JsonUtils.class);

  /**
   * Parses JSON file.
   *
   * @param filename JSON filename
   * @return parsed json file (as DocumentContext)
   * @throws IOException when file can't be read
   */
  public static DocumentContext parseJsonFile(String filename) throws IOException {
    return com.jayway.jsonpath.JsonPath.using(Configuration.defaultConfiguration()).parse(readFile(filename));
  }

  /**
   * @param arrayPathString array path (e.g. organizations[0].users)
   * @param context JSON context
   * @return size of array defined by arrayPathString
   */
  public static int getArraySize(String arrayPathString, DocumentContext context) {
    try {
      return context.read(String.format("%s.length()", arrayPathString));
    } catch (PathNotFoundException e) {
      log.trace("No data was found for JSON path {}.", arrayPathString);
      return 0;
    }
  }

}
