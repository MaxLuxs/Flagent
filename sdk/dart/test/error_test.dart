import 'package:test/test.dart';
import 'package:flagent_client/flagent_client.dart';

// tests for Error
void main() {
  final instance = Error((b) => b..message = 'Something went wrong');

  group(Error, () {
    test('to test the property `message`', () async {
      expect(instance.message, equals('Something went wrong'));
    });
  });
}
