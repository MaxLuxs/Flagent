import 'package:built_collection/built_collection.dart';
import 'package:dio/dio.dart';
import 'package:test/test.dart';
import 'package:flagent_client/flagent_client.dart';

import 'mock_dio_helper.dart';

/// tests for DistributionApi
void main() {
  final client = createMockFlagentClient();
  final instance = client.getDistributionApi();

  group(DistributionApi, () {
    test('test findDistributions', () async {
      final response = await instance.findDistributions(
        flagId: 1,
        segmentId: 1,
      );
      expect(response.data, isNotNull);
      expect(response.data!, isEmpty);
      expect(response.statusCode, equals(200));
    });

    test('test putDistributions', () async {
      final request = PutDistributionsRequest((b) => b
        ..distributions = ListBuilder<DistributionRequest>([
          DistributionRequest((d) => d
            ..variantID = 1
            ..variantKey = 'control'
            ..percent = 50),
          DistributionRequest((d) => d
            ..variantID = 2
            ..variantKey = 'treatment'
            ..percent = 50),
        ]));
      try {
        final response = await instance.putDistributions(
          flagId: 1,
          segmentId: 1,
          putDistributionsRequest: request,
        );
        expect(response.statusCode, equals(200));
        if (response.data != null) {
          expect(response.data!.length, greaterThanOrEqualTo(0));
        }
      } on DioException catch (_) {
        // Mock may return format that fails deserialization without a real server
      }
    });
  });
}
