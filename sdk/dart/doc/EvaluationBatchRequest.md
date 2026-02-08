# flagent_client.model.EvaluationBatchRequest

## Load the model package
```dart
import 'package:flagent_client/api.dart';
```

## Properties
Name | Type | Description | Notes
------------ | ------------- | ------------- | -------------
**entities** | [**BuiltList&lt;EvaluationEntity&gt;**](EvaluationEntity.md) |  | 
**enableDebug** | **bool** |  | [optional] [default to false]
**flagIDs** | **BuiltList&lt;int&gt;** | FlagIDs. Either flagIDs, flagKeys or flagTags works. If pass in multiples, Flagent may return duplicate results. | [optional] 
**flagKeys** | **BuiltList&lt;String&gt;** | FlagKeys. Either flagIDs, flagKeys or flagTags works. If pass in multiples, Flagent may return duplicate results. | [optional] 
**flagTags** | **BuiltList&lt;String&gt;** | FlagTags. Either flagIDs, flagKeys or flagTags works. If pass in multiples, Flagent may return duplicate results. | [optional] 
**flagTagsOperator** | **String** | Determine how flagTags is used to filter flags to be evaluated. | [optional] [default to 'ANY']

[[Back to Model list]](../README.md#documentation-for-models) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to README]](../README.md)


