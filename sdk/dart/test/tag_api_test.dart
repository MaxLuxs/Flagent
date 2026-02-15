import 'package:test/test.dart';
import 'package:flagent_client/flagent_client.dart';

import 'mock_dio_helper.dart';

/// tests for TagApi
void main() {
  final client = createMockFlagentClient();
  final instance = client.getTagApi();

  group(TagApi, () {
    test('test createFlagTag', () async {
      final request = CreateTagRequest((b) => b..value = 'production');
      final response = await instance.createFlagTag(
        flagId: 1,
        createTagRequest: request,
      );
      expect(response.data, isNotNull);
      expect(response.data!.id, equals(1));
      expect(response.data!.value, equals('tag1'));
      expect(response.statusCode, equals(200));
    });

    test('test deleteFlagTag', () async {
      final response = await instance.deleteFlagTag(flagId: 1, tagId: 1);
      expect(response.statusCode, equals(200));
    });

    test('test findAllTags', () async {
      final response = await instance.findAllTags();
      expect(response.data, isNotNull);
      expect(response.data!, isEmpty);
      expect(response.statusCode, equals(200));
    });

    test('test findFlagTags', () async {
      final response = await instance.findFlagTags(flagId: 1);
      expect(response.data, isNotNull);
      expect(response.data!, isEmpty);
      expect(response.statusCode, equals(200));
    });
  });
}
