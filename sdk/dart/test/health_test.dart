import 'package:test/test.dart';
import 'package:flagent_client/flagent_client.dart';

// tests for Health
void main() {
  final instance = Health((b) => b..status = 'ok');

  group(Health, () {
    test('to test the property `status`', () async {
      expect(instance.status, equals('ok'));
    });
  });
}
