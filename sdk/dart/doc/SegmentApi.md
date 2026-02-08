# flagent_client.api.SegmentApi

## Load the API package
```dart
import 'package:flagent_client/api.dart';
```

All URIs are relative to *http://localhost:18000/api/v1*

Method | HTTP request | Description
------------- | ------------- | -------------
[**createSegment**](SegmentApi.md#createsegment) | **POST** /flags/{flagId}/segments | Create segment
[**deleteSegment**](SegmentApi.md#deletesegment) | **DELETE** /flags/{flagId}/segments/{segmentId} | Delete segment
[**findSegments**](SegmentApi.md#findsegments) | **GET** /flags/{flagId}/segments | Get segments for flag
[**putSegment**](SegmentApi.md#putsegment) | **PUT** /flags/{flagId}/segments/{segmentId} | Update segment
[**putSegmentReorder**](SegmentApi.md#putsegmentreorder) | **PUT** /flags/{flagId}/segments/reorder | Reorder segments


# **createSegment**
> Segment createSegment(flagId, createSegmentRequest)

Create segment

Create a new segment for the flag. Segments define the audience and are evaluated in order by rank.

### Example
```dart
import 'package:flagent_client/api.dart';

final api = FlagentClient().getSegmentApi();
final int flagId = 789; // int | Numeric ID of the flag
final CreateSegmentRequest createSegmentRequest = {"description":"US users","rolloutPercent":100}; // CreateSegmentRequest | 

try {
    final response = api.createSegment(flagId, createSegmentRequest);
    print(response);
} on DioException catch (e) {
    print('Exception when calling SegmentApi->createSegment: $e\n');
}
```

### Parameters

Name | Type | Description  | Notes
------------- | ------------- | ------------- | -------------
 **flagId** | **int**| Numeric ID of the flag | 
 **createSegmentRequest** | [**CreateSegmentRequest**](CreateSegmentRequest.md)|  | 

### Return type

[**Segment**](Segment.md)

### Authorization

No authorization required

### HTTP request headers

 - **Content-Type**: application/json
 - **Accept**: application/json

[[Back to top]](#) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to Model list]](../README.md#documentation-for-models) [[Back to README]](../README.md)

# **deleteSegment**
> deleteSegment(flagId, segmentId)

Delete segment

Delete a segment. This will also delete all constraints and distributions associated with the segment.

### Example
```dart
import 'package:flagent_client/api.dart';

final api = FlagentClient().getSegmentApi();
final int flagId = 789; // int | Numeric ID of the flag
final int segmentId = 789; // int | Numeric ID of the segment

try {
    api.deleteSegment(flagId, segmentId);
} on DioException catch (e) {
    print('Exception when calling SegmentApi->deleteSegment: $e\n');
}
```

### Parameters

Name | Type | Description  | Notes
------------- | ------------- | ------------- | -------------
 **flagId** | **int**| Numeric ID of the flag | 
 **segmentId** | **int**| Numeric ID of the segment | 

### Return type

void (empty response body)

### Authorization

No authorization required

### HTTP request headers

 - **Content-Type**: Not defined
 - **Accept**: application/json

[[Back to top]](#) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to Model list]](../README.md#documentation-for-models) [[Back to README]](../README.md)

# **findSegments**
> BuiltList<Segment> findSegments(flagId)

Get segments for flag

### Example
```dart
import 'package:flagent_client/api.dart';

final api = FlagentClient().getSegmentApi();
final int flagId = 789; // int | Numeric ID of the flag

try {
    final response = api.findSegments(flagId);
    print(response);
} on DioException catch (e) {
    print('Exception when calling SegmentApi->findSegments: $e\n');
}
```

### Parameters

Name | Type | Description  | Notes
------------- | ------------- | ------------- | -------------
 **flagId** | **int**| Numeric ID of the flag | 

### Return type

[**BuiltList&lt;Segment&gt;**](Segment.md)

### Authorization

No authorization required

### HTTP request headers

 - **Content-Type**: Not defined
 - **Accept**: application/json

[[Back to top]](#) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to Model list]](../README.md#documentation-for-models) [[Back to README]](../README.md)

# **putSegment**
> Segment putSegment(flagId, segmentId, putSegmentRequest)

Update segment

### Example
```dart
import 'package:flagent_client/api.dart';

final api = FlagentClient().getSegmentApi();
final int flagId = 789; // int | Numeric ID of the flag
final int segmentId = 789; // int | Numeric ID of the segment
final PutSegmentRequest putSegmentRequest = {"description":"US users - updated","rolloutPercent":50}; // PutSegmentRequest | 

try {
    final response = api.putSegment(flagId, segmentId, putSegmentRequest);
    print(response);
} on DioException catch (e) {
    print('Exception when calling SegmentApi->putSegment: $e\n');
}
```

### Parameters

Name | Type | Description  | Notes
------------- | ------------- | ------------- | -------------
 **flagId** | **int**| Numeric ID of the flag | 
 **segmentId** | **int**| Numeric ID of the segment | 
 **putSegmentRequest** | [**PutSegmentRequest**](PutSegmentRequest.md)|  | 

### Return type

[**Segment**](Segment.md)

### Authorization

No authorization required

### HTTP request headers

 - **Content-Type**: application/json
 - **Accept**: application/json

[[Back to top]](#) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to Model list]](../README.md#documentation-for-models) [[Back to README]](../README.md)

# **putSegmentReorder**
> putSegmentReorder(flagId, putSegmentReorderRequest)

Reorder segments

### Example
```dart
import 'package:flagent_client/api.dart';

final api = FlagentClient().getSegmentApi();
final int flagId = 789; // int | Numeric ID of the flag
final PutSegmentReorderRequest putSegmentReorderRequest = {"segmentIDs":[2,1,3]}; // PutSegmentReorderRequest | 

try {
    api.putSegmentReorder(flagId, putSegmentReorderRequest);
} on DioException catch (e) {
    print('Exception when calling SegmentApi->putSegmentReorder: $e\n');
}
```

### Parameters

Name | Type | Description  | Notes
------------- | ------------- | ------------- | -------------
 **flagId** | **int**| Numeric ID of the flag | 
 **putSegmentReorderRequest** | [**PutSegmentReorderRequest**](PutSegmentReorderRequest.md)|  | 

### Return type

void (empty response body)

### Authorization

No authorization required

### HTTP request headers

 - **Content-Type**: application/json
 - **Accept**: application/json

[[Back to top]](#) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to Model list]](../README.md#documentation-for-models) [[Back to README]](../README.md)

