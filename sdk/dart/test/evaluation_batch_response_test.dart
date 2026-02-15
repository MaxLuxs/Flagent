import 'package:built_collection/built_collection.dart';
import 'package:test/test.dart';
import 'package:flagent_client/flagent_client.dart';

// tests for EvaluationBatchResponse
void main() {
  final instance = EvaluationBatchResponse((b) => b
    ..evaluationResults = ListBuilder<EvalResult>([
      EvalResult((e) => e
        ..flagKey = 'test_flag'
        ..variantKey = 'control'),
    ]));

  group(EvaluationBatchResponse, () {
    test('to test the property `evaluationResults`', () async {
      expect(instance.evaluationResults.length, equals(1));
      expect(instance.evaluationResults[0].flagKey, equals('test_flag'));
      expect(instance.evaluationResults[0].variantKey, equals('control'));
    });
  });
}
