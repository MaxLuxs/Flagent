# flagent_client.api.DistributionApi

## Load the API package
```dart
import 'package:flagent_client/api.dart';
```

All URIs are relative to *http://localhost:18000/api/v1*

Method | HTTP request | Description
------------- | ------------- | -------------
[**findDistributions**](DistributionApi.md#finddistributions) | **GET** /flags/{flagId}/segments/{segmentId}/distributions | Get distributions for segment
[**putDistributions**](DistributionApi.md#putdistributions) | **PUT** /flags/{flagId}/segments/{segmentId}/distributions | Update distributions


# **findDistributions**
> BuiltList<Distribution> findDistributions(flagId, segmentId)

Get distributions for segment

### Example
```dart
import 'package:flagent_client/api.dart';

final api = FlagentClient().getDistributionApi();
final int flagId = 789; // int | Numeric ID of the flag
final int segmentId = 789; // int | Numeric ID of the segment

try {
    final response = api.findDistributions(flagId, segmentId);
    print(response);
} on DioException catch (e) {
    print('Exception when calling DistributionApi->findDistributions: $e\n');
}
```

### Parameters

Name | Type | Description  | Notes
------------- | ------------- | ------------- | -------------
 **flagId** | **int**| Numeric ID of the flag | 
 **segmentId** | **int**| Numeric ID of the segment | 

### Return type

[**BuiltList&lt;Distribution&gt;**](Distribution.md)

### Authorization

No authorization required

### HTTP request headers

 - **Content-Type**: Not defined
 - **Accept**: application/json

[[Back to top]](#) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to Model list]](../README.md#documentation-for-models) [[Back to README]](../README.md)

# **putDistributions**
> BuiltList<Distribution> putDistributions(flagId, segmentId, putDistributionsRequest)

Update distributions

Replace the distribution with the new setting. The sum of all percentages must equal 100.

### Example
```dart
import 'package:flagent_client/api.dart';

final api = FlagentClient().getDistributionApi();
final int flagId = 789; // int | Numeric ID of the flag
final int segmentId = 789; // int | Numeric ID of the segment
final PutDistributionsRequest putDistributionsRequest = {"distributions":[{"variantID":1,"variantKey":"control","percent":50},{"variantID":2,"variantKey":"treatment","percent":50}]}; // PutDistributionsRequest | 

try {
    final response = api.putDistributions(flagId, segmentId, putDistributionsRequest);
    print(response);
} on DioException catch (e) {
    print('Exception when calling DistributionApi->putDistributions: $e\n');
}
```

### Parameters

Name | Type | Description  | Notes
------------- | ------------- | ------------- | -------------
 **flagId** | **int**| Numeric ID of the flag | 
 **segmentId** | **int**| Numeric ID of the segment | 
 **putDistributionsRequest** | [**PutDistributionsRequest**](PutDistributionsRequest.md)|  | 

### Return type

[**BuiltList&lt;Distribution&gt;**](Distribution.md)

### Authorization

No authorization required

### HTTP request headers

 - **Content-Type**: application/json
 - **Accept**: application/json

[[Back to top]](#) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to Model list]](../README.md#documentation-for-models) [[Back to README]](../README.md)

