# flagent_client.api.VariantApi

## Load the API package
```dart
import 'package:flagent_client/api.dart';
```

All URIs are relative to *http://localhost:18000/api/v1*

Method | HTTP request | Description
------------- | ------------- | -------------
[**createVariant**](VariantApi.md#createvariant) | **POST** /flags/{flagId}/variants | Create variant
[**deleteVariant**](VariantApi.md#deletevariant) | **DELETE** /flags/{flagId}/variants/{variantId} | Delete variant
[**findVariants**](VariantApi.md#findvariants) | **GET** /flags/{flagId}/variants | Get variants for flag
[**putVariant**](VariantApi.md#putvariant) | **PUT** /flags/{flagId}/variants/{variantId} | Update variant


# **createVariant**
> Variant createVariant(flagId, createVariantRequest)

Create variant

Create a variant for the flag. Variants are the possible outcomes of flag evaluation.

### Example
```dart
import 'package:flagent_client/api.dart';

final api = FlagentClient().getVariantApi();
final int flagId = 789; // int | Numeric ID of the flag
final CreateVariantRequest createVariantRequest = {"key":"treatment","attachment":{"color":"blue","size":"large"}}; // CreateVariantRequest | 

try {
    final response = api.createVariant(flagId, createVariantRequest);
    print(response);
} on DioException catch (e) {
    print('Exception when calling VariantApi->createVariant: $e\n');
}
```

### Parameters

Name | Type | Description  | Notes
------------- | ------------- | ------------- | -------------
 **flagId** | **int**| Numeric ID of the flag | 
 **createVariantRequest** | [**CreateVariantRequest**](CreateVariantRequest.md)|  | 

### Return type

[**Variant**](Variant.md)

### Authorization

No authorization required

### HTTP request headers

 - **Content-Type**: application/json
 - **Accept**: application/json

[[Back to top]](#) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to Model list]](../README.md#documentation-for-models) [[Back to README]](../README.md)

# **deleteVariant**
> deleteVariant(flagId, variantId)

Delete variant

Delete a variant. This will also remove it from all distributions.

### Example
```dart
import 'package:flagent_client/api.dart';

final api = FlagentClient().getVariantApi();
final int flagId = 789; // int | Numeric ID of the flag
final int variantId = 789; // int | Numeric ID of the variant

try {
    api.deleteVariant(flagId, variantId);
} on DioException catch (e) {
    print('Exception when calling VariantApi->deleteVariant: $e\n');
}
```

### Parameters

Name | Type | Description  | Notes
------------- | ------------- | ------------- | -------------
 **flagId** | **int**| Numeric ID of the flag | 
 **variantId** | **int**| Numeric ID of the variant | 

### Return type

void (empty response body)

### Authorization

No authorization required

### HTTP request headers

 - **Content-Type**: Not defined
 - **Accept**: application/json

[[Back to top]](#) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to Model list]](../README.md#documentation-for-models) [[Back to README]](../README.md)

# **findVariants**
> BuiltList<Variant> findVariants(flagId)

Get variants for flag

### Example
```dart
import 'package:flagent_client/api.dart';

final api = FlagentClient().getVariantApi();
final int flagId = 789; // int | Numeric ID of the flag

try {
    final response = api.findVariants(flagId);
    print(response);
} on DioException catch (e) {
    print('Exception when calling VariantApi->findVariants: $e\n');
}
```

### Parameters

Name | Type | Description  | Notes
------------- | ------------- | ------------- | -------------
 **flagId** | **int**| Numeric ID of the flag | 

### Return type

[**BuiltList&lt;Variant&gt;**](Variant.md)

### Authorization

No authorization required

### HTTP request headers

 - **Content-Type**: Not defined
 - **Accept**: application/json

[[Back to top]](#) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to Model list]](../README.md#documentation-for-models) [[Back to README]](../README.md)

# **putVariant**
> Variant putVariant(flagId, variantId, putVariantRequest)

Update variant

### Example
```dart
import 'package:flagent_client/api.dart';

final api = FlagentClient().getVariantApi();
final int flagId = 789; // int | Numeric ID of the flag
final int variantId = 789; // int | Numeric ID of the variant
final PutVariantRequest putVariantRequest = {"key":"treatment_updated","attachment":{"color":"red","size":"small"}}; // PutVariantRequest | 

try {
    final response = api.putVariant(flagId, variantId, putVariantRequest);
    print(response);
} on DioException catch (e) {
    print('Exception when calling VariantApi->putVariant: $e\n');
}
```

### Parameters

Name | Type | Description  | Notes
------------- | ------------- | ------------- | -------------
 **flagId** | **int**| Numeric ID of the flag | 
 **variantId** | **int**| Numeric ID of the variant | 
 **putVariantRequest** | [**PutVariantRequest**](PutVariantRequest.md)|  | 

### Return type

[**Variant**](Variant.md)

### Authorization

No authorization required

### HTTP request headers

 - **Content-Type**: application/json
 - **Accept**: application/json

[[Back to top]](#) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to Model list]](../README.md#documentation-for-models) [[Back to README]](../README.md)

