//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:built_collection/built_collection.dart';
import 'package:flagent_client/src/model/eval_debug_log.dart';
import 'package:flagent_client/src/model/eval_context.dart';
import 'package:built_value/json_object.dart';
import 'package:built_value/built_value.dart';
import 'package:built_value/serializer.dart';

part 'eval_result.g.dart';

/// EvalResult
///
/// Properties:
/// * [flagID] 
/// * [flagKey] 
/// * [flagSnapshotID] 
/// * [flagTags] 
/// * [segmentID] 
/// * [variantID] 
/// * [variantKey] 
/// * [variantAttachment] 
/// * [evalContext] 
/// * [timestamp] 
/// * [evalDebugLog] 
@BuiltValue()
abstract class EvalResult implements Built<EvalResult, EvalResultBuilder> {
  @BuiltValueField(wireName: r'flagID')
  int? get flagID;

  @BuiltValueField(wireName: r'flagKey')
  String? get flagKey;

  @BuiltValueField(wireName: r'flagSnapshotID')
  int? get flagSnapshotID;

  @BuiltValueField(wireName: r'flagTags')
  BuiltList<String>? get flagTags;

  @BuiltValueField(wireName: r'segmentID')
  int? get segmentID;

  @BuiltValueField(wireName: r'variantID')
  int? get variantID;

  @BuiltValueField(wireName: r'variantKey')
  String? get variantKey;

  @BuiltValueField(wireName: r'variantAttachment')
  BuiltMap<String, JsonObject?>? get variantAttachment;

  @BuiltValueField(wireName: r'evalContext')
  EvalContext? get evalContext;

  @BuiltValueField(wireName: r'timestamp')
  DateTime? get timestamp;

  @BuiltValueField(wireName: r'evalDebugLog')
  EvalDebugLog? get evalDebugLog;

  EvalResult._();

  factory EvalResult([void updates(EvalResultBuilder b)]) = _$EvalResult;

  @BuiltValueHook(initializeBuilder: true)
  static void _defaults(EvalResultBuilder b) => b;

  @BuiltValueSerializer(custom: true)
  static Serializer<EvalResult> get serializer => _$EvalResultSerializer();
}

class _$EvalResultSerializer implements PrimitiveSerializer<EvalResult> {
  @override
  final Iterable<Type> types = const [EvalResult, _$EvalResult];

  @override
  final String wireName = r'EvalResult';

  Iterable<Object?> _serializeProperties(
    Serializers serializers,
    EvalResult object, {
    FullType specifiedType = FullType.unspecified,
  }) sync* {
    if (object.flagID != null) {
      yield r'flagID';
      yield serializers.serialize(
        object.flagID,
        specifiedType: const FullType(int),
      );
    }
    if (object.flagKey != null) {
      yield r'flagKey';
      yield serializers.serialize(
        object.flagKey,
        specifiedType: const FullType(String),
      );
    }
    if (object.flagSnapshotID != null) {
      yield r'flagSnapshotID';
      yield serializers.serialize(
        object.flagSnapshotID,
        specifiedType: const FullType(int),
      );
    }
    if (object.flagTags != null) {
      yield r'flagTags';
      yield serializers.serialize(
        object.flagTags,
        specifiedType: const FullType.nullable(BuiltList, [FullType(String)]),
      );
    }
    if (object.segmentID != null) {
      yield r'segmentID';
      yield serializers.serialize(
        object.segmentID,
        specifiedType: const FullType.nullable(int),
      );
    }
    if (object.variantID != null) {
      yield r'variantID';
      yield serializers.serialize(
        object.variantID,
        specifiedType: const FullType.nullable(int),
      );
    }
    if (object.variantKey != null) {
      yield r'variantKey';
      yield serializers.serialize(
        object.variantKey,
        specifiedType: const FullType.nullable(String),
      );
    }
    if (object.variantAttachment != null) {
      yield r'variantAttachment';
      yield serializers.serialize(
        object.variantAttachment,
        specifiedType: const FullType.nullable(BuiltMap, [FullType(String), FullType.nullable(JsonObject)]),
      );
    }
    if (object.evalContext != null) {
      yield r'evalContext';
      yield serializers.serialize(
        object.evalContext,
        specifiedType: const FullType(EvalContext),
      );
    }
    if (object.timestamp != null) {
      yield r'timestamp';
      yield serializers.serialize(
        object.timestamp,
        specifiedType: const FullType(DateTime),
      );
    }
    if (object.evalDebugLog != null) {
      yield r'evalDebugLog';
      yield serializers.serialize(
        object.evalDebugLog,
        specifiedType: const FullType(EvalDebugLog),
      );
    }
  }

