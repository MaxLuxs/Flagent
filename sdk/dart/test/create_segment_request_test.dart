import 'package:test/test.dart';
import 'package:flagent_client/flagent_client.dart';

// tests for CreateSegmentRequest
void main() {
  final instance = CreateSegmentRequest((b) => b
    ..description = 'Segment desc'
    ..rolloutPercent = 50);

  group(CreateSegmentRequest, () {
    test('to test the property `description`', () async {
      expect(instance.description, equals('Segment desc'));
    });

    test('to test the property `rolloutPercent`', () async {
      expect(instance.rolloutPercent, equals(50));
    });
  });
}
