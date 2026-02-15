// Integration tests for EvaluationApi against a live Flagent backend.
//
// Run with backend up and optional env:
//   FLAGENT_BASE_URL=http://localhost:18000/api/v1 dart run test test/evaluation_api_integration_test.dart
//
// If FLAGENT_BASE_URL is not set, all tests are skipped.

import 'dart:io';

import 'package:built_collection/built_collection.dart';
import 'package:test/test.dart';
import 'package:flagent_client/flagent_client.dart';

final baseUrl = Platform.environment['FLAGENT_BASE_URL'];

void main() {
  if ((baseUrl ?? '').isEmpty) {
    test('integration tests skipped (set FLAGENT_BASE_URL to run)', () {
      expect(true, isTrue);
    });
    return;
  }

  late FlagentClient client;
  late EvaluationApi api;

  setUpAll(() {
    client = FlagentClient(basePathOverride: baseUrl);
    api = client.getEvaluationApi();
  });

  group('EvaluationApi (integration)', () {
    test('postEvaluation with flagKey and entity', () async {
      final context = EvalContext((b) => b
        ..entityID = 'user-integration-dart'
        ..entityType = 'user'
        ..flagKey = 'my_feature_flag');
      final response = await api.postEvaluation(evalContext: context);
      expect(response.statusCode, 200);
      expect(response.data, isNotNull);
      expect(response.data!.flagKey, 'my_feature_flag');
      expect(response.data!.variantKey, isNotNull);
    });

    test('postEvaluationBatch with flagKeys and entities', () async {
      final request = EvaluationBatchRequest((b) => b
        ..entities = ListBuilder<EvaluationEntity>([
          EvaluationEntity((e) => e
            ..entityID = 'batch-dart-1'
            ..entityType = 'user'),
        ])
        ..flagKeys = ListBuilder<String>(['my_feature_flag']));
      final response = await api.postEvaluationBatch(
        evaluationBatchRequest: request,
      );
      expect(response.statusCode, 200);
      expect(response.data, isNotNull);
      expect(response.data!.evaluationResults, isNotNull);
    });
  });
}
