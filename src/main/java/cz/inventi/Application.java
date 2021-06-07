package cz.inventi;

import java.io.IOException;

public class Application {

  public Application() {
    CsvConvertor convertor = new CsvConvertor();
    try {
      convertor.convert("test.json", "output");
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  public static void main(String[] args) {
    new Application();

  }
}
