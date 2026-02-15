import 'package:test/test.dart';
import 'package:flagent_client/flagent_client.dart';

import 'mock_dio_helper.dart';

/// tests for HealthApi
void main() {
  final client = createMockFlagentClient();
  final instance = client.getHealthApi();

  group(HealthApi, () {
    test('test getHealth', () async {
      final response = await instance.getHealth();
      expect(response.data, isNotNull);
      expect(response.data!.status, equals('ok'));
      expect(response.statusCode, equals(200));
    });

    test('test getInfo', () async {
      final response = await instance.getInfo();
      expect(response.data, isNotNull);
      expect(response.data!.version, equals('1.0.0'));
      expect(response.data!.gitCommit, equals('abc'));
      expect(response.statusCode, equals(200));
    });
  });
}
