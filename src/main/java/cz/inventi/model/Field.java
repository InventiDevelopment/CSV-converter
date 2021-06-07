package cz.inventi.model;

public interface Field {

  String getName();

  String getJsonPath();

  void setJsonPath(String jsonPath);

  boolean isRequired();

}
