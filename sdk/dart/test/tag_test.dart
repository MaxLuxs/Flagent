import 'package:test/test.dart';
import 'package:flagent_client/flagent_client.dart';

// tests for Tag
void main() {
  final instance = Tag((b) => b
    ..id = 1
    ..value = 'production');

  group(Tag, () {
    test('to test the property `id`', () async {
      expect(instance.id, equals(1));
    });

    test('to test the property `value`', () async {
      expect(instance.value, equals('production'));
    });
  });
}
