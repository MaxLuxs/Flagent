import 'package:dio/dio.dart';
import 'package:flagent_enhanced/flagent_enhanced.dart';
import 'package:test/test.dart';

/// Builds a Dio that stubs POST /evaluation to return the given [variantKey].
Dio dioStubEval({String? variantKey}) {
  final dio = Dio(BaseOptions(baseUrl: 'http://localhost:18000/api/v1'));
  dio.interceptors.add(InterceptorsWrapper(
    onRequest: (options, handler) {
      if (options.path.endsWith('evaluation') &&
          !options.path.contains('batch') &&
          options.method == 'POST') {
        final data = <String, dynamic>{};
        if (variantKey != null) data['variantKey'] = variantKey;
        return handler.resolve(
          Response(requestOptions: options, data: data, statusCode: 200),
        );
      }
      return handler.next(options);
    },
  ));
  return dio;
}

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

    group('isEnabled', () {
      test('returns true when variantKey is on', () async {
        manager = FlagentManager(
          'http://localhost:18000/api/v1',
          dio: dioStubEval(variantKey: 'on'),
          config: FlagentConfig(enableCache: false),
        );
        final result = await manager!.isEnabled(
          flagKey: 'test_flag',
          entityID: 'user1',
          defaultValue: false,
        );
        expect(result, isTrue);
      });

      test('returns true when variantKey is non-off value', () async {
        manager = FlagentManager(
          'http://localhost:18000/api/v1',
          dio: dioStubEval(variantKey: 'variant_a'),
          config: FlagentConfig(enableCache: false),
        );
        final result = await manager!.isEnabled(
          flagKey: 'test_flag',
          entityID: 'user1',
          defaultValue: false,
        );
        expect(result, isTrue);
      });

      test('returns false when variantKey is off', () async {
        manager = FlagentManager(
          'http://localhost:18000/api/v1',
          dio: dioStubEval(variantKey: 'off'),
          config: FlagentConfig(enableCache: false),
        );
        final result = await manager!.isEnabled(
          flagKey: 'test_flag',
          entityID: 'user1',
          defaultValue: true,
        );
        expect(result, isFalse);
      });

      test('returns false when variantKey is false', () async {
        manager = FlagentManager(
          'http://localhost:18000/api/v1',
          dio: dioStubEval(variantKey: 'false'),
          config: FlagentConfig(enableCache: false),
        );
        final result = await manager!.isEnabled(
          flagKey: 'test_flag',
          entityID: 'user1',
          defaultValue: true,
        );
        expect(result, isFalse);
      });

      test('returns false when variantKey is 0', () async {
        manager = FlagentManager(
          'http://localhost:18000/api/v1',
          dio: dioStubEval(variantKey: '0'),
          config: FlagentConfig(enableCache: false),
        );
        final result = await manager!.isEnabled(
          flagKey: 'test_flag',
          entityID: 'user1',
          defaultValue: true,
        );
        expect(result, isFalse);
      });

      test('returns defaultValue when variantKey is null/empty', () async {
        manager = FlagentManager(
          'http://localhost:18000/api/v1',
          dio: dioStubEval(), // no variantKey
          config: FlagentConfig(enableCache: false),
        );
        final resultFalse = await manager!.isEnabled(
          flagKey: 'test_flag',
          entityID: 'user1',
          defaultValue: false,
        );
        expect(resultFalse, isFalse);
        final resultTrue = await manager!.isEnabled(
          flagKey: 'test_flag',
          entityID: 'user1',
          defaultValue: true,
        );
        expect(resultTrue, isTrue);
      });
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
