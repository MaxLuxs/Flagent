# flagent_client.api.FlagApi

## Load the API package
```dart
import 'package:flagent_client/api.dart';
```

All URIs are relative to *http://localhost:18000/api/v1*

Method | HTTP request | Description
------------- | ------------- | -------------
[**createFlag**](FlagApi.md#createflag) | **POST** /flags | Create a new flag
[**deleteFlag**](FlagApi.md#deleteflag) | **DELETE** /flags/{flagId} | Delete flag
[**findFlags**](FlagApi.md#findflags) | **GET** /flags | Get all flags
[**getFlag**](FlagApi.md#getflag) | **GET** /flags/{flagId} | Get flag by ID
[**getFlagEntityTypes**](FlagApi.md#getflagentitytypes) | **GET** /flags/entity_types | Get all entity types
[**getFlagSnapshots**](FlagApi.md#getflagsnapshots) | **GET** /flags/{flagId}/snapshots | Get flag snapshots
[**putFlag**](FlagApi.md#putflag) | **PUT** /flags/{flagId} | Update flag
[**restoreFlag**](FlagApi.md#restoreflag) | **PUT** /flags/{flagId}/restore | Restore deleted flag
[**setFlagEnabled**](FlagApi.md#setflagenabled) | **PUT** /flags/{flagId}/enabled | Set flag enabled status


# **createFlag**
> Flag createFlag(createFlagRequest)

Create a new flag

### Example
```dart
import 'package:flagent_client/api.dart';

final api = FlagentClient().getFlagApi();
final CreateFlagRequest createFlagRequest = {"description":"New feature flag","key":"new_feature"}; // CreateFlagRequest | 

try {
    final response = api.createFlag(createFlagRequest);
    print(response);
} on DioException catch (e) {
    print('Exception when calling FlagApi->createFlag: $e\n');
}
```

### Parameters

Name | Type | Description  | Notes
------------- | ------------- | ------------- | -------------
 **createFlagRequest** | [**CreateFlagRequest**](CreateFlagRequest.md)|  | 

### Return type

[**Flag**](Flag.md)

### Authorization

No authorization required

### HTTP request headers

 - **Content-Type**: application/json
 - **Accept**: application/json

[[Back to top]](#) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to Model list]](../README.md#documentation-for-models) [[Back to README]](../README.md)

# **deleteFlag**
> deleteFlag(flagId)

Delete flag

### Example
```dart
import 'package:flagent_client/api.dart';

final api = FlagentClient().getFlagApi();
final int flagId = 789; // int | Numeric ID of the flag

try {
    api.deleteFlag(flagId);
} on DioException catch (e) {
    print('Exception when calling FlagApi->deleteFlag: $e\n');
}
```

### Parameters

Name | Type | Description  | Notes
------------- | ------------- | ------------- | -------------
 **flagId** | **int**| Numeric ID of the flag | 

### Return type

void (empty response body)

### Authorization

No authorization required

### HTTP request headers

 - **Content-Type**: Not defined
 - **Accept**: application/json

[[Back to top]](#) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to Model list]](../README.md#documentation-for-models) [[Back to README]](../README.md)

# **findFlags**
> BuiltList<Flag> findFlags(limit, offset, enabled, description, key, descriptionLike, preload, deleted, tags)

Get all flags

### Example
```dart
import 'package:flagent_client/api.dart';

final api = FlagentClient().getFlagApi();
final int limit = 789; // int | The numbers of flags to return
final int offset = 789; // int | Return flags given the offset, it should usually set together with limit
final bool enabled = true; // bool | Return flags having given enabled status
final String description = description_example; // String | Filter flags by exact description match
final String key = key_example; // String | Filter flags by exact key match
final String descriptionLike = descriptionLike_example; // String | Filter flags by partial description match
final bool preload = true; // bool | Preload segments, variants, and tags
final bool deleted = true; // bool | Include deleted flags in results
final String tags = tags_example; // String | Filter flags by tags (comma-separated)

try {
    final response = api.findFlags(limit, offset, enabled, description, key, descriptionLike, preload, deleted, tags);
    print(response);
} on DioException catch (e) {
    print('Exception when calling FlagApi->findFlags: $e\n');
}
```

### Parameters

Name | Type | Description  | Notes
------------- | ------------- | ------------- | -------------
 **limit** | **int**| The numbers of flags to return | [optional] 
 **offset** | **int**| Return flags given the offset, it should usually set together with limit | [optional] [default to 0]
 **enabled** | **bool**| Return flags having given enabled status | [optional] 
 **description** | **String**| Filter flags by exact description match | [optional] 
 **key** | **String**| Filter flags by exact key match | [optional] 
 **descriptionLike** | **String**| Filter flags by partial description match | [optional] 
 **preload** | **bool**| Preload segments, variants, and tags | [optional] [default to false]
 **deleted** | **bool**| Include deleted flags in results | [optional] [default to false]
 **tags** | **String**| Filter flags by tags (comma-separated) | [optional] 

### Return type

[**BuiltList&lt;Flag&gt;**](Flag.md)

### Authorization

No authorization required

### HTTP request headers

 - **Content-Type**: Not defined
 - **Accept**: application/json

[[Back to top]](#) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to Model list]](../README.md#documentation-for-models) [[Back to README]](../README.md)

# **getFlag**
> Flag getFlag(flagId)

Get flag by ID

### Example
```dart
import 'package:flagent_client/api.dart';

final api = FlagentClient().getFlagApi();
final int flagId = 789; // int | Numeric ID of the flag

try {
    final response = api.getFlag(flagId);
    print(response);
} on DioException catch (e) {
    print('Exception when calling FlagApi->getFlag: $e\n');
}
```

### Parameters

Name | Type | Description  | Notes
------------- | ------------- | ------------- | -------------
 **flagId** | **int**| Numeric ID of the flag | 

### Return type

[**Flag**](Flag.md)

### Authorization

No authorization required

### HTTP request headers

 - **Content-Type**: Not defined
 - **Accept**: application/json

[[Back to top]](#) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to Model list]](../README.md#documentation-for-models) [[Back to README]](../README.md)

# **getFlagEntityTypes**
> BuiltList<String> getFlagEntityTypes()

Get all entity types

### Example
```dart
import 'package:flagent_client/api.dart';

final api = FlagentClient().getFlagApi();

try {
    final response = api.getFlagEntityTypes();
    print(response);
} on DioException catch (e) {
    print('Exception when calling FlagApi->getFlagEntityTypes: $e\n');
}
```

### Parameters
This endpoint does not need any parameter.

### Return type

**BuiltList&lt;String&gt;**

### Authorization

No authorization required

### HTTP request headers

 - **Content-Type**: Not defined
 - **Accept**: application/json

[[Back to top]](#) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to Model list]](../README.md#documentation-for-models) [[Back to README]](../README.md)

# **getFlagSnapshots**
> BuiltList<FlagSnapshot> getFlagSnapshots(flagId, limit, offset, sort)

Get flag snapshots

### Example
```dart
import 'package:flagent_client/api.dart';

final api = FlagentClient().getFlagApi();
final int flagId = 789; // int | Numeric ID of the flag
final int limit = 789; // int | The number of snapshots to return
final int offset = 789; // int | Return snapshots given the offset
final String sort = sort_example; // String | Sort order

try {
    final response = api.getFlagSnapshots(flagId, limit, offset, sort);
    print(response);
} on DioException catch (e) {
    print('Exception when calling FlagApi->getFlagSnapshots: $e\n');
}
```

### Parameters

Name | Type | Description  | Notes
------------- | ------------- | ------------- | -------------
 **flagId** | **int**| Numeric ID of the flag | 
 **limit** | **int**| The number of snapshots to return | [optional] 
 **offset** | **int**| Return snapshots given the offset | [optional] [default to 0]
 **sort** | **String**| Sort order | [optional] 

### Return type

[**BuiltList&lt;FlagSnapshot&gt;**](FlagSnapshot.md)

### Authorization

No authorization required

### HTTP request headers

 - **Content-Type**: Not defined
 - **Accept**: application/json

[[Back to top]](#) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to Model list]](../README.md#documentation-for-models) [[Back to README]](../README.md)

# **putFlag**
> Flag putFlag(flagId, putFlagRequest)

Update flag

### Example
```dart
import 'package:flagent_client/api.dart';

final api = FlagentClient().getFlagApi();
final int flagId = 789; // int | Numeric ID of the flag
final PutFlagRequest putFlagRequest = ; // PutFlagRequest | 

try {
    final response = api.putFlag(flagId, putFlagRequest);
    print(response);
} on DioException catch (e) {
    print('Exception when calling FlagApi->putFlag: $e\n');
}
```

### Parameters

Name | Type | Description  | Notes
------------- | ------------- | ------------- | -------------
 **flagId** | **int**| Numeric ID of the flag | 
 **putFlagRequest** | [**PutFlagRequest**](PutFlagRequest.md)|  | 

### Return type

[**Flag**](Flag.md)

### Authorization

No authorization required

### HTTP request headers

 - **Content-Type**: application/json
 - **Accept**: application/json

[[Back to top]](#) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to Model list]](../README.md#documentation-for-models) [[Back to README]](../README.md)

# **restoreFlag**
> Flag restoreFlag(flagId)

Restore deleted flag

### Example
```dart
import 'package:flagent_client/api.dart';

final api = FlagentClient().getFlagApi();
final int flagId = 789; // int | Numeric ID of the flag

try {
    final response = api.restoreFlag(flagId);
    print(response);
} on DioException catch (e) {
    print('Exception when calling FlagApi->restoreFlag: $e\n');
}
```

### Parameters

Name | Type | Description  | Notes
------------- | ------------- | ------------- | -------------
 **flagId** | **int**| Numeric ID of the flag | 

### Return type

[**Flag**](Flag.md)

### Authorization

No authorization required

### HTTP request headers

 - **Content-Type**: Not defined
 - **Accept**: application/json

[[Back to top]](#) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to Model list]](../README.md#documentation-for-models) [[Back to README]](../README.md)

# **setFlagEnabled**
> Flag setFlagEnabled(flagId, setFlagEnabledRequest)

Set flag enabled status

### Example
```dart
import 'package:flagent_client/api.dart';

final api = FlagentClient().getFlagApi();
final int flagId = 789; // int | Numeric ID of the flag
final SetFlagEnabledRequest setFlagEnabledRequest = {"enabled":true}; // SetFlagEnabledRequest | 

try {
    final response = api.setFlagEnabled(flagId, setFlagEnabledRequest);
    print(response);
} on DioException catch (e) {
    print('Exception when calling FlagApi->setFlagEnabled: $e\n');
}
```

### Parameters

Name | Type | Description  | Notes
------------- | ------------- | ------------- | -------------
 **flagId** | **int**| Numeric ID of the flag | 
 **setFlagEnabledRequest** | [**SetFlagEnabledRequest**](SetFlagEnabledRequest.md)|  | 

### Return type

[**Flag**](Flag.md)

### Authorization

No authorization required

### HTTP request headers

 - **Content-Type**: application/json
 - **Accept**: application/json

[[Back to top]](#) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to Model list]](../README.md#documentation-for-models) [[Back to README]](../README.md)

