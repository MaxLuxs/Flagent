# flagent_client.api.ConstraintApi

## Load the API package
```dart
import 'package:flagent_client/api.dart';
```

All URIs are relative to *http://localhost:18000/api/v1*

Method | HTTP request | Description
------------- | ------------- | -------------
[**createConstraint**](ConstraintApi.md#createconstraint) | **POST** /flags/{flagId}/segments/{segmentId}/constraints | Create constraint
[**deleteConstraint**](ConstraintApi.md#deleteconstraint) | **DELETE** /flags/{flagId}/segments/{segmentId}/constraints/{constraintId} | Delete constraint
[**findConstraints**](ConstraintApi.md#findconstraints) | **GET** /flags/{flagId}/segments/{segmentId}/constraints | Get constraints for segment
[**putConstraint**](ConstraintApi.md#putconstraint) | **PUT** /flags/{flagId}/segments/{segmentId}/constraints/{constraintId} | Update constraint


# **createConstraint**
> Constraint createConstraint(flagId, segmentId, createConstraintRequest)

Create constraint

Create a constraint for the segment. Constraints define conditions that must be met for a segment to match.

### Example
```dart
import 'package:flagent_client/api.dart';

final api = FlagentClient().getConstraintApi();
final int flagId = 789; // int | Numeric ID of the flag
final int segmentId = 789; // int | Numeric ID of the segment
final CreateConstraintRequest createConstraintRequest = {"property":"region","operator":"EQ","value":"US"}; // CreateConstraintRequest | 

try {
    final response = api.createConstraint(flagId, segmentId, createConstraintRequest);
    print(response);
} on DioException catch (e) {
    print('Exception when calling ConstraintApi->createConstraint: $e\n');
}
```

### Parameters

Name | Type | Description  | Notes
------------- | ------------- | ------------- | -------------
 **flagId** | **int**| Numeric ID of the flag | 
 **segmentId** | **int**| Numeric ID of the segment | 
 **createConstraintRequest** | [**CreateConstraintRequest**](CreateConstraintRequest.md)|  | 

### Return type

[**Constraint**](Constraint.md)

### Authorization

No authorization required

### HTTP request headers

 - **Content-Type**: application/json
 - **Accept**: application/json

[[Back to top]](#) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to Model list]](../README.md#documentation-for-models) [[Back to README]](../README.md)

# **deleteConstraint**
> deleteConstraint(flagId, segmentId, constraintId)

Delete constraint

Delete a constraint from the segment.

### Example
```dart
import 'package:flagent_client/api.dart';

final api = FlagentClient().getConstraintApi();
final int flagId = 789; // int | Numeric ID of the flag
final int segmentId = 789; // int | Numeric ID of the segment
final int constraintId = 789; // int | Numeric ID of the constraint

try {
    api.deleteConstraint(flagId, segmentId, constraintId);
} on DioException catch (e) {
    print('Exception when calling ConstraintApi->deleteConstraint: $e\n');
}
```

### Parameters

Name | Type | Description  | Notes
------------- | ------------- | ------------- | -------------
 **flagId** | **int**| Numeric ID of the flag | 
 **segmentId** | **int**| Numeric ID of the segment | 
 **constraintId** | **int**| Numeric ID of the constraint | 

### Return type

void (empty response body)

### Authorization

No authorization required

### HTTP request headers

 - **Content-Type**: Not defined
 - **Accept**: application/json

[[Back to top]](#) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to Model list]](../README.md#documentation-for-models) [[Back to README]](../README.md)

# **findConstraints**
> BuiltList<Constraint> findConstraints(flagId, segmentId)

Get constraints for segment

### Example
```dart
import 'package:flagent_client/api.dart';

final api = FlagentClient().getConstraintApi();
final int flagId = 789; // int | Numeric ID of the flag
final int segmentId = 789; // int | Numeric ID of the segment

try {
    final response = api.findConstraints(flagId, segmentId);
    print(response);
} on DioException catch (e) {
    print('Exception when calling ConstraintApi->findConstraints: $e\n');
}
```

### Parameters

Name | Type | Description  | Notes
------------- | ------------- | ------------- | -------------
 **flagId** | **int**| Numeric ID of the flag | 
 **segmentId** | **int**| Numeric ID of the segment | 

### Return type

[**BuiltList&lt;Constraint&gt;**](Constraint.md)

### Authorization

No authorization required

### HTTP request headers

 - **Content-Type**: Not defined
 - **Accept**: application/json

[[Back to top]](#) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to Model list]](../README.md#documentation-for-models) [[Back to README]](../README.md)

# **putConstraint**
> Constraint putConstraint(flagId, segmentId, constraintId, putConstraintRequest)

Update constraint

### Example
```dart
import 'package:flagent_client/api.dart';

final api = FlagentClient().getConstraintApi();
final int flagId = 789; // int | Numeric ID of the flag
final int segmentId = 789; // int | Numeric ID of the segment
final int constraintId = 789; // int | Numeric ID of the constraint
final PutConstraintRequest putConstraintRequest = {"property":"region","operator":"IN","value":"US,CA,MX"}; // PutConstraintRequest | 

try {
    final response = api.putConstraint(flagId, segmentId, constraintId, putConstraintRequest);
    print(response);
} on DioException catch (e) {
    print('Exception when calling ConstraintApi->putConstraint: $e\n');
}
```

### Parameters

Name | Type | Description  | Notes
------------- | ------------- | ------------- | -------------
 **flagId** | **int**| Numeric ID of the flag | 
 **segmentId** | **int**| Numeric ID of the segment | 
 **constraintId** | **int**| Numeric ID of the constraint | 
 **putConstraintRequest** | [**PutConstraintRequest**](PutConstraintRequest.md)|  | 

### Return type

[**Constraint**](Constraint.md)

### Authorization

No authorization required

### HTTP request headers

 - **Content-Type**: application/json
 - **Accept**: application/json

[[Back to top]](#) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to Model list]](../README.md#documentation-for-models) [[Back to README]](../README.md)

