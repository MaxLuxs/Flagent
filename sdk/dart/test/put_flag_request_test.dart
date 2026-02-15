import 'package:test/test.dart';
import 'package:flagent_client/flagent_client.dart';

// tests for PutFlagRequest
void main() {
  final instance = PutFlagRequest((b) => b
    ..description = 'Updated description'
    ..key = 'updated_key'
    ..dataRecordsEnabled = true
    ..entityType = 'device'
    ..notes = 'Updated notes');

  group(PutFlagRequest, () {
    test('to test the property `description`', () async {
      expect(instance.description, equals('Updated description'));
    });

    test('to test the property `key`', () async {
      expect(instance.key, equals('updated_key'));
    });

    test('to test the property `dataRecordsEnabled`', () async {
      expect(instance.dataRecordsEnabled, equals(true));
    });

    test('to test the property `entityType`', () async {
      expect(instance.entityType, equals('device'));
    });

    test('to test the property `notes`', () async {
      expect(instance.notes, equals('Updated notes'));
    });
  });
}
