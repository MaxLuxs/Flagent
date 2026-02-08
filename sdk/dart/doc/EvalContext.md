# flagent_client.model.EvalContext

## Load the model package
```dart
import 'package:flagent_client/api.dart';
```

## Properties
Name | Type | Description | Notes
------------ | ------------- | ------------- | -------------
**entityID** | **String** | EntityID is used to deterministically at random to evaluate the flag result. If it's empty, Flagent will randomly generate one. | [optional] 
**entityType** | **String** |  | [optional] 
**entityContext** | [**BuiltMap&lt;String, JsonObject&gt;**](JsonObject.md) |  | [optional] 
**enableDebug** | **bool** |  | [optional] [default to false]
**flagID** | **int** | FlagID. flagID or flagKey will resolve to the same flag. Either works. | [optional] 
**flagKey** | **String** | FlagKey. flagID or flagKey will resolve to the same flag. Either works. | [optional] 
**flagTags** | **BuiltList&lt;String&gt;** | FlagTags. flagTags looks up flags by tag. Either works. | [optional] 
**flagTagsOperator** | **String** | Determine how flagTags is used to filter flags to be evaluated. OR extends the evaluation to those which contains at least one of the provided flagTags or AND limit the evaluation to those which contains all the flagTags. | [optional] [default to 'ANY']

[[Back to Model list]](../README.md#documentation-for-models) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to README]](../README.md)


