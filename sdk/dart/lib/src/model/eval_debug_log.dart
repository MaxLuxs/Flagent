//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:built_collection/built_collection.dart';
import 'package:flagent_client/src/model/segment_debug_log.dart';
import 'package:built_value/built_value.dart';
import 'package:built_value/serializer.dart';

part 'eval_debug_log.g.dart';

/// EvalDebugLog
///
/// Properties:
/// * [msg] 
/// * [segmentDebugLogs] 
@BuiltValue()
abstract class EvalDebugLog implements Built<EvalDebugLog, EvalDebugLogBuilder> {
  @BuiltValueField(wireName: r'msg')
  String? get msg;

  @BuiltValueField(wireName: r'segmentDebugLogs')
  BuiltList<SegmentDebugLog>? get segmentDebugLogs;

  EvalDebugLog._();

  factory EvalDebugLog([void updates(EvalDebugLogBuilder b)]) = _$EvalDebugLog;

  @BuiltValueHook(initializeBuilder: true)
  static void _defaults(EvalDebugLogBuilder b) => b;

  @BuiltValueSerializer(custom: true)
  static Serializer<EvalDebugLog> get serializer => _$EvalDebugLogSerializer();
}

class _$EvalDebugLogSerializer implements PrimitiveSerializer<EvalDebugLog> {
  @override
  final Iterable<Type> types = const [EvalDebugLog, _$EvalDebugLog];

  @override
  final String wireName = r'EvalDebugLog';

  Iterable<Object?> _serializeProperties(
    Serializers serializers,
    EvalDebugLog object, {
    FullType specifiedType = FullType.unspecified,
  }) sync* {
    if (object.msg != null) {
      yield r'msg';
      yield serializers.serialize(
        object.msg,
        specifiedType: const FullType(String),
      );
    }
    if (object.segmentDebugLogs != null) {
      yield r'segmentDebugLogs';
      yield serializers.serialize(
        object.segmentDebugLogs,
        specifiedType: const FullType(BuiltList, [FullType(SegmentDebugLog)]),
      );
    }
  }

  @override
  Object serialize(
    Serializers serializers,
    EvalDebugLog object, {
    FullType specifiedType = FullType.unspecified,
  }) {
    return _serializeProperties(serializers, object, specifiedType: specifiedType).toList();
  }

  void _deserializeProperties(
    Serializers serializers,
    Object serialized, {
    FullType specifiedType = FullType.unspecified,
    required List<Object?> serializedList,
    required EvalDebugLogBuilder result,
    required List<Object?> unhandled,
  }) {
    for (var i = 0; i < serializedList.length; i += 2) {
      final key = serializedList[i] as String;
      final value = serializedList[i + 1];
      switch (key) {
        case r'msg':
          final valueDes = serializers.deserialize(
            value,
            specifiedType: const FullType(String),
          ) as String;
          result.msg = valueDes;
          break;
        case r'segmentDebugLogs':
          final valueDes = serializers.deserialize(
            value,
            specifiedType: const FullType(BuiltList, [FullType(SegmentDebugLog)]),
          ) as BuiltList<SegmentDebugLog>;
          result.segmentDebugLogs.replace(valueDes);
          break;
        default:
          unhandled.add(key);
          unhandled.add(value);
          break;
      }
    }
  }

  @override
  EvalDebugLog deserialize(
    Serializers serializers,
    Object serialized, {
    FullType specifiedType = FullType.unspecified,
  }) {
    final result = EvalDebugLogBuilder();
    final serializedList = (serialized as Iterable<Object?>).toList();
    final unhandled = <Object?>[];
    _deserializeProperties(
      serializers,
      serialized,
      specifiedType: specifiedType,
      serializedList: serializedList,
      unhandled: unhandled,
      result: result,
    );
    return result.build();
  }
}

