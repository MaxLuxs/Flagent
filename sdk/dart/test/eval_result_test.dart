import 'package:built_collection/built_collection.dart';
import 'package:built_value/json_object.dart';
import 'package:test/test.dart';
import 'package:flagent_client/flagent_client.dart';

// tests for EvalResult
void main() {
  final evalCtx = EvalContext((b) => b
    ..entityID = 'user-1'
    ..entityType = 'user'
    ..flagKey = 'test_flag');
  final instance = EvalResult((b) => b
    ..flagID = 1
    ..flagKey = 'test_flag'
    ..flagSnapshotID = 10
    ..flagTags = ListBuilder<String>(['tag1'])
    ..segmentID = 5
    ..variantID = 2
    ..variantKey = 'control'
    ..variantAttachment = MapBuilder<String, JsonObject?>()
    ..evalContext = evalCtx.toBuilder()
    ..timestamp = DateTime.utc(2024, 1, 1, 12, 0, 0)
    ..evalDebugLog = null);

  group(EvalResult, () {
    test('to test the property `flagID`', () async {
      expect(instance.flagID, equals(1));
    });

    test('to test the property `flagKey`', () async {
      expect(instance.flagKey, equals('test_flag'));
    });

    test('to test the property `flagSnapshotID`', () async {
      expect(instance.flagSnapshotID, equals(10));
    });

    test('to test the property `flagTags`', () async {
      expect(instance.flagTags, isNotNull);
      expect(instance.flagTags!.length, equals(1));
      expect(instance.flagTags![0], equals('tag1'));
    });

    test('to test the property `segmentID`', () async {
      expect(instance.segmentID, equals(5));
    });

    test('to test the property `variantID`', () async {
      expect(instance.variantID, equals(2));
    });

    test('to test the property `variantKey`', () async {
      expect(instance.variantKey, equals('control'));
    });

    test('to test the property `variantAttachment`', () async {
      expect(instance.variantAttachment, isNotNull);
      expect(instance.variantAttachment!.isEmpty, isTrue);
    });

    test('to test the property `evalContext`', () async {
      expect(instance.evalContext, isNotNull);
      expect(instance.evalContext!.entityID, equals('user-1'));
      expect(instance.evalContext!.flagKey, equals('test_flag'));
    });

    test('to test the property `timestamp`', () async {
      expect(instance.timestamp, equals(DateTime.utc(2024, 1, 1, 12, 0, 0)));
    });

    test('to test the property `evalDebugLog`', () async {
      expect(instance.evalDebugLog, isNull);
    });
  });
}
