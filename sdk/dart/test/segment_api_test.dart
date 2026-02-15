import 'package:built_collection/built_collection.dart';
import 'package:test/test.dart';
import 'package:flagent_client/flagent_client.dart';

import 'mock_dio_helper.dart';

/// tests for SegmentApi
void main() {
  final client = createMockFlagentClient();
  final instance = client.getSegmentApi();

  group(SegmentApi, () {
    test('test createSegment', () async {
      final request = CreateSegmentRequest((b) => b
        ..description = 'New segment'
        ..rolloutPercent = 50);
      final response = await instance.createSegment(
        flagId: 1,
        createSegmentRequest: request,
      );
      expect(response.data, isNotNull);
      expect(response.data!.id, equals(1));
      expect(response.data!.description, equals('Seg'));
      expect(response.statusCode, equals(200));
    });

    test('test deleteSegment', () async {
      final response = await instance.deleteSegment(flagId: 1, segmentId: 1);
      expect(response.statusCode, equals(200));
    });

    test('test findSegments', () async {
      final response = await instance.findSegments(flagId: 1);
      expect(response.data, isNotNull);
      expect(response.data!, isEmpty);
      expect(response.statusCode, equals(200));
    });

    test('test putSegment', () async {
      final request = PutSegmentRequest((b) => b
        ..description = 'Updated'
        ..rolloutPercent = 75);
      final response = await instance.putSegment(
        flagId: 1,
        segmentId: 1,
        putSegmentRequest: request,
      );
      expect(response.data, isNotNull);
      expect(response.statusCode, equals(200));
    });

    test('test putSegmentReorder', () async {
      final request = PutSegmentReorderRequest((b) => b
        ..segmentIDs = ListBuilder<int>([1, 2, 3]));
      final response = await instance.putSegmentReorder(
        flagId: 1,
        putSegmentReorderRequest: request,
      );
      expect(response.statusCode, equals(200));
    });
  });
}
