import 'package:test/test.dart';
import 'package:flagent_client/flagent_client.dart';

// tests for FlagSnapshot
void main() {
  final flag = Flag((b) => b
    ..id = 1
    ..key = 'snapshot_flag'
    ..description = 'Desc'
    ..enabled = true
    ..dataRecordsEnabled = false);
  final instance = FlagSnapshot((b) => b
    ..id = 100
    ..updatedBy = 'admin'
    ..flag = flag.toBuilder()
    ..updatedAt = DateTime.utc(2024, 1, 1, 12, 0, 0));

  group(FlagSnapshot, () {
    test('to test the property `id`', () async {
      expect(instance.id, equals(100));
    });

    test('to test the property `updatedBy`', () async {
      expect(instance.updatedBy, equals('admin'));
    });

    test('to test the property `flag`', () async {
      expect(instance.flag.key, equals('snapshot_flag'));
      expect(instance.flag.id, equals(1));
    });

    test('to test the property `updatedAt`', () async {
      expect(instance.updatedAt, equals(DateTime.utc(2024, 1, 1, 12, 0, 0)));
    });
  });
}
