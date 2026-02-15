import 'package:test/test.dart';
import 'package:flagent_client/flagent_client.dart';

// tests for CreateFlagRequest
void main() {
  final instance = CreateFlagRequest((b) => b
    ..description = 'New flag'
    ..key = 'new_feature'
    ..template = 'default');

  group(CreateFlagRequest, () {
    test('to test the property `description`', () async {
      expect(instance.description, equals('New flag'));
    });

    test('to test the property `key`', () async {
      expect(instance.key, equals('new_feature'));
    });

    test('to test the property `template`', () async {
      expect(instance.template, equals('default'));
    });
  });
}
