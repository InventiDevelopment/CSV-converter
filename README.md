# JSON to CSV converter

This library provides a configurable JSON → CSV converter. The main features are:

1. Ability to define which fields should be converted from JSON.
2. Ability to define the names of converted fields in CSV (columns headers).
3. Mark field as required if needed - then the whole row won't be added if this field isn't present.
4. You can choose output, It can be either CSV file or OutputStream.

## How to use
`JsonToCsvConverter.convert` receives the source JSON filename and `CsvDefinition`. There are two implementations for `CsvDefinition`: </br>
- `FileCsvDefinition` for CSV file as an output
- `OutputStreamCsvDefinition` for OutputStream as an output


A list of `CsvField` defines how the JSON file should be converted. `CsvField` represents a CSV column and contains:
- **name** - the column name in the output CSV,
- **source JSON path** - the path to the related JSON property that should be converted to this field (see supported JSON paths below)
- **flag required** - if set to `true` and the value doesn't exist (is not present), then the whole row will be skipped.
- **customMapper** - Java BiFunction<Field, String, List<String>> parameter, You can easily define custom mapping via lambda. If not set, field is mapped 1 to 1.

### Supported JSON paths
These types of JSON paths are currently supported:

#### Simple paths

- plain fields - `"name"`, `"version"`
- nested object fields -  `"options.advanced"`, `"some.nested.property"`

#### Paths containing arrays   
These paths contain arrays marked with the array identifier `[*]` - "array paths".

- arrays of plain values - `"phones[*]"`
  - used for arrays of strings, numbers etc.
- object fields inside of arrays - `"organizations[*].name"`, `"organizations[*].id"`
- nested arrays, object fields inside of nested arrays - `"users[*].phones[*]"`, `"organizations[*].users[*].name"`

In these cases, more CSV rows will be generated. For example, the path `"organizations[*].users[*].name"` and JSON

```json
{
  "organizations": [
    {"users": [{"name": "first"}, {"name": "second"}]},
    {"users": [{"name": "third"}]},
    {"users": [{"name": "fourth"}, {"name": "fifth"}, {"name": "sixth"}]}
  ]
}
```

will result in 6 rows (2 + 1 + 3 users). Therefore, the number of output rows is equal to the number of nested items defined by the path,
`"users"` in this case. Plain fields defined by simple paths will have the same values for each of these rows (they are duplicated).

The number of generated rows doesn't depend on whether the array is a root object field or a field of some nested object, so:
- `"user.phones[*]"` is identical to `"phones[*]"`
- `"organizations[*].name"` is identical to `"some.nested.object.organizations[*].name"`
- `"organizations[*].users[*].name"` is identical to `"data.organizations[*].users[*].name"` etc.

### Combining paths
The list of `CsvField` can contain different combinations of both simple and array paths.
 
#### Only simple paths
Simple paths always generate only 1 row per item.

#### Only one "array path"
In case there's only one "array path" and the rest are simple paths (or no other paths), the number of rows is equal to the number of items for the given "array path" (the number of the nested items).

#### Hierarchical combination of "array paths"
In this case, a hierarchical combination of "array paths" and other simple paths (or no other paths) is used - for example `["organizations[*].name", "organizations[*].users[*].name", "organizations[*].users[*].emails[*].address"]`.
The number of rows is equal to the number of items in the most nested path - so in this case, it would be the last path (and it would produce one row for each of the most nested items - `emails`, in this example).

The values produced by other paths will be duplicated for deeper paths, for example, if the first `organization` object contains 3 `users`, each having 1 `email`, then `organization.name` will be duplicated 3 times for each of these `users` rows (see example below).

#### Non-hierarchical combination of "array paths" (NOT YET SUPPORTED)
Non-hierarchical combination of "array paths" and other simple paths (or no other paths).

**Please note:** the current version of `JsonToCsvConverter` doesn't support this case yet. For example, combination of paths `"emails[*].address"` and `"phones[*]"` for JSON

