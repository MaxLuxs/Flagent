import 'package:test/test.dart';
import 'package:flagent_client/flagent_client.dart';

// tests for PutConstraintRequest
void main() {
  final instance = PutConstraintRequest((b) => b
    ..property = 'region'
    ..operator_ = 'IN'
    ..value = 'EU,US');

  group(PutConstraintRequest, () {
    test('to test the property `property`', () async {
      expect(instance.property, equals('region'));
    });

    test('to test the property `operator_`', () async {
      expect(instance.operator_, equals('IN'));
    });

    test('to test the property `value`', () async {
      expect(instance.value, equals('EU,US'));
    });
  });
}
