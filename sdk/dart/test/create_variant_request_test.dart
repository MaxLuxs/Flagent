import 'package:built_collection/built_collection.dart';
import 'package:built_value/json_object.dart';
import 'package:test/test.dart';
import 'package:flagent_client/flagent_client.dart';

// tests for CreateVariantRequest
void main() {
  final instance = CreateVariantRequest((b) => b
    ..key = 'variant_a'
    ..attachment = MapBuilder<String, JsonObject?>());

  group(CreateVariantRequest, () {
    test('to test the property `key`', () async {
      expect(instance.key, equals('variant_a'));
    });

    test('to test the property `attachment`', () async {
      expect(instance.attachment, isNotNull);
      expect(instance.attachment!.isEmpty, isTrue);
    });
  });
}
