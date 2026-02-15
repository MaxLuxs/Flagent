import 'package:test/test.dart';
import 'package:flagent_client/flagent_client.dart';

import 'mock_dio_helper.dart';

/// tests for FlagApi
void main() {
  final client = createMockFlagentClient();
  final instance = client.getFlagApi();

  group(FlagApi, () {
    test('test createFlag', () async {
      final request = CreateFlagRequest((b) => b
        ..description = 'New flag'
        ..key = 'new_flag');
      final response = await instance.createFlag(createFlagRequest: request);
      expect(response.data, isNotNull);
      expect(response.data!.key, equals('new_flag'));
      expect(response.data!.id, equals(1));
      expect(response.statusCode, equals(200));
    });

    test('test deleteFlag', () async {
      final response = await instance.deleteFlag(flagId: 1);
      expect(response.statusCode, equals(200));
    });

    test('test findFlags', () async {
      final response = await instance.findFlags();
      expect(response.data, isNotNull);
      expect(response.data!, isEmpty);
      expect(response.statusCode, equals(200));
    });

    test('test getFlag', () async {
      final response = await instance.getFlag(flagId: 1);
      expect(response.data, isNotNull);
      expect(response.data!.id, equals(1));
      expect(response.data!.key, equals('test_flag'));
      expect(response.statusCode, equals(200));
    });

    test('test getFlagEntityTypes', () async {
      final response = await instance.getFlagEntityTypes();
      expect(response.data, isNotNull);
      expect(response.data!.length, equals(2));
      expect(response.data!.contains('user'), isTrue);
      expect(response.statusCode, equals(200));
    });

    test('test getFlagSnapshots', () async {
      final response = await instance.getFlagSnapshots(flagId: 1);
      expect(response.data, isNotNull);
      expect(response.data!, isEmpty);
      expect(response.statusCode, equals(200));
    });

    test('test putFlag', () async {
      final request = PutFlagRequest((b) => b
        ..description = 'Updated'
        ..key = 'updated_key');
      final response = await instance.putFlag(flagId: 1, putFlagRequest: request);
      expect(response.data, isNotNull);
      expect(response.statusCode, equals(200));
    });

    test('test restoreFlag', () async {
      final response = await instance.restoreFlag(flagId: 1);
      expect(response.statusCode, equals(200));
    });

    test('test setFlagEnabled', () async {
      final request = SetFlagEnabledRequest((b) => b..enabled = true);
      final response = await instance.setFlagEnabled(
        flagId: 1,
        setFlagEnabledRequest: request,
      );
      expect(response.statusCode, equals(200));
    });
  });
}
