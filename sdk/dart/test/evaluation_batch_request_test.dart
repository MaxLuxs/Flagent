import 'package:test/test.dart';
import 'package:flagent_client/flagent_client.dart';

// tests for EvaluationBatchRequest
void main() {
  final instance = EvaluationBatchRequestBuilder();
  // TODO add properties to the builder and call build()

  group(EvaluationBatchRequest, () {
    // BuiltList<EvaluationEntity> entities
    test('to test the property `entities`', () async {
      // TODO
    });

    // bool enableDebug (default value: false)
    test('to test the property `enableDebug`', () async {
      // TODO
    });

    // FlagIDs. Either flagIDs, flagKeys or flagTags works. If pass in multiples, Flagent may return duplicate results.
    // BuiltList<int> flagIDs
    test('to test the property `flagIDs`', () async {
      // TODO
    });

    // FlagKeys. Either flagIDs, flagKeys or flagTags works. If pass in multiples, Flagent may return duplicate results.
    // BuiltList<String> flagKeys
    test('to test the property `flagKeys`', () async {
      // TODO
    });

    // FlagTags. Either flagIDs, flagKeys or flagTags works. If pass in multiples, Flagent may return duplicate results.
    // BuiltList<String> flagTags
    test('to test the property `flagTags`', () async {
      // TODO
    });

    // Determine how flagTags is used to filter flags to be evaluated.
    // String flagTagsOperator (default value: 'ANY')
    test('to test the property `flagTagsOperator`', () async {
      // TODO
    });

  });
}
