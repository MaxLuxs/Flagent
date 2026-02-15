import 'package:test/test.dart';
import 'package:flagent_client/flagent_client.dart';

// tests for Info
void main() {
  final instance = Info((b) => b
    ..version = '1.0.0'
    ..buildTime = '2024-01-01T00:00:00Z'
    ..gitCommit = 'abc123');

  group(Info, () {
    test('to test the property `version`', () async {
      expect(instance.version, equals('1.0.0'));
    });

    test('to test the property `buildTime`', () async {
      expect(instance.buildTime, equals('2024-01-01T00:00:00Z'));
    });

    test('to test the property `gitCommit`', () async {
      expect(instance.gitCommit, equals('abc123'));
    });
  });
}
