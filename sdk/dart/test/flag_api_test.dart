import 'package:test/test.dart';
import 'package:flagent_client/flagent_client.dart';


/// tests for FlagApi
void main() {
  final instance = FlagentClient().getFlagApi();

  group(FlagApi, () {
    // Create a new flag
    //
    //Future<Flag> createFlag(CreateFlagRequest createFlagRequest) async
    test('test createFlag', () async {
      // TODO
    });

    // Delete flag
    //
    //Future deleteFlag(int flagId) async
    test('test deleteFlag', () async {
      // TODO
    });

    // Get all flags
    //
    //Future<BuiltList<Flag>> findFlags({ int limit, int offset, bool enabled, String description, String key, String descriptionLike, bool preload, bool deleted, String tags }) async
    test('test findFlags', () async {
      // TODO
    });

    // Get flag by ID
    //
    //Future<Flag> getFlag(int flagId) async
    test('test getFlag', () async {
      // TODO
    });

    // Get all entity types
    //
    //Future<BuiltList<String>> getFlagEntityTypes() async
    test('test getFlagEntityTypes', () async {
      // TODO
    });

    // Get flag snapshots
    //
    //Future<BuiltList<FlagSnapshot>> getFlagSnapshots(int flagId, { int limit, int offset, String sort }) async
    test('test getFlagSnapshots', () async {
      // TODO
    });

    // Update flag
    //
    //Future<Flag> putFlag(int flagId, PutFlagRequest putFlagRequest) async
    test('test putFlag', () async {
      // TODO
    });

    // Restore deleted flag
    //
    //Future<Flag> restoreFlag(int flagId) async
    test('test restoreFlag', () async {
      // TODO
    });

    // Set flag enabled status
    //
    //Future<Flag> setFlagEnabled(int flagId, SetFlagEnabledRequest setFlagEnabledRequest) async
    test('test setFlagEnabled', () async {
      // TODO
    });

  });
}
