//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:built_collection/built_collection.dart';
import 'package:built_value/json_object.dart';
import 'package:built_value/built_value.dart';
import 'package:built_value/serializer.dart';

part 'eval_context.g.dart';

/// EvalContext
///
/// Properties:
/// * [entityID] - EntityID is used to deterministically at random to evaluate the flag result. If it's empty, Flagent will randomly generate one.
/// * [entityType] 
/// * [entityContext] 
/// * [enableDebug] 
/// * [flagID] - FlagID. flagID or flagKey will resolve to the same flag. Either works.
/// * [flagKey] - FlagKey. flagID or flagKey will resolve to the same flag. Either works.
/// * [flagTags] - FlagTags. flagTags looks up flags by tag. Either works.
/// * [flagTagsOperator] - Determine how flagTags is used to filter flags to be evaluated. OR extends the evaluation to those which contains at least one of the provided flagTags or AND limit the evaluation to those which contains all the flagTags.
@BuiltValue()
abstract class EvalContext implements Built<EvalContext, EvalContextBuilder> {
  /// EntityID is used to deterministically at random to evaluate the flag result. If it's empty, Flagent will randomly generate one.
  @BuiltValueField(wireName: r'entityID')
  String? get entityID;

  @BuiltValueField(wireName: r'entityType')
  String? get entityType;

  @BuiltValueField(wireName: r'entityContext')
  BuiltMap<String, JsonObject?>? get entityContext;

  @BuiltValueField(wireName: r'enableDebug')
  bool? get enableDebug;

  /// FlagID. flagID or flagKey will resolve to the same flag. Either works.
  @BuiltValueField(wireName: r'flagID')
  int? get flagID;

  /// FlagKey. flagID or flagKey will resolve to the same flag. Either works.
  @BuiltValueField(wireName: r'flagKey')
  String? get flagKey;

  /// FlagTags. flagTags looks up flags by tag. Either works.
  @BuiltValueField(wireName: r'flagTags')
  BuiltList<String>? get flagTags;

  /// Determine how flagTags is used to filter flags to be evaluated. OR extends the evaluation to those which contains at least one of the provided flagTags or AND limit the evaluation to those which contains all the flagTags.
  @BuiltValueField(wireName: r'flagTagsOperator')
  EvalContextFlagTagsOperatorEnum? get flagTagsOperator;
  // enum flagTagsOperatorEnum {  ANY,  ALL,  };

  EvalContext._();

  factory EvalContext([void updates(EvalContextBuilder b)]) = _$EvalContext;

  @BuiltValueHook(initializeBuilder: true)
  static void _defaults(EvalContextBuilder b) => b
      ..enableDebug = false
      ..flagTagsOperator = EvalContextFlagTagsOperatorEnum.valueOf('ANY');

  @BuiltValueSerializer(custom: true)
  static Serializer<EvalContext> get serializer => _$EvalContextSerializer();
}

class _$EvalContextSerializer implements PrimitiveSerializer<EvalContext> {
  @override
  final Iterable<Type> types = const [EvalContext, _$EvalContext];

  @override
  final String wireName = r'EvalContext';

  Iterable<Object?> _serializeProperties(
    Serializers serializers,
    EvalContext object, {
    FullType specifiedType = FullType.unspecified,
  }) sync* {
    if (object.entityID != null) {
      yield r'entityID';
      yield serializers.serialize(
        object.entityID,
        specifiedType: const FullType(String),
      );
    }
    if (object.entityType != null) {
      yield r'entityType';
      yield serializers.serialize(
        object.entityType,
        specifiedType: const FullType(String),
      );
    }
    if (object.entityContext != null) {
      yield r'entityContext';
      yield serializers.serialize(
        object.entityContext,
        specifiedType: const FullType.nullable(BuiltMap, [FullType(String), FullType.nullable(JsonObject)]),
      );
    }
    if (object.enableDebug != null) {
      yield r'enableDebug';
      yield serializers.serialize(
        object.enableDebug,
        specifiedType: const FullType(bool),
      );
    }
    if (object.flagID != null) {
      yield r'flagID';
      yield serializers.serialize(
        object.flagID,
        specifiedType: const FullType.nullable(int),
      );
    }
    if (object.flagKey != null) {
      yield r'flagKey';
      yield serializers.serialize(
        object.flagKey,
        specifiedType: const FullType.nullable(String),
      );
    }
    if (object.flagTags != null) {
      yield r'flagTags';
      yield serializers.serialize(
        object.flagTags,
        specifiedType: const FullType.nullable(BuiltList, [FullType(String)]),
      );
    }
    if (object.flagTagsOperator != null) {
      yield r'flagTagsOperator';
      yield serializers.serialize(
        object.flagTagsOperator,
        specifiedType: const FullType.nullable(EvalContextFlagTagsOperatorEnum),
      );
    }
  }

