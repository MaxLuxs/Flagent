import 'package:flagent_enhanced/flagent_enhanced.dart';
import 'package:test/test.dart';

void main() {
  group('FlagentConfig', () {
    test('default config', () {
      const config = FlagentConfig();
      expect(config.enableCache, isTrue);
      expect(config.enableDebugLogging, isFalse);
      expect(config.cacheTtlMs, equals(5 * 60 * 1000));
    });

    test('custom config', () {
      const config = FlagentConfig(
        cacheTtlMs: 120000,
        enableCache: false,
        enableDebugLogging: true,
      );
      expect(config.cacheTtlMs, equals(120000));
      expect(config.enableCache, isFalse);
      expect(config.enableDebugLogging, isTrue);
    });
  });

  group('defaultFlagentConfig', () {
    test('matches default FlagentConfig', () {
      expect(defaultFlagentConfig.cacheTtlMs, equals(5 * 60 * 1000));
      expect(defaultFlagentConfig.enableCache, isTrue);
    });
  });
}
