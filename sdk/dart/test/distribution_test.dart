import 'package:test/test.dart';
import 'package:flagent_client/flagent_client.dart';

// tests for Distribution
void main() {
  final instance = Distribution((b) => b
    ..id = 1
    ..segmentID = 10
    ..variantID = 20
    ..variantKey = 'control'
    ..percent = 50);

  group(Distribution, () {
    test('to test the property `id`', () async {
      expect(instance.id, equals(1));
    });

    test('to test the property `segmentID`', () async {
      expect(instance.segmentID, equals(10));
    });

    test('to test the property `variantID`', () async {
      expect(instance.variantID, equals(20));
    });

    test('to test the property `variantKey`', () async {
      expect(instance.variantKey, equals('control'));
    });

    test('to test the property `percent`', () async {
      expect(instance.percent, equals(50));
    });
  });
}
