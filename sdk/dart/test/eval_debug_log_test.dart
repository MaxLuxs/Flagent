import 'package:built_collection/built_collection.dart';
import 'package:test/test.dart';
import 'package:flagent_client/flagent_client.dart';

// tests for EvalDebugLog
void main() {
  final instance = EvalDebugLog((b) => b
    ..msg = 'Evaluation debug'
    ..segmentDebugLogs = ListBuilder<SegmentDebugLog>([
      SegmentDebugLog((s) => s..segmentID = 1..msg = 'Matched'),
    ]));

  group(EvalDebugLog, () {
    test('to test the property `msg`', () async {
      expect(instance.msg, equals('Evaluation debug'));
    });

    test('to test the property `segmentDebugLogs`', () async {
      expect(instance.segmentDebugLogs, isNotNull);
      expect(instance.segmentDebugLogs!.length, equals(1));
      expect(instance.segmentDebugLogs![0].msg, equals('Matched'));
    });
  });
}