  @override
  Object serialize(
    Serializers serializers,
    EvalResult object, {
    FullType specifiedType = FullType.unspecified,
  }) {
    return _serializeProperties(serializers, object, specifiedType: specifiedType).toList();
  }

  void _deserializeProperties(
    Serializers serializers,
    Object serialized, {
    FullType specifiedType = FullType.unspecified,
    required List<Object?> serializedList,
    required EvalResultBuilder result,
    required List<Object?> unhandled,
  }) {
    for (var i = 0; i < serializedList.length; i += 2) {
      final key = serializedList[i] as String;
      final value = serializedList[i + 1];
      switch (key) {
        case r'flagID':
          final valueDes = serializers.deserialize(
            value,
            specifiedType: const FullType(int),
          ) as int;
          result.flagID = valueDes;
          break;
        case r'flagKey':
          final valueDes = serializers.deserialize(
            value,
            specifiedType: const FullType(String),
          ) as String;
          result.flagKey = valueDes;
          break;
        case r'flagSnapshotID':
          final valueDes = serializers.deserialize(
            value,
            specifiedType: const FullType(int),
          ) as int;
          result.flagSnapshotID = valueDes;
          break;
        case r'flagTags':
          final valueDes = serializers.deserialize(
            value,
            specifiedType: const FullType.nullable(BuiltList, [FullType(String)]),
          ) as BuiltList<String>?;
          if (valueDes == null) continue;
          result.flagTags.replace(valueDes);
          break;
        case r'segmentID':
          final valueDes = serializers.deserialize(
            value,
            specifiedType: const FullType.nullable(int),
          ) as int?;
          if (valueDes == null) continue;
          result.segmentID = valueDes;
          break;
        case r'variantID':
          final valueDes = serializers.deserialize(
            value,
            specifiedType: const FullType.nullable(int),
          ) as int?;
          if (valueDes == null) continue;
          result.variantID = valueDes;
          break;
        case r'variantKey':
          final valueDes = serializers.deserialize(
            value,
            specifiedType: const FullType.nullable(String),
          ) as String?;
          if (valueDes == null) continue;
          result.variantKey = valueDes;
          break;
        case r'variantAttachment':
          final valueDes = serializers.deserialize(
            value,
            specifiedType: const FullType.nullable(BuiltMap, [FullType(String), FullType.nullable(JsonObject)]),
          ) as BuiltMap<String, JsonObject?>?;
          if (valueDes == null) continue;
          result.variantAttachment.replace(valueDes);
          break;
        case r'evalContext':
          final valueDes = serializers.deserialize(
            value,
            specifiedType: const FullType(EvalContext),
          ) as EvalContext;
          result.evalContext.replace(valueDes);
          break;
        case r'timestamp':
          final valueDes = serializers.deserialize(
            value,
            specifiedType: const FullType(DateTime),
          ) as DateTime;
          result.timestamp = valueDes;
          break;
        case r'evalDebugLog':
          final valueDes = serializers.deserialize(
            value,
            specifiedType: const FullType(EvalDebugLog),
          ) as EvalDebugLog;
          result.evalDebugLog.replace(valueDes);
          break;
        default:
          unhandled.add(key);
          unhandled.add(value);
          break;
      }
    }
  }

  @override
  EvalResult deserialize(
    Serializers serializers,
    Object serialized, {
    FullType specifiedType = FullType.unspecified,
  }) {
    final result = EvalResultBuilder();
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

