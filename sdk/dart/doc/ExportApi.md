# flagent_client.api.ExportApi

## Load the API package
```dart
import 'package:flagent_client/api.dart';
```

All URIs are relative to *http://localhost:18000/api/v1*

Method | HTTP request | Description
------------- | ------------- | -------------
[**getExportEvalCacheJSON**](ExportApi.md#getexportevalcachejson) | **GET** /export/eval_cache/json | Export eval cache as JSON
[**getExportSQLite**](ExportApi.md#getexportsqlite) | **GET** /export/sqlite | Export database as SQLite


# **getExportEvalCacheJSON**
> BuiltMap<String, JsonObject> getExportEvalCacheJSON()

Export eval cache as JSON

Export JSON format of the eval cache dump. This endpoint exports the current state of the evaluation cache in JSON format.

### Example
```dart
import 'package:flagent_client/api.dart';

final api = FlagentClient().getExportApi();

try {
    final response = api.getExportEvalCacheJSON();
    print(response);
} on DioException catch (e) {
    print('Exception when calling ExportApi->getExportEvalCacheJSON: $e\n');
}
```

### Parameters
This endpoint does not need any parameter.

### Return type

[**BuiltMap&lt;String, JsonObject&gt;**](JsonObject.md)

### Authorization

No authorization required

### HTTP request headers

 - **Content-Type**: Not defined
 - **Accept**: application/json

[[Back to top]](#) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to Model list]](../README.md#documentation-for-models) [[Back to README]](../README.md)

# **getExportSQLite**
> Uint8List getExportSQLite(excludeSnapshots)

Export database as SQLite

Export sqlite3 format of the db dump, which is converted from the main database. Returns a SQLite database file that can be used for backup or migration purposes.

### Example
```dart
import 'package:flagent_client/api.dart';

final api = FlagentClient().getExportApi();
final bool excludeSnapshots = true; // bool | Export without snapshots data - useful for smaller db without snapshots

try {
    final response = api.getExportSQLite(excludeSnapshots);
    print(response);
} on DioException catch (e) {
    print('Exception when calling ExportApi->getExportSQLite: $e\n');
}
```

### Parameters

Name | Type | Description  | Notes
------------- | ------------- | ------------- | -------------
 **excludeSnapshots** | **bool**| Export without snapshots data - useful for smaller db without snapshots | [optional] [default to false]

### Return type

[**Uint8List**](Uint8List.md)

### Authorization

No authorization required

### HTTP request headers

 - **Content-Type**: Not defined
 - **Accept**: application/octet-stream, application/json

[[Back to top]](#) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to Model list]](../README.md#documentation-for-models) [[Back to README]](../README.md)

