import 'package:built_collection/built_collection.dart';
import 'package:test/test.dart';
import 'package:flagent_client/flagent_client.dart';

import 'mock_dio_helper.dart';

/// tests for EvaluationApi
void main() {
  final client = createMockFlagentClient();
  final instance = client.getEvaluationApi();

  group(EvaluationApi, () {
    test('test postEvaluation', () async {
      final context = EvalContext((b) => b
        ..entityID = 'user-1'
        ..entityType = 'user'
        ..flagKey = 'test_flag');
      final response = await instance.postEvaluation(evalContext: context);
      expect(response.data, isNotNull);
      expect(response.data!.flagKey, equals('test_flag'));
      expect(response.data!.variantKey, equals('control'));
      expect(response.statusCode, equals(200));
    });

    test('test postEvaluationBatch', () async {
      final request = EvaluationBatchRequest((b) => b
        ..entities = ListBuilder<EvaluationEntity>([
          EvaluationEntity((e) => e..entityID = 'user-1'..entityType = 'user'),
        ])
        ..flagKeys = ListBuilder<String>(['test_flag']));
      final response = await instance.postEvaluationBatch(
        evaluationBatchRequest: request,
      );
      expect(response.data, isNotNull);
      expect(response.data!.evaluationResults, isEmpty);
      expect(response.statusCode, equals(200));
    });
  });
}
