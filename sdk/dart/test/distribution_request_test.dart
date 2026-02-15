import 'package:test/test.dart';
import 'package:flagent_client/flagent_client.dart';

// tests for DistributionRequest
void main() {
  final instance = DistributionRequest((b) => b
    ..variantID = 1
    ..variantKey = 'control'
    ..percent = 50);

  group(DistributionRequest, () {
    test('to test the property `variantID`', () async {
      expect(instance.variantID, equals(1));
    });

    test('to test the property `variantKey`', () async {
      expect(instance.variantKey, equals('control'));
    });

    test('to test the property `percent`', () async {
      expect(instance.percent, equals(50));
    });
  });
}
