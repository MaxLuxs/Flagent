import 'dart:typed_data';

import 'package:dio/dio.dart';
import 'package:flagent_client/flagent_client.dart';
import 'package:flagent_client/src/api.dart';

/// Creates a [FlagentClient] with a Dio instance that intercepts requests
/// and returns mock JSON responses. Used for API tests without a real server.
FlagentClient createMockFlagentClient({
  Map<String, dynamic> Function(String path, String method)? responseFor,
}) {
  final dio = Dio(BaseOptions(
    baseUrl: 'http://localhost:18000/api/v1',
    connectTimeout: const Duration(milliseconds: 100),
    receiveTimeout: const Duration(milliseconds: 100),
  ));

  dio.interceptors.add(InterceptorsWrapper(
    onRequest: (options, handler) {
      final path = options.uri.path;
      final method = options.method;
      dynamic data;
      if (responseFor != null) {
        data = responseFor(path, method);
      } else {
        data = _defaultMockResponse(path, method);
      }
      handler.resolve(Response(
        requestOptions: options,
        data: data,
        statusCode: 200,
      ));
    },
  ));

  return FlagentClient(dio: dio);
}

dynamic _defaultMockResponse(String path, String method) {
  if (path == '/health' || path.endsWith('/health')) {
    return {'status': 'ok'};
  }
  if (path == '/info' || path.endsWith('/info')) {
    return {'version': '1.0.0', 'buildTime': '2024-01-01', 'gitCommit': 'abc'};
  }
  if (path.endsWith('/flags') && method == 'GET') {
    return [];
  }
  final singleFlagJson = {
    'id': 1,
    'key': 'test_flag',
    'description': 'Test',
    'enabled': true,
    'dataRecordsEnabled': false,
  };
  final isSingleFlagPath = path.contains('/flags/') &&
      !path.contains('snapshots') &&
      !path.contains('entity_types') &&
      !path.contains('/segments') &&
      !path.contains('/variants') &&
      !path.contains('/tags') &&
      !path.contains('/constraints') &&
      !path.contains('/distributions') &&
      !path.endsWith('/flags');
  if (isSingleFlagPath && (method == 'GET' || method == 'PUT' || method == 'POST')) {
    return singleFlagJson;
  }
  if (path.endsWith('/flags') && method == 'POST') {
    return {
      'id': 1,
      'key': 'new_flag',
      'description': 'Created',
      'enabled': true,
      'dataRecordsEnabled': false,
    };
  }
  if (path.contains('/snapshots')) {
    return [];
  }
  if (path.contains('entity_types')) {
    return ['user', 'device'];
  }
  if (path.endsWith('/evaluation') && method == 'POST' && !path.contains('batch')) {
    return {
      'flagKey': 'test_flag',
      'variantKey': 'control',
      'segmentID': 1,
      'variantID': 1,
    };
  }
  if (path.endsWith('/evaluation/batch') && method == 'POST') {
    return {'evaluationResults': []};
  }
  if (path.contains('/segments')) {
    if (method == 'GET') return [];
    return {'id': 1, 'flagID': 1, 'description': 'Seg', 'rank': 1, 'rolloutPercent': 100};
  }
  if (path.contains('/variants')) {
    if (method == 'GET') return [];
    return {'id': 1, 'flagID': 1, 'key': 'control'};
  }
  if (path.contains('/tags')) {
    if (method == 'GET') return [];
    return {'id': 1, 'value': 'tag1'};
  }
  if (path.contains('/constraints')) {
    if (method == 'GET') return [];
    // built_value list format: [key, value, key, value, ...]
    return [
      r'id', 1, r'segmentID', 1, r'property', 'x', r'operator', 'EQ', r'value', 'y'
    ];
  }
  if (path.contains('/distributions')) {
    if (method == 'GET') return [];
    if (method == 'PUT') {
      return [
        [r'id', 1, r'segmentID', 1, r'variantID', 1, r'variantKey', 'control', r'percent', 50],
        [r'id', 2, r'segmentID', 1, r'variantID', 2, r'variantKey', 'treatment', r'percent', 50],
      ];
    }
    return [r'id', 1, r'segmentID', 1, r'variantID', 1, r'variantKey', 'control', r'percent', 100];
  }
  if (path == '/export/eval_cache/json' || path.endsWith('eval_cache/json')) {
    return {};
  }
  if (path == '/export/sqlite' || path.endsWith('export/sqlite')) {
    return Uint8List(0);
  }
  return {};
}
