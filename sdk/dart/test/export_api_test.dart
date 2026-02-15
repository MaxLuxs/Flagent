import 'dart:typed_data';

import 'package:test/test.dart';
import 'package:flagent_client/flagent_client.dart';

import 'mock_dio_helper.dart';

/// tests for ExportApi
void main() {
  final client = createMockFlagentClient();
  final instance = client.getExportApi();

  group(ExportApi, () {
    test('test getExportEvalCacheJSON', () async {
      final response = await instance.getExportEvalCacheJSON();
      expect(response.statusCode, equals(200));
      expect(response.data, isNotNull);
      // BuiltMap from empty {} or serialized empty map
      expect(response.data!.length, greaterThanOrEqualTo(0));
    });

    test('test getExportSQLite', () async {
      final response = await instance.getExportSQLite();
      expect(response.data, isNotNull);
      expect(response.data, isA<Uint8List>());
      expect(response.statusCode, equals(200));
    });
  });
}
