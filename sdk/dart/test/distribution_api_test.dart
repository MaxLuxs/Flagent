import 'package:test/test.dart';
import 'package:flagent_client/flagent_client.dart';


/// tests for DistributionApi
void main() {
  final instance = FlagentClient().getDistributionApi();

  group(DistributionApi, () {
    // Get distributions for segment
    //
    //Future<BuiltList<Distribution>> findDistributions(int flagId, int segmentId) async
    test('test findDistributions', () async {
      // TODO
    });

    // Update distributions
    //
    // Replace the distribution with the new setting. The sum of all percentages must equal 100.
    //
    //Future<BuiltList<Distribution>> putDistributions(int flagId, int segmentId, PutDistributionsRequest putDistributionsRequest) async
    test('test putDistributions', () async {
      // TODO
    });

  });
}
