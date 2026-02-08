import 'package:test/test.dart';
import 'package:flagent_client/flagent_client.dart';

// tests for EvalContext
void main() {
  final instance = EvalContextBuilder();
  // TODO add properties to the builder and call build()

  group(EvalContext, () {
    // EntityID is used to deterministically at random to evaluate the flag result. If it's empty, Flagent will randomly generate one.
    // String entityID
    test('to test the property `entityID`', () async {
      // TODO
    });

    // String entityType
    test('to test the property `entityType`', () async {
      // TODO
    });

    // BuiltMap<String, JsonObject> entityContext
    test('to test the property `entityContext`', () async {
      // TODO
    });

    // bool enableDebug (default value: false)
    test('to test the property `enableDebug`', () async {
      // TODO
    });

    // FlagID. flagID or flagKey will resolve to the same flag. Either works.
    // int flagID
    test('to test the property `flagID`', () async {
      // TODO
    });

    // FlagKey. flagID or flagKey will resolve to the same flag. Either works.
    // String flagKey
    test('to test the property `flagKey`', () async {
      // TODO
    });

    // FlagTags. flagTags looks up flags by tag. Either works.
    // BuiltList<String> flagTags
    test('to test the property `flagTags`', () async {
      // TODO
    });

    // Determine how flagTags is used to filter flags to be evaluated. OR extends the evaluation to those which contains at least one of the provided flagTags or AND limit the evaluation to those which contains all the flagTags.
    // String flagTagsOperator (default value: 'ANY')
    test('to test the property `flagTagsOperator`', () async {
      // TODO
    });

  });
}