  @override
  Object serialize(
    Serializers serializers,
    EvalContext object, {
    FullType specifiedType = FullType.unspecified,
  }) {
    return _serializeProperties(serializers, object, specifiedType: specifiedType).toList();
  }

  void _deserializeProperties(
    Serializers serializers,
    Object serialized, {
    FullType specifiedType = FullType.unspecified,
    required List<Object?> serializedList,
    required EvalContextBuilder result,
    required List<Object?> unhandled,
  }) {
    for (var i = 0; i < serializedList.length; i += 2) {
      final key = serializedList[i] as String;
      final value = serializedList[i + 1];
      switch (key) {
        case r'entityID':
          final valueDes = serializers.deserialize(
            value,
            specifiedType: const FullType(String),
          ) as String;
          result.entityID = valueDes;
          break;
        case r'entityType':
          final valueDes = serializers.deserialize(
            value,
            specifiedType: const FullType(String),
          ) as String;
          result.entityType = valueDes;
          break;
        case r'entityContext':
          final valueDes = serializers.deserialize(
            value,
            specifiedType: const FullType.nullable(BuiltMap, [FullType(String), FullType.nullable(JsonObject)]),
          ) as BuiltMap<String, JsonObject?>?;
          if (valueDes == null) continue;
          result.entityContext.replace(valueDes);
          break;
        case r'enableDebug':
          final valueDes = serializers.deserialize(
            value,
            specifiedType: const FullType(bool),
          ) as bool;
          result.enableDebug = valueDes;
          break;
        case r'flagID':
          final valueDes = serializers.deserialize(
            value,
            specifiedType: const FullType.nullable(int),
          ) as int?;
          if (valueDes == null) continue;
          result.flagID = valueDes;
          break;
        case r'flagKey':
          final valueDes = serializers.deserialize(
            value,
            specifiedType: const FullType.nullable(String),
          ) as String?;
          if (valueDes == null) continue;
          result.flagKey = valueDes;
          break;
        case r'flagTags':
          final valueDes = serializers.deserialize(
            value,
            specifiedType: const FullType.nullable(BuiltList, [FullType(String)]),
          ) as BuiltList<String>?;
          if (valueDes == null) continue;
          result.flagTags.replace(valueDes);
          break;
        case r'flagTagsOperator':
          final valueDes = serializers.deserialize(
            value,
            specifiedType: const FullType.nullable(EvalContextFlagTagsOperatorEnum),
          ) as EvalContextFlagTagsOperatorEnum?;
          if (valueDes == null) continue;
          result.flagTagsOperator = valueDes;
          break;
        default:
          unhandled.add(key);
          unhandled.add(value);
          break;
      }
    }
  }

  @override
  EvalContext deserialize(
    Serializers serializers,
    Object serialized, {
    FullType specifiedType = FullType.unspecified,
  }) {
    final result = EvalContextBuilder();
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

class EvalContextFlagTagsOperatorEnum extends EnumClass {

  /// Determine how flagTags is used to filter flags to be evaluated. OR extends the evaluation to those which contains at least one of the provided flagTags or AND limit the evaluation to those which contains all the flagTags.
  @BuiltValueEnumConst(wireName: r'ANY')
  static const EvalContextFlagTagsOperatorEnum ANY = _$evalContextFlagTagsOperatorEnum_ANY;
  /// Determine how flagTags is used to filter flags to be evaluated. OR extends the evaluation to those which contains at least one of the provided flagTags or AND limit the evaluation to those which contains all the flagTags.
  @BuiltValueEnumConst(wireName: r'ALL')
  static const EvalContextFlagTagsOperatorEnum ALL = _$evalContextFlagTagsOperatorEnum_ALL;

  static Serializer<EvalContextFlagTagsOperatorEnum> get serializer => _$evalContextFlagTagsOperatorEnumSerializer;

  const EvalContextFlagTagsOperatorEnum._(String name): super(name);

  static BuiltSet<EvalContextFlagTagsOperatorEnum> get values => _$evalContextFlagTagsOperatorEnumValues;
  static EvalContextFlagTagsOperatorEnum valueOf(String name) => _$evalContextFlagTagsOperatorEnumValueOf(name);
}

