# JSON to CSV converter

This library provides configurable JSON → CSV converter. Main features are:

1. Define, which fields should be converted from JSON
2. Define the names of converted fields in CSV (names of columns)
3. Mark field as required if needed - then the whole row won't be added if this field isn't present

## How to use
`JsonToCsvConverter.convert` receives source JSON filename and `CsvDefinition`. You can use prepared
`DefaultCsvDefinition` and define only its name, target filename (full name with path) and list of `CsvField`.

List of `CsvField` defines how the JSON file should be converted. `CsvField` represents CSV column and contains:
- **name** - name of related column in output CSV
- **source JSON path** - path to related JSON property, that should be converted to this field (see supported JSON paths below)
- **flag required** - If required is true, but value doesn't exist, then the whole row will be skipped

### Supported JSON paths
These types of JSON paths are now supported:

#### Simple paths

- simple plane fields - `"name"`, `"version"`
- nested fields -  `"options.advanced"`, `"some.nested.property"`

#### Paths containing arrays   
These paths contains arrays marked by array identifier `[*]`. Let's call them "array paths".

- plane value array item - `"phones[*]"`
  - used for arrays of strings, numbers etc.
- field of array item - `"organizations[*].name"`, `"organizations[*].id"`
- plane value or field of nested array's item (item of array, which is a field of items of another array) -
  `"users[*].phones[*]"`, `"organizations[*].users[*].name"`

In this case more CSV rows will be generated. For example for path `"organizations[*].users[*].name"` and JSON

```json
{
  "organizations": [
    {"users": [{"name": "first"}, {"name": "second"}]},
    {"users": [{"name": "third"}]},
    {"users": [{"name": "fourth"}, {"name": "fifth"}, {"name": "sixth"}]},
  ]
}
```

will be genereated 6 rows (2 + 1 + 3 users). So the number of rows equals to the number of _the deepest_ items defined by path,
`"users"` in this case. Plane fields defined by simple paths will have the same values for each of these rows.

Number of generated rows doesn't depends on whether array is root object field or field of some nested object,
so:
- `"user.phones[*]"` similar to `"phones[*]"`
- `"organizations[*].name"` similar to `"some.nested.object.organizations[*].name"`
- `"organizations[*].users[*].name"` similar to `"data.organizations[*].users[*].name"` etc.

### Combining paths
List of `CsvField` can contain different combinations of simple and array paths.
 
#### Only simple paths
Always generate only 1 row.
#### Only one "array path"
Only one "array path" and other simple paths (or no other paths). 
Number of rows equals to number of rows for that "array path" (number of _the deepest_ items).

#### Hierarchical combination of "array paths"
Hierarchical combination of "array paths" and other simple paths (or no other paths).

As `["organizations[*].name", "organizations[*].users[*].name", "organizations[*].users[*].emails[*].address"]`.
Number of rows is equals to the number of _the deepest_ path, so the last path in this case (which prooduce one row for each of _the deepest_ items, so `emails` in this case).

Values produced by other paths will be duplicated for deeper paths, for example if first `organization` contains 3 `users`, each has 1`email`, then `organization.name` will be duplicated
3 times for each of this "users" rows (see example below).
#### Non-hierarchical combination of "array paths" (NOT YET SUPPORTED)
Non-hierarchical combination of "array paths" and other simple paths (or no other paths).

**Note that** now `JsonToCsvConverter` doesn't support this case. For example combination of paths `"emails[*].address"` and `"phones[*]"` for JSON

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

If you need to convert such JSON, **we recommend** to "split" non-hierarchical combination to multiple hierarchical combinations and somehow process (join) that CSVs after conversion.
So in this case separately convert `"emails[*].address"` and `"phones[*]"` to CSV, then join result CSVs as you will need.
### Example
Code:
```java
import cz.inventi.JsonToCsvConverter;
import cz.inventi.model.CsvDefinition;
import cz.inventi.model.DefaultCsvDefinition;
import cz.inventi.model.CsvField;

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

Input JSON:
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
Output CSV:
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