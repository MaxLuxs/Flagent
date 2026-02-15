import 'package:test/test.dart';
import 'package:flagent_client/flagent_client.dart';

// tests for PutSegmentRequest
void main() {
  final instance = PutSegmentRequest((b) => b
    ..description = 'Updated segment'
    ..rolloutPercent = 75);

  group(PutSegmentRequest, () {
    test('to test the property `description`', () async {
      expect(instance.description, equals('Updated segment'));
    });

    test('to test the property `rolloutPercent`', () async {
      expect(instance.rolloutPercent, equals(75));
    });
  });
}
