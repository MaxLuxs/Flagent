import 'package:test/test.dart';
import 'package:flagent_client/flagent_client.dart';


/// tests for VariantApi
void main() {
  final instance = FlagentClient().getVariantApi();

  group(VariantApi, () {
    // Create variant
    //
    // Create a variant for the flag. Variants are the possible outcomes of flag evaluation.
    //
    //Future<Variant> createVariant(int flagId, CreateVariantRequest createVariantRequest) async
    test('test createVariant', () async {
      // TODO
    });

    // Delete variant
    //
    // Delete a variant. This will also remove it from all distributions.
    //
    //Future deleteVariant(int flagId, int variantId) async
    test('test deleteVariant', () async {
      // TODO
    });

    // Get variants for flag
    //
    //Future<BuiltList<Variant>> findVariants(int flagId) async
    test('test findVariants', () async {
      // TODO
    });

    // Update variant
    //
    //Future<Variant> putVariant(int flagId, int variantId, PutVariantRequest putVariantRequest) async
    test('test putVariant', () async {
      // TODO
    });

  });
}
