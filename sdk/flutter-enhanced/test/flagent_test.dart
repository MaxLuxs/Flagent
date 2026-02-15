import 'package:flagent_enhanced/flagent_enhanced.dart';
import 'package:test/test.dart';

void main() {
  group('Flagent.create', () {
    test('returns FlagentManager', () {
      final client = Flagent.create(
        baseUrl: 'https://api.example.com/api/v1',
        config: FlagentConfig(enableCache: false),
      );
      expect(client, isA<FlagentManager>());
      client.destroy();
    });

    test('with default config uses cache', () {
      final client = Flagent.create(baseUrl: 'https://api.example.com/api/v1');
      expect(client, isA<FlagentManager>());
      client.destroy();
    });
  });

  group('Flagent.managed', () {
    test('returns FlagentManager with given config', () {
      const config = FlagentConfig(
        cacheTtlMs: 60000,
        enableCache: false,
      );
      final client = Flagent.managed('https://api.example.com/api/v1', config);
      expect(client, isA<FlagentManager>());
      client.destroy();
    });
  });

  group('Flagent.fromOptions', () {
    test('returns FlagentManager matching options', () {
      const options = FlagentOptions(
        baseUrl: 'https://api.example.com/api/v1',
        config: FlagentConfig(enableCache: false),
      );
      final client = Flagent.fromOptions(options);
      expect(client, isA<FlagentManager>());
      client.destroy();
    });
  });
}
