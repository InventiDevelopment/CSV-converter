# Basic converter example

The example shows an ability of the CSV converter to do custom mapping rather than 1 to 1 mapping. 
CsvField has a new constructor which accepts 
```
BiFunction<Field, String, List<String>> customMapper 
```
as parameter for custom mapping. <br /><br />
The code inside `CustomFieldMappingConverter` converts the input JSON file (`input.json`) to CSV; `output.csv` shows the result of such action.

> Note: The code is simplified (no package etc.) and assumes the CWD to be set to the current directory.
