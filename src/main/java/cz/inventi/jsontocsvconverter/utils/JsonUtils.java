package cz.inventi.jsontocsvconverter.utils;

import com.jayway.jsonpath.Configuration;
import com.jayway.jsonpath.DocumentContext;
import com.jayway.jsonpath.PathNotFoundException;
import lombok.extern.log4j.Log4j2;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import org.apache.commons.io.IOUtils;

/**
 * Contains util methods useful for work with JSON files.
 */
@Log4j2
public class JsonUtils {

  /**
   * Parses JSON file.
   *
   * @param filename JSON filename
   * @return parsed json file (as DocumentContext)
   * @throws IOException when file can't be read
   */
  public static DocumentContext parseJsonFile(String filename) throws IOException {
    return com.jayway.jsonpath.JsonPath.using(Configuration.defaultConfiguration()).parse(FileUtils.readFile(filename));
  }

  /**
   * Parses JSON file.
   *
   * @param file JSON file
   * @return parsed json file (as DocumentContext)
   * @throws IOException when file can't be read
   */
  public static DocumentContext parseJsonFile(File file) throws IOException {
    return com.jayway.jsonpath.JsonPath.using(Configuration.defaultConfiguration()).parse(FileUtils.readFile(file));
  }

  /**
   * Parses JSON file.
   *
   * @param input stream JSON data
   * @return parsed json file (as DocumentContext)
   * @throws IOException when file can't be read
   */
  public static DocumentContext parseJsonFile(InputStream input) throws IOException {
    return com.jayway.jsonpath.JsonPath.using(Configuration.defaultConfiguration()).parse(
        IOUtils.toString(input, StandardCharsets.UTF_8));
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
