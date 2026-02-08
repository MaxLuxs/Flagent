import 'package:test/test.dart';
import 'package:flagent_client/flagent_client.dart';


/// tests for ExportApi
void main() {
  final instance = FlagentClient().getExportApi();

  group(ExportApi, () {
    // Export eval cache as JSON
    //
    // Export JSON format of the eval cache dump. This endpoint exports the current state of the evaluation cache in JSON format.
    //
    //Future<BuiltMap<String, JsonObject>> getExportEvalCacheJSON() async
    test('test getExportEvalCacheJSON', () async {
      // TODO
    });

    // Export database as SQLite
    //
    // Export sqlite3 format of the db dump, which is converted from the main database. Returns a SQLite database file that can be used for backup or migration purposes.
    //
    //Future<Uint8List> getExportSQLite({ bool excludeSnapshots }) async
    test('test getExportSQLite', () async {
      // TODO
    });

  });
}
