import 'package:test/test.dart';
import 'package:flagent_client/flagent_client.dart';


/// tests for HealthApi
void main() {
  final instance = FlagentClient().getHealthApi();

  group(HealthApi, () {
    // Health check
    //
    // Check if Flagent is healthy
    //
    //Future<Health> getHealth() async
    test('test getHealth', () async {
      // TODO
    });

    // Get version information
    //
    //Future<Info> getInfo() async
    test('test getInfo', () async {
      // TODO
    });

  });
}
