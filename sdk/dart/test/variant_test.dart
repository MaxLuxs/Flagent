import 'package:built_collection/built_collection.dart';
import 'package:built_value/json_object.dart';
import 'package:test/test.dart';
import 'package:flagent_client/flagent_client.dart';

// tests for Variant
void main() {
  final instance = Variant((b) => b
    ..id = 1
    ..flagID = 10
    ..key = 'control'
    ..attachment = MapBuilder<String, JsonObject?>({
      'key': JsonObject({'nested': 'value'}),
    }));

  group(Variant, () {
    test('to test the property `id`', () async {
      expect(instance.id, equals(1));
    });

    test('to test the property `flagID`', () async {
      expect(instance.flagID, equals(10));
    });

    test('to test the property `key`', () async {
      expect(instance.key, equals('control'));
    });

    test('to test the property `attachment`', () async {
      expect(instance.attachment, isNotNull);
      expect(instance.attachment!.length, equals(1));
    });
  });
}
