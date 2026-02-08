# flagent_client.api.TagApi

## Load the API package
```dart
import 'package:flagent_client/api.dart';
```

All URIs are relative to *http://localhost:18000/api/v1*

Method | HTTP request | Description
------------- | ------------- | -------------
[**createFlagTag**](TagApi.md#createflagtag) | **POST** /flags/{flagId}/tags | Create tag and associate with flag
[**deleteFlagTag**](TagApi.md#deleteflagtag) | **DELETE** /flags/{flagId}/tags/{tagId} | Remove tag from flag
[**findAllTags**](TagApi.md#findalltags) | **GET** /tags | Get all tags
[**findFlagTags**](TagApi.md#findflagtags) | **GET** /flags/{flagId}/tags | Get tags for flag


# **createFlagTag**
> Tag createFlagTag(flagId, createTagRequest)

Create tag and associate with flag

Create a tag and associate it with the flag. Tags are used for organizing and filtering flags.

### Example
```dart
import 'package:flagent_client/api.dart';

final api = FlagentClient().getTagApi();
final int flagId = 789; // int | Numeric ID of the flag
final CreateTagRequest createTagRequest = {"value":"production"}; // CreateTagRequest | 

try {
    final response = api.createFlagTag(flagId, createTagRequest);
    print(response);
} on DioException catch (e) {
    print('Exception when calling TagApi->createFlagTag: $e\n');
}
```

### Parameters

Name | Type | Description  | Notes
------------- | ------------- | ------------- | -------------
 **flagId** | **int**| Numeric ID of the flag | 
 **createTagRequest** | [**CreateTagRequest**](CreateTagRequest.md)|  | 

### Return type

[**Tag**](Tag.md)

### Authorization

No authorization required

### HTTP request headers

 - **Content-Type**: application/json
 - **Accept**: application/json

[[Back to top]](#) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to Model list]](../README.md#documentation-for-models) [[Back to README]](../README.md)

# **deleteFlagTag**
> deleteFlagTag(flagId, tagId)

Remove tag from flag

### Example
```dart
import 'package:flagent_client/api.dart';

final api = FlagentClient().getTagApi();
final int flagId = 789; // int | Numeric ID of the flag
final int tagId = 789; // int | Numeric ID of the tag

try {
    api.deleteFlagTag(flagId, tagId);
} on DioException catch (e) {
    print('Exception when calling TagApi->deleteFlagTag: $e\n');
}
```

### Parameters

Name | Type | Description  | Notes
------------- | ------------- | ------------- | -------------
 **flagId** | **int**| Numeric ID of the flag | 
 **tagId** | **int**| Numeric ID of the tag | 

### Return type

void (empty response body)

### Authorization

No authorization required

### HTTP request headers

 - **Content-Type**: Not defined
 - **Accept**: application/json

[[Back to top]](#) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to Model list]](../README.md#documentation-for-models) [[Back to README]](../README.md)

# **findAllTags**
> BuiltList<Tag> findAllTags(limit, offset, valueLike)

Get all tags

### Example
```dart
import 'package:flagent_client/api.dart';

final api = FlagentClient().getTagApi();
final int limit = 789; // int | The numbers of tags to return
final int offset = 789; // int | Return tags given the offset
final String valueLike = valueLike_example; // String | Return tags partially matching given value

try {
    final response = api.findAllTags(limit, offset, valueLike);
    print(response);
} on DioException catch (e) {
    print('Exception when calling TagApi->findAllTags: $e\n');
}
```

### Parameters

Name | Type | Description  | Notes
------------- | ------------- | ------------- | -------------
 **limit** | **int**| The numbers of tags to return | [optional] 
 **offset** | **int**| Return tags given the offset | [optional] [default to 0]
 **valueLike** | **String**| Return tags partially matching given value | [optional] 

### Return type

[**BuiltList&lt;Tag&gt;**](Tag.md)

### Authorization

No authorization required

### HTTP request headers

 - **Content-Type**: Not defined
 - **Accept**: application/json

[[Back to top]](#) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to Model list]](../README.md#documentation-for-models) [[Back to README]](../README.md)

# **findFlagTags**
> BuiltList<Tag> findFlagTags(flagId)

Get tags for flag

### Example
```dart
import 'package:flagent_client/api.dart';

final api = FlagentClient().getTagApi();
final int flagId = 789; // int | Numeric ID of the flag

try {
    final response = api.findFlagTags(flagId);
    print(response);
} on DioException catch (e) {
    print('Exception when calling TagApi->findFlagTags: $e\n');
}
```

### Parameters

Name | Type | Description  | Notes
------------- | ------------- | ------------- | -------------
 **flagId** | **int**| Numeric ID of the flag | 

### Return type

[**BuiltList&lt;Tag&gt;**](Tag.md)

### Authorization

No authorization required

### HTTP request headers

 - **Content-Type**: Not defined
 - **Accept**: application/json

[[Back to top]](#) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to Model list]](../README.md#documentation-for-models) [[Back to README]](../README.md)

