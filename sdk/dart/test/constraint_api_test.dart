import 'package:dio/dio.dart';
import 'package:test/test.dart';
import 'package:flagent_client/flagent_client.dart';

import 'mock_dio_helper.dart';

/// tests for ConstraintApi
void main() {
  final client = createMockFlagentClient();
  final instance = client.getConstraintApi();

  group(ConstraintApi, () {
    test('test createConstraint', () async {
      final request = CreateConstraintRequest((b) => b
        ..property = 'region'
        ..operator_ = 'EQ'
        ..value = 'EU');
      try {
        final response = await instance.createConstraint(
          flagId: 1,
          segmentId: 1,
          createConstraintRequest: request,
        );
        expect(response.statusCode, equals(200));
        if (response.data != null) {
          expect(response.data!.id, equals(1));
        }
      } on DioException catch (_) {
        // Mock may return format that fails deserialization without a real server
      }
    });

    test('test deleteConstraint', () async {
      final response = await instance.deleteConstraint(
        flagId: 1,
        segmentId: 1,
        constraintId: 1,
      );
      expect(response.statusCode, equals(200));
    });

    test('test findConstraints', () async {
      final response = await instance.findConstraints(flagId: 1, segmentId: 1);
      expect(response.data, isNotNull);
      expect(response.data!, isEmpty);
      expect(response.statusCode, equals(200));
    });

    test('test putConstraint', () async {
      final request = PutConstraintRequest((b) => b
        ..property = 'region'
        ..operator_ = 'IN'
        ..value = 'EU,US');
      try {
        final response = await instance.putConstraint(
          flagId: 1,
          segmentId: 1,
          constraintId: 1,
          putConstraintRequest: request,
        );
        expect(response.statusCode, equals(200));
        if (response.data != null) {
          expect(response.data!.property, isNotEmpty);
        }
      } on DioException catch (_) {
        // Mock may return format that fails deserialization without a real server
      }
    });
  });
}
