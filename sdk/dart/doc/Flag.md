# flagent_client.model.Flag

## Load the model package
```dart
import 'package:flagent_client/api.dart';
```

## Properties
Name | Type | Description | Notes
------------ | ------------- | ------------- | -------------
**id** | **int** |  | 
**key** | **String** | Unique key representation of the flag | 
**description** | **String** |  | 
**enabled** | **bool** |  | 
**snapshotID** | **int** |  | [optional] 
**dataRecordsEnabled** | **bool** | Enabled data records will get data logging in the metrics pipeline | 
**entityType** | **String** | It will override the entityType in the evaluation logs if it's not empty | [optional] 
**notes** | **String** | Flag usage details in markdown format | [optional] 
**createdBy** | **String** |  | [optional] 
**updatedBy** | **String** |  | [optional] 
**segments** | [**BuiltList&lt;Segment&gt;**](Segment.md) |  | [optional] 
**variants** | [**BuiltList&lt;Variant&gt;**](Variant.md) |  | [optional] 
**tags** | [**BuiltList&lt;Tag&gt;**](Tag.md) |  | [optional] 

[[Back to Model list]](../README.md#documentation-for-models) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to README]](../README.md)


