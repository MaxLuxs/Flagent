import 'package:built_collection/built_collection.dart';
import 'package:built_value/json_object.dart';
import 'package:test/test.dart';
import 'package:flagent_client/flagent_client.dart';

// tests for PutVariantRequest
void main() {
  final instance = PutVariantRequest((b) => b
    ..key = 'variant_b'
    ..attachment = MapBuilder<String, JsonObject?>());

  group(PutVariantRequest, () {
    test('to test the property `key`', () async {
      expect(instance.key, equals('variant_b'));
    });

    test('to test the property `attachment`', () async {
      expect(instance.attachment, isNotNull);
      expect(instance.attachment!.isEmpty, isTrue);
    });
  });
}
