# flagent_client.api.EvaluationApi

## Load the API package
```dart
import 'package:flagent_client/api.dart';
```

All URIs are relative to *http://localhost:18000/api/v1*

Method | HTTP request | Description
------------- | ------------- | -------------
[**postEvaluation**](EvaluationApi.md#postevaluation) | **POST** /evaluation | Evaluate flag
[**postEvaluationBatch**](EvaluationApi.md#postevaluationbatch) | **POST** /evaluation/batch | Batch evaluate flags


# **postEvaluation**
> EvalResult postEvaluation(evalContext)

Evaluate flag

### Example
```dart
import 'package:flagent_client/api.dart';

final api = FlagentClient().getEvaluationApi();
final EvalContext evalContext = {"flagID":1,"entityID":"user123","entityType":"user","entityContext":{"region":"US","tier":"premium"},"enableDebug":false}; // EvalContext | 

try {
    final response = api.postEvaluation(evalContext);
    print(response);
} on DioException catch (e) {
    print('Exception when calling EvaluationApi->postEvaluation: $e\n');
}
```

### Parameters

Name | Type | Description  | Notes
------------- | ------------- | ------------- | -------------
 **evalContext** | [**EvalContext**](EvalContext.md)|  | 

### Return type

[**EvalResult**](EvalResult.md)

### Authorization

No authorization required

### HTTP request headers

 - **Content-Type**: application/json
 - **Accept**: application/json

[[Back to top]](#) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to Model list]](../README.md#documentation-for-models) [[Back to README]](../README.md)

# **postEvaluationBatch**
> EvaluationBatchResponse postEvaluationBatch(evaluationBatchRequest)

Batch evaluate flags

Evaluate multiple flags for multiple entities in a single request. More efficient than multiple single evaluation requests.

### Example
```dart
import 'package:flagent_client/api.dart';

final api = FlagentClient().getEvaluationApi();
final EvaluationBatchRequest evaluationBatchRequest = {"entities":[{"entityID":"user123","entityType":"user","entityContext":{"region":"US","tier":"premium"}},{"entityID":"user456","entityType":"user","entityContext":{"region":"EU","tier":"basic"}}],"flagIDs":[1,2],"enableDebug":false}; // EvaluationBatchRequest | 

try {
    final response = api.postEvaluationBatch(evaluationBatchRequest);
    print(response);
} on DioException catch (e) {
    print('Exception when calling EvaluationApi->postEvaluationBatch: $e\n');
}
```

### Parameters

Name | Type | Description  | Notes
------------- | ------------- | ------------- | -------------
 **evaluationBatchRequest** | [**EvaluationBatchRequest**](EvaluationBatchRequest.md)|  | 

### Return type

[**EvaluationBatchResponse**](EvaluationBatchResponse.md)

### Authorization

No authorization required

### HTTP request headers

 - **Content-Type**: application/json
 - **Accept**: application/json

[[Back to top]](#) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to Model list]](../README.md#documentation-for-models) [[Back to README]](../README.md)

