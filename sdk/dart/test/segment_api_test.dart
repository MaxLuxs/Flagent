import 'package:test/test.dart';
import 'package:flagent_client/flagent_client.dart';


/// tests for SegmentApi
void main() {
  final instance = FlagentClient().getSegmentApi();

  group(SegmentApi, () {
    // Create segment
    //
    // Create a new segment for the flag. Segments define the audience and are evaluated in order by rank.
    //
    //Future<Segment> createSegment(int flagId, CreateSegmentRequest createSegmentRequest) async
    test('test createSegment', () async {
      // TODO
    });

    // Delete segment
    //
    // Delete a segment. This will also delete all constraints and distributions associated with the segment.
    //
    //Future deleteSegment(int flagId, int segmentId) async
    test('test deleteSegment', () async {
      // TODO
    });

    // Get segments for flag
    //
    //Future<BuiltList<Segment>> findSegments(int flagId) async
    test('test findSegments', () async {
      // TODO
    });

    // Update segment
    //
    //Future<Segment> putSegment(int flagId, int segmentId, PutSegmentRequest putSegmentRequest) async
    test('test putSegment', () async {
      // TODO
    });

    // Reorder segments
    //
    //Future putSegmentReorder(int flagId, PutSegmentReorderRequest putSegmentReorderRequest) async
    test('test putSegmentReorder', () async {
      // TODO
    });

  });
}
