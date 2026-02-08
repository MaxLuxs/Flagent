import 'package:dio/dio.dart';
import 'package:flagent_enhanced/flagent_enhanced.dart';
import 'package:test/test.dart';

void main() {
  group('FlagentManager', () {
    late Dio mockDio;
    FlagentManager? manager;

    setUp(() {
      mockDio = Dio(BaseOptions(baseUrl: 'http://localhost:18000/api/v1'));
    });

    tearDown(() {
      manager?.destroy();
    });

    group('buildEvaluationEntity', () {
      test('creates entity with required fields', () {
        final entity = buildEvaluationEntity(
          entityID: 'user1',
          entityType: 'user',
          entityContext: {'region': 'US'},
        );

        expect(entity.entityID, equals('user1'));
        expect(entity.entityType, equals('user'));
      });

      test('creates entity with minimal fields', () {
        final entity = buildEvaluationEntity(entityID: 'anon');

        expect(entity.entityID, equals('anon'));
        expect(entity.entityType, isNull);
        expect(entity.entityContext, isNull);
      });
    });

    group('with cache disabled', () {
      setUp(() {
        manager = FlagentManager(
          'http://localhost:18000/api/v1',
          dio: mockDio,
          config: FlagentConfig(enableCache: false),
        );
      });

      test('clearCache does not throw', () async {
        await manager!.clearCache();
      });

      test('evictExpired does not throw', () async {
        await manager!.evictExpired();
      });
    });

    group('destroy', () {
      test('does not throw', () {
        final m = FlagentManager(
          'http://localhost:18000/api/v1',
          config: FlagentConfig(enableCache: false),
        );
        m.destroy();
      });
    });
  });
}
