import 'package:test/test.dart';
import 'package:flagent_client/flagent_client.dart';

// tests for SegmentDebugLog
void main() {
  final instance = SegmentDebugLog((b) => b
    ..segmentID = 1
    ..msg = 'Segment matched');

  group(SegmentDebugLog, () {
    test('to test the property `segmentID`', () async {
      expect(instance.segmentID, equals(1));
    });

    test('to test the property `msg`', () async {
      expect(instance.msg, equals('Segment matched'));
    });
  });
}
