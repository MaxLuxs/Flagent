import 'package:built_collection/built_collection.dart';
import 'package:test/test.dart';
import 'package:flagent_client/flagent_client.dart';

// tests for Flag
void main() {
  final instance = Flag((b) => b
    ..id = 1
    ..key = 'test_flag'
    ..description = 'Test description'
    ..enabled = true
    ..snapshotID = 10
    ..dataRecordsEnabled = false
    ..entityType = 'user'
    ..notes = 'Usage notes'
    ..createdBy = 'admin'
    ..updatedBy = 'admin'
    ..segments = ListBuilder<Segment>()
    ..variants = ListBuilder<Variant>()
    ..tags = ListBuilder<Tag>());

  group(Flag, () {
    test('to test the property `id`', () async {
      expect(instance.id, equals(1));
    });

    test('to test the property `key`', () async {
      expect(instance.key, equals('test_flag'));
    });

    test('to test the property `description`', () async {
      expect(instance.description, equals('Test description'));
    });

    test('to test the property `enabled`', () async {
      expect(instance.enabled, equals(true));
    });

    test('to test the property `snapshotID`', () async {
      expect(instance.snapshotID, equals(10));
    });

    test('to test the property `dataRecordsEnabled`', () async {
      expect(instance.dataRecordsEnabled, equals(false));
    });

    test('to test the property `entityType`', () async {
      expect(instance.entityType, equals('user'));
    });

    test('to test the property `notes`', () async {
      expect(instance.notes, equals('Usage notes'));
    });

    test('to test the property `createdBy`', () async {
      expect(instance.createdBy, equals('admin'));
    });

    test('to test the property `updatedBy`', () async {
      expect(instance.updatedBy, equals('admin'));
    });

    test('to test the property `segments`', () async {
      expect(instance.segments, isNotNull);
      expect(instance.segments!.length, equals(0));
    });

    test('to test the property `variants`', () async {
      expect(instance.variants, isNotNull);
      expect(instance.variants!.length, equals(0));
    });

    test('to test the property `tags`', () async {
      expect(instance.tags, isNotNull);
      expect(instance.tags!.length, equals(0));
    });
  });
}
