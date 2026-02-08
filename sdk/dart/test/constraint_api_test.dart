import 'package:test/test.dart';
import 'package:flagent_client/flagent_client.dart';


/// tests for ConstraintApi
void main() {
  final instance = FlagentClient().getConstraintApi();

  group(ConstraintApi, () {
    // Create constraint
    //
    // Create a constraint for the segment. Constraints define conditions that must be met for a segment to match.
    //
    //Future<Constraint> createConstraint(int flagId, int segmentId, CreateConstraintRequest createConstraintRequest) async
    test('test createConstraint', () async {
      // TODO
    });

    // Delete constraint
    //
    // Delete a constraint from the segment.
    //
    //Future deleteConstraint(int flagId, int segmentId, int constraintId) async
    test('test deleteConstraint', () async {
      // TODO
    });

    // Get constraints for segment
    //
    //Future<BuiltList<Constraint>> findConstraints(int flagId, int segmentId) async
    test('test findConstraints', () async {
      // TODO
    });

    // Update constraint
    //
    //Future<Constraint> putConstraint(int flagId, int segmentId, int constraintId, PutConstraintRequest putConstraintRequest) async
    test('test putConstraint', () async {
      // TODO
    });

  });
}
