import 'package:built_collection/built_collection.dart';
import 'package:test/test.dart';
import 'package:flagent_client/flagent_client.dart';

// tests for EvaluationBatchRequest
void main() {
  final instance = EvaluationBatchRequest((b) => b
    ..entities = ListBuilder<EvaluationEntity>([
      EvaluationEntity((e) => e
        ..entityID = 'user-1'
        ..entityType = 'user'),
    ])
    ..enableDebug = false
    ..flagIDs = ListBuilder<int>([1, 2])
    ..flagKeys = ListBuilder<String>(['flag_a'])
    ..flagTags = ListBuilder<String>(['tag1'])
    ..flagTagsOperator = EvaluationBatchRequestFlagTagsOperatorEnum.valueOf('ANY'));

  group(EvaluationBatchRequest, () {
    test('to test the property `entities`', () async {
      expect(instance.entities.length, equals(1));
      expect(instance.entities[0].entityID, equals('user-1'));
    });

    test('to test the property `enableDebug`', () async {
      expect(instance.enableDebug, equals(false));
    });

    test('to test the property `flagIDs`', () async {
      expect(instance.flagIDs, isNotNull);
      expect(instance.flagIDs!.length, equals(2));
      expect(instance.flagIDs![0], equals(1));
    });

    test('to test the property `flagKeys`', () async {
      expect(instance.flagKeys, isNotNull);
      expect(instance.flagKeys!.length, equals(1));
      expect(instance.flagKeys![0], equals('flag_a'));
    });

    test('to test the property `flagTags`', () async {
      expect(instance.flagTags, isNotNull);
      expect(instance.flagTags!.length, equals(1));
      expect(instance.flagTags![0], equals('tag1'));
    });

    test('to test the property `flagTagsOperator`', () async {
      expect(
        instance.flagTagsOperator,
        equals(EvaluationBatchRequestFlagTagsOperatorEnum.valueOf('ANY')),
      );
    });
  });
}
