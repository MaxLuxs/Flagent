import 'package:test/test.dart';
import 'package:flagent_client/flagent_client.dart';

// tests for Flag
void main() {
  final instance = FlagBuilder();
  // TODO add properties to the builder and call build()

  group(Flag, () {
    // int id
    test('to test the property `id`', () async {
      // TODO
    });

    // Unique key representation of the flag
    // String key
    test('to test the property `key`', () async {
      // TODO
    });

    // String description
    test('to test the property `description`', () async {
      // TODO
    });

    // bool enabled
    test('to test the property `enabled`', () async {
      // TODO
    });

    // int snapshotID
    test('to test the property `snapshotID`', () async {
      // TODO
    });

    // Enabled data records will get data logging in the metrics pipeline
    // bool dataRecordsEnabled
    test('to test the property `dataRecordsEnabled`', () async {
      // TODO
    });

    // It will override the entityType in the evaluation logs if it's not empty
    // String entityType
    test('to test the property `entityType`', () async {
      // TODO
    });

    // Flag usage details in markdown format
    // String notes
    test('to test the property `notes`', () async {
      // TODO
    });

    // String createdBy
    test('to test the property `createdBy`', () async {
      // TODO
    });

    // String updatedBy
    test('to test the property `updatedBy`', () async {
      // TODO
    });

    // BuiltList<Segment> segments
    test('to test the property `segments`', () async {
      // TODO
    });

    // BuiltList<Variant> variants
    test('to test the property `variants`', () async {
      // TODO
    });

    // BuiltList<Tag> tags
    test('to test the property `tags`', () async {
      // TODO
    });

  });
}
