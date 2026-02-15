import 'package:test/test.dart';
import 'package:flagent_client/flagent_client.dart';

// tests for SetFlagEnabledRequest
void main() {
  final instance = SetFlagEnabledRequest((b) => b..enabled = true);

  group(SetFlagEnabledRequest, () {
    test('to test the property `enabled`', () async {
      expect(instance.enabled, equals(true));
    });
  });
}
