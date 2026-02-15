import 'package:test/test.dart';
import 'package:flagent_client/flagent_client.dart';

// tests for CreateConstraintRequest
void main() {
  final instance = CreateConstraintRequest((b) => b
    ..property = 'tier'
    ..operator_ = 'EQ'
    ..value = 'premium');

  group(CreateConstraintRequest, () {
    test('to test the property `property`', () async {
      expect(instance.property, equals('tier'));
    });

    test('to test the property `operator_`', () async {
      expect(instance.operator_, equals('EQ'));
    });

    test('to test the property `value`', () async {
      expect(instance.value, equals('premium'));
    });
  });
}
