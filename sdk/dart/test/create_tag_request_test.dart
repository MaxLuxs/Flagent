import 'package:test/test.dart';
import 'package:flagent_client/flagent_client.dart';

// tests for CreateTagRequest
void main() {
  final instance = CreateTagRequest((b) => b..value = 'production');

  group(CreateTagRequest, () {
    test('to test the property `value`', () async {
      expect(instance.value, equals('production'));
    });
  });
}
