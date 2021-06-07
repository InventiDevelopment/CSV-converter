package cz.inventi.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder(toBuilder = true)
public class JsonPath {
  private String jsonPath;
  private List<JsonPath> children = new ArrayList<>();

  private String arrayPath;
  private Map<List<Integer>, Integer> arrayIndexes = new HashMap<>();
}
