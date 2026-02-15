import 'package:test/test.dart';
import 'package:flagent_client/flagent_client.dart';

import 'mock_dio_helper.dart';

/// tests for VariantApi
void main() {
  final client = createMockFlagentClient();
  final instance = client.getVariantApi();

  group(VariantApi, () {
    test('test createVariant', () async {
      final request = CreateVariantRequest((b) => b..key = 'variant_a');
      final response = await instance.createVariant(
        flagId: 1,
        createVariantRequest: request,
      );
      expect(response.data, isNotNull);
      expect(response.data!.id, equals(1));
      expect(response.data!.key, equals('control'));
      expect(response.statusCode, equals(200));
    });

    test('test deleteVariant', () async {
      final response = await instance.deleteVariant(flagId: 1, variantId: 1);
      expect(response.statusCode, equals(200));
    });

    test('test findVariants', () async {
      final response = await instance.findVariants(flagId: 1);
      expect(response.data, isNotNull);
      expect(response.data!, isEmpty);
      expect(response.statusCode, equals(200));
    });

    test('test putVariant', () async {
      final request = PutVariantRequest((b) => b..key = 'updated_variant');
      final response = await instance.putVariant(
        flagId: 1,
        variantId: 1,
        putVariantRequest: request,
      );
      expect(response.data, isNotNull);
      expect(response.data!.key, equals('control'));
      expect(response.statusCode, equals(200));
    });
  });
}
