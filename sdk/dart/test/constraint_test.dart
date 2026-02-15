import 'package:test/test.dart';
import 'package:flagent_client/flagent_client.dart';

// tests for Constraint
void main() {
  final instance = Constraint((b) => b
    ..id = 1
    ..segmentID = 10
    ..property = 'region'
    ..operator_ = ConstraintOperator_Enum.valueOf('EQ')
    ..value = 'EU');

  group(Constraint, () {
    test('to test the property `id`', () async {
      expect(instance.id, equals(1));
    });

    test('to test the property `segmentID`', () async {
      expect(instance.segmentID, equals(10));
    });

    test('to test the property `property`', () async {
      expect(instance.property, equals('region'));
    });

    test('to test the property `operator_`', () async {
      expect(instance.operator_, equals(ConstraintOperator_Enum.valueOf('EQ')));
    });

    test('to test the property `value`', () async {
      expect(instance.value, equals('EU'));
    });
  });
}
