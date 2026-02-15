import 'package:built_collection/built_collection.dart';
import 'package:built_value/json_object.dart';
import 'package:test/test.dart';
import 'package:flagent_client/flagent_client.dart';

// tests for EvalContext
void main() {
  final instance = EvalContext((b) => b
    ..entityID = 'entity-123'
    ..entityType = 'user'
    ..entityContext = MapBuilder<String, JsonObject?>({
      'tier': JsonObject('premium'),
    })
    ..enableDebug = true
    ..flagID = 1
    ..flagKey = 'my_flag'
    ..flagTags = ListBuilder<String>(['experiment'])
    ..flagTagsOperator = EvalContextFlagTagsOperatorEnum.valueOf('ALL'));

  group(EvalContext, () {
    test('to test the property `entityID`', () async {
      expect(instance.entityID, equals('entity-123'));
    });

    test('to test the property `entityType`', () async {
      expect(instance.entityType, equals('user'));
    });

    test('to test the property `entityContext`', () async {
      expect(instance.entityContext, isNotNull);
      expect(instance.entityContext!.length, equals(1));
    });

    test('to test the property `enableDebug`', () async {
      expect(instance.enableDebug, equals(true));
    });

    test('to test the property `flagID`', () async {
      expect(instance.flagID, equals(1));
    });

    test('to test the property `flagKey`', () async {
      expect(instance.flagKey, equals('my_flag'));
    });

    test('to test the property `flagTags`', () async {
      expect(instance.flagTags, isNotNull);
      expect(instance.flagTags!.length, equals(1));
      expect(instance.flagTags![0], equals('experiment'));
    });

    test('to test the property `flagTagsOperator`', () async {
      expect(
        instance.flagTagsOperator,
        equals(EvalContextFlagTagsOperatorEnum.valueOf('ALL')),
      );
    });
  });
}
