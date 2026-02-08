import 'package:test/test.dart';
import 'package:flagent_client/flagent_client.dart';


/// tests for EvaluationApi
void main() {
  final instance = FlagentClient().getEvaluationApi();

  group(EvaluationApi, () {
    // Evaluate flag
    //
    //Future<EvalResult> postEvaluation(EvalContext evalContext) async
    test('test postEvaluation', () async {
      // TODO
    });

    // Batch evaluate flags
    //
    // Evaluate multiple flags for multiple entities in a single request. More efficient than multiple single evaluation requests.
    //
    //Future<EvaluationBatchResponse> postEvaluationBatch(EvaluationBatchRequest evaluationBatchRequest) async
    test('test postEvaluationBatch', () async {
      // TODO
    });

  });
}