```json
{
  "emails": [
    {"address": "email1@email.com", "active": true},
    {"address": "email2@email.com", "active": false},
    {"address": "email3@email.com"}
  ],
  "phones": ["12345", "67890", "13579"]
}
```
can't be converted. 

If you need to convert such JSON, we recommend to "split" the non-hierarchical combination into multiple hierarchical combinations and process (join) the CSVs after conversion. So in this case, separately convert `"emails[*].address"` and `"phones[*]"` to CSVs and then join the result CSVs as you need.

## Example

### Code

```java
import JsonToCsvConverter;
import CsvDefinition;
import DefaultCsvDefinition;
import CsvField;

public class Example {

  public static void main(String[] args) {
    JsonToCsvConverter converter = new JsonToCsvConverter();
    CsvDefinition csvDefinition = new DefaultCsvDefinition("Example", "my/output/file.csv", List.of(
        new CsvField("NAME", "name", false),
        new CsvField("VERSION", "version", false),
        new CsvField("DATE", "date", false),
        new CsvField("ORG ID", "organizations[*].id", false),
        new CsvField("ORG NAME", "organizations[*].name", false),
        new CsvField("USER ID", "organizations[*].users[*].id", false),
        new CsvField("USER NAME", "organizations[*].users[*].name", false)
    ));
    converter.convert("my/source/file.json", csvDefinition);
  }
}
```

### Input JSON

```json
{
  "name": "Field name",
  "version": "1.0.0",
  "date": "2021-07-05",
  "options": {
    "advanced": "value"
  },
  "organizations": [
    {
      "id": "42e2190f-7fb5-4b19-97e5-8f6c90276167",
      "name": "First test organization",
      "description": "First description",
      "users": [
        {
          "id": "a1535974-5946-4d07-80da-1a55925bf912",
          "name": "First test user"
        },
        {
          "id": "69c5abb9-7a10-43ab-bee9-e5ac3d0f745b",
          "name": "Second test user"
        },
        {
          "id": "lwmrftvs-ypwd-b65w-o9hc-x8n4w95iogo0",
          "name": "Third test user"
        },
        {
          "id": "f8kdwhh9-mmfa-umn3-uk6rtajiqystj4mo",
          "name": "Fourth test user"
        }
      ]

    },
    {
      "id": "53d3def7-d402-4d04-845e-b9da7c532dbd",
      "name": "Second test organization",
      "description": "Second description",
      "users": [
        {
          "id": "okqbgvmb-rjdm-uino-swgj60nocadn8g5w",
          "name": "Fifth test user"
        },
        {
          "id": "0d93fe40-bb90-4f17-b83f-78437754da89",
          "name": "Sixth test user"
        }
      ]
    }
  ]
}
```

### Result (CSV output)

```csv
NAME;VERSION;DATE;ORG ID;ORG NAME;USER ID;USER NAME
Field name;1.0.0;2021-07-05;42e2190f-7fb5-4b19-97e5-8f6c90276167;First test organization;a1535974-5946-4d07-80da-1a55925bf912;First test user
Field name;1.0.0;2021-07-05;42e2190f-7fb5-4b19-97e5-8f6c90276167;First test organization;69c5abb9-7a10-43ab-bee9-e5ac3d0f745b;Second test user
Field name;1.0.0;2021-07-05;42e2190f-7fb5-4b19-97e5-8f6c90276167;First test organization;lwmrftvs-ypwd-b65w-o9hc-x8n4w95iogo0;Third test user
Field name;1.0.0;2021-07-05;42e2190f-7fb5-4b19-97e5-8f6c90276167;First test organization;f8kdwhh9-mmfa-umn3-uk6rtajiqystj4mo;Fourth test user
Field name;1.0.0;2021-07-05;53d3def7-d402-4d04-845e-b9da7c532dbd;Second test organization;okqbgvmb-rjdm-uino-swgj60nocadn8g5w;Fifth test user
Field name;1.0.0;2021-07-05;53d3def7-d402-4d04-845e-b9da7c532dbd;Second test organization;0d93fe40-bb90-4f17-b83f-78437754da89;Sixth test user

```
See more examples in **src/test** folder.
