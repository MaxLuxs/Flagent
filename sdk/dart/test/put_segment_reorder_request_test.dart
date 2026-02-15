import 'package:built_collection/built_collection.dart';
import 'package:test/test.dart';
import 'package:flagent_client/flagent_client.dart';

// tests for PutSegmentReorderRequest
void main() {
  final instance = PutSegmentReorderRequest((b) => b
    ..segmentIDs = ListBuilder<int>([3, 1, 2]));

  group(PutSegmentReorderRequest, () {
    test('to test the property `segmentIDs`', () async {
      expect(instance.segmentIDs.length, equals(3));
      expect(instance.segmentIDs[0], equals(3));
      expect(instance.segmentIDs[1], equals(1));
      expect(instance.segmentIDs[2], equals(2));
    });
  });
}
