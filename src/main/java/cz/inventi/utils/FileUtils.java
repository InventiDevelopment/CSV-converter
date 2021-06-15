package cz.inventi.utils;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

/**
 * Contains methods for work with files and directories
 */
public class FileUtils {

  /**
   * Reads file with filename.
   *
   * @param filename filename
   * @return string content of file
   * @throws IOException when file cannot be read
   */
  public static String readFile(String filename) throws IOException {
    return org.apache.commons.io.FileUtils.readFileToString(new File(filename), StandardCharsets.UTF_8);
  }

  /**
   * Checks if target directory exists. If not, creates it.
   *
   * @param target path to target directory
   * @throws IOException when directory doesn't exist and couldn't be created.
   */
  public static void ensureTargetExists(String target) throws IOException {
    File directory = new File(target);

    if (!directory.exists() && !directory.mkdirs()) {
      throw new IOException("The target directory doesn't exist and couldn't be created.");
    }
  }
}
