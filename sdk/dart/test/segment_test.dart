import 'package:built_collection/built_collection.dart';
import 'package:test/test.dart';
import 'package:flagent_client/flagent_client.dart';

// tests for Segment
void main() {
  final instance = Segment((b) => b
    ..id = 1
    ..flagID = 10
    ..description = 'EU users'
    ..rank = 1
    ..rolloutPercent = 100
    ..constraints = ListBuilder<Constraint>()
    ..distributions = ListBuilder<Distribution>());

  group(Segment, () {
    test('to test the property `id`', () async {
      expect(instance.id, equals(1));
    });

    test('to test the property `flagID`', () async {
      expect(instance.flagID, equals(10));
    });

    test('to test the property `description`', () async {
      expect(instance.description, equals('EU users'));
    });

    test('to test the property `rank`', () async {
      expect(instance.rank, equals(1));
    });

    test('to test the property `rolloutPercent`', () async {
      expect(instance.rolloutPercent, equals(100));
    });

    test('to test the property `constraints`', () async {
      expect(instance.constraints, isNotNull);
      expect(instance.constraints!.length, equals(0));
    });

    test('to test the property `distributions`', () async {
      expect(instance.distributions, isNotNull);
      expect(instance.distributions!.length, equals(0));
    });
  });
}
