import 'package:test/test.dart';
import 'package:flagent_client/flagent_client.dart';


/// tests for TagApi
void main() {
  final instance = FlagentClient().getTagApi();

  group(TagApi, () {
    // Create tag and associate with flag
    //
    // Create a tag and associate it with the flag. Tags are used for organizing and filtering flags.
    //
    //Future<Tag> createFlagTag(int flagId, CreateTagRequest createTagRequest) async
    test('test createFlagTag', () async {
      // TODO
    });

    // Remove tag from flag
    //
    //Future deleteFlagTag(int flagId, int tagId) async
    test('test deleteFlagTag', () async {
      // TODO
    });

    // Get all tags
    //
    //Future<BuiltList<Tag>> findAllTags({ int limit, int offset, String valueLike }) async
    test('test findAllTags', () async {
      // TODO
    });

    // Get tags for flag
    //
    //Future<BuiltList<Tag>> findFlagTags(int flagId) async
    test('test findFlagTags', () async {
      // TODO
    });

  });
}
