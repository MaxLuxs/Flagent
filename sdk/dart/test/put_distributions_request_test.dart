import 'package:built_collection/built_collection.dart';
import 'package:test/test.dart';
import 'package:flagent_client/flagent_client.dart';

// tests for PutDistributionsRequest
void main() {
  final instance = PutDistributionsRequest((b) => b
    ..distributions = ListBuilder<DistributionRequest>([
      DistributionRequest((d) => d
        ..variantID = 1
        ..variantKey = 'control'
        ..percent = 50),
      DistributionRequest((d) => d
        ..variantID = 2
        ..variantKey = 'treatment'
        ..percent = 50),
    ]));

  group(PutDistributionsRequest, () {
    test('to test the property `distributions`', () async {
      expect(instance.distributions.length, equals(2));
      expect(instance.distributions[0].variantKey, equals('control'));
      expect(instance.distributions[1].variantKey, equals('treatment'));
    });
  });
}
