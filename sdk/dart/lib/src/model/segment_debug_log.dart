//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:built_value/built_value.dart';
import 'package:built_value/serializer.dart';

part 'segment_debug_log.g.dart';

/// SegmentDebugLog
///
/// Properties:
/// * [segmentID] 
/// * [msg] 
@BuiltValue()
abstract class SegmentDebugLog implements Built<SegmentDebugLog, SegmentDebugLogBuilder> {
  @BuiltValueField(wireName: r'segmentID')
  int? get segmentID;

  @BuiltValueField(wireName: r'msg')
  String? get msg;

  SegmentDebugLog._();

  factory SegmentDebugLog([void updates(SegmentDebugLogBuilder b)]) = _$SegmentDebugLog;

  @BuiltValueHook(initializeBuilder: true)
  static void _defaults(SegmentDebugLogBuilder b) => b;

  @BuiltValueSerializer(custom: true)
  static Serializer<SegmentDebugLog> get serializer => _$SegmentDebugLogSerializer();
}

class _$SegmentDebugLogSerializer implements PrimitiveSerializer<SegmentDebugLog> {
  @override
  final Iterable<Type> types = const [SegmentDebugLog, _$SegmentDebugLog];

  @override
  final String wireName = r'SegmentDebugLog';

  Iterable<Object?> _serializeProperties(
    Serializers serializers,
    SegmentDebugLog object, {
    FullType specifiedType = FullType.unspecified,
  }) sync* {
    if (object.segmentID != null) {
      yield r'segmentID';
      yield serializers.serialize(
        object.segmentID,
        specifiedType: const FullType(int),
      );
    }
    if (object.msg != null) {
      yield r'msg';
      yield serializers.serialize(
        object.msg,
        specifiedType: const FullType(String),
      );
    }
  }

  @override
  Object serialize(
    Serializers serializers,
    SegmentDebugLog object, {
    FullType specifiedType = FullType.unspecified,
  }) {
    return _serializeProperties(serializers, object, specifiedType: specifiedType).toList();
  }

  void _deserializeProperties(
    Serializers serializers,
    Object serialized, {
    FullType specifiedType = FullType.unspecified,
    required List<Object?> serializedList,
    required SegmentDebugLogBuilder result,
    required List<Object?> unhandled,
  }) {
    for (var i = 0; i < serializedList.length; i += 2) {
      final key = serializedList[i] as String;
      final value = serializedList[i + 1];
      switch (key) {
        case r'segmentID':
          final valueDes = serializers.deserialize(
            value,
            specifiedType: const FullType(int),
          ) as int;
          result.segmentID = valueDes;
          break;
        case r'msg':
          final valueDes = serializers.deserialize(
            value,
            specifiedType: const FullType(String),
          ) as String;
          result.msg = valueDes;
          break;
        default:
          unhandled.add(key);
          unhandled.add(value);
          break;
      }
    }
  }

  @override
  SegmentDebugLog deserialize(
    Serializers serializers,
    Object serialized, {
    FullType specifiedType = FullType.unspecified,
  }) {
    final result = SegmentDebugLogBuilder();
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

