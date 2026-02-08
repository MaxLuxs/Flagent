# flagent_client.api.HealthApi

## Load the API package
```dart
import 'package:flagent_client/api.dart';
```

All URIs are relative to *http://localhost:18000/api/v1*

Method | HTTP request | Description
------------- | ------------- | -------------
[**getHealth**](HealthApi.md#gethealth) | **GET** /health | Health check
[**getInfo**](HealthApi.md#getinfo) | **GET** /info | Get version information


# **getHealth**
> Health getHealth()

Health check

Check if Flagent is healthy

### Example
```dart
import 'package:flagent_client/api.dart';

final api = FlagentClient().getHealthApi();

try {
    final response = api.getHealth();
    print(response);
} on DioException catch (e) {
    print('Exception when calling HealthApi->getHealth: $e\n');
}
```

### Parameters
This endpoint does not need any parameter.

### Return type

[**Health**](Health.md)

### Authorization

No authorization required

### HTTP request headers

 - **Content-Type**: Not defined
 - **Accept**: application/json

[[Back to top]](#) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to Model list]](../README.md#documentation-for-models) [[Back to README]](../README.md)

# **getInfo**
> Info getInfo()

Get version information

### Example
```dart
import 'package:flagent_client/api.dart';

final api = FlagentClient().getHealthApi();

try {
    final response = api.getInfo();
    print(response);
} on DioException catch (e) {
    print('Exception when calling HealthApi->getInfo: $e\n');
}
```

### Parameters
This endpoint does not need any parameter.

### Return type

[**Info**](Info.md)

### Authorization

No authorization required

### HTTP request headers

 - **Content-Type**: Not defined
 - **Accept**: application/json

[[Back to top]](#) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to Model list]](../README.md#documentation-for-models) [[Back to README]](../README.md)

