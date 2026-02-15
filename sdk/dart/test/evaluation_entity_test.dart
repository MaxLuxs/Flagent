import 'package:built_collection/built_collection.dart';
import 'package:built_value/json_object.dart';
import 'package:test/test.dart';
import 'package:flagent_client/flagent_client.dart';

// tests for EvaluationEntity
void main() {
  final instance = EvaluationEntity((b) => b
    ..entityID = 'user-1'
    ..entityType = 'user'
    ..entityContext = MapBuilder<String, JsonObject?>());

  group(EvaluationEntity, () {
    test('to test the property `entityID`', () async {
      expect(instance.entityID, equals('user-1'));
    });

    test('to test the property `entityType`', () async {
      expect(instance.entityType, equals('user'));
    });

    test('to test the property `entityContext`', () async {
      expect(instance.entityContext, isNotNull);
      expect(instance.entityContext!.isEmpty, isTrue);
    });
  });
}
