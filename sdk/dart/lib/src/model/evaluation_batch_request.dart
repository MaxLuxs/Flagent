//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:built_collection/built_collection.dart';
import 'package:flagent_client/src/model/evaluation_entity.dart';
import 'package:built_value/built_value.dart';
import 'package:built_value/serializer.dart';

part 'evaluation_batch_request.g.dart';

/// EvaluationBatchRequest
///
/// Properties:
/// * [entities] 
/// * [enableDebug] 
/// * [flagIDs] - FlagIDs. Either flagIDs, flagKeys or flagTags works. If pass in multiples, Flagent may return duplicate results.
/// * [flagKeys] - FlagKeys. Either flagIDs, flagKeys or flagTags works. If pass in multiples, Flagent may return duplicate results.
/// * [flagTags] - FlagTags. Either flagIDs, flagKeys or flagTags works. If pass in multiples, Flagent may return duplicate results.
/// * [flagTagsOperator] - Determine how flagTags is used to filter flags to be evaluated.
@BuiltValue()
abstract class EvaluationBatchRequest implements Built<EvaluationBatchRequest, EvaluationBatchRequestBuilder> {
  @BuiltValueField(wireName: r'entities')
  BuiltList<EvaluationEntity> get entities;

  @BuiltValueField(wireName: r'enableDebug')
  bool? get enableDebug;

  /// FlagIDs. Either flagIDs, flagKeys or flagTags works. If pass in multiples, Flagent may return duplicate results.
  @BuiltValueField(wireName: r'flagIDs')
  BuiltList<int>? get flagIDs;

  /// FlagKeys. Either flagIDs, flagKeys or flagTags works. If pass in multiples, Flagent may return duplicate results.
  @BuiltValueField(wireName: r'flagKeys')
  BuiltList<String>? get flagKeys;

  /// FlagTags. Either flagIDs, flagKeys or flagTags works. If pass in multiples, Flagent may return duplicate results.
  @BuiltValueField(wireName: r'flagTags')
  BuiltList<String>? get flagTags;

  /// Determine how flagTags is used to filter flags to be evaluated.
  @BuiltValueField(wireName: r'flagTagsOperator')
  EvaluationBatchRequestFlagTagsOperatorEnum? get flagTagsOperator;
  // enum flagTagsOperatorEnum {  ANY,  ALL,  };

  EvaluationBatchRequest._();

  factory EvaluationBatchRequest([void updates(EvaluationBatchRequestBuilder b)]) = _$EvaluationBatchRequest;

  @BuiltValueHook(initializeBuilder: true)
  static void _defaults(EvaluationBatchRequestBuilder b) => b
      ..enableDebug = false
      ..flagTagsOperator = EvaluationBatchRequestFlagTagsOperatorEnum.valueOf('ANY');

  @BuiltValueSerializer(custom: true)
  static Serializer<EvaluationBatchRequest> get serializer => _$EvaluationBatchRequestSerializer();
}

class _$EvaluationBatchRequestSerializer implements PrimitiveSerializer<EvaluationBatchRequest> {
  @override
  final Iterable<Type> types = const [EvaluationBatchRequest, _$EvaluationBatchRequest];

  @override
  final String wireName = r'EvaluationBatchRequest';

  Iterable<Object?> _serializeProperties(
    Serializers serializers,
    EvaluationBatchRequest object, {
    FullType specifiedType = FullType.unspecified,
  }) sync* {
    yield r'entities';
    yield serializers.serialize(
      object.entities,
      specifiedType: const FullType(BuiltList, [FullType(EvaluationEntity)]),
    );
    if (object.enableDebug != null) {
      yield r'enableDebug';
      yield serializers.serialize(
        object.enableDebug,
        specifiedType: const FullType(bool),
      );
    }
    if (object.flagIDs != null) {
      yield r'flagIDs';
      yield serializers.serialize(
        object.flagIDs,
        specifiedType: const FullType.nullable(BuiltList, [FullType(int)]),
      );
    }
    if (object.flagKeys != null) {
      yield r'flagKeys';
      yield serializers.serialize(
        object.flagKeys,
        specifiedType: const FullType.nullable(BuiltList, [FullType(String)]),
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
        specifiedType: const FullType.nullable(EvaluationBatchRequestFlagTagsOperatorEnum),
      );
    }
  }

  @override
  Object serialize(
    Serializers serializers,
    EvaluationBatchRequest object, {
    FullType specifiedType = FullType.unspecified,
  }) {
    return _serializeProperties(serializers, object, specifiedType: specifiedType).toList();
  }

  void _deserializeProperties(
    Serializers serializers,
    Object serialized, {
    FullType specifiedType = FullType.unspecified,
    required List<Object?> serializedList,
    required EvaluationBatchRequestBuilder result,
    required List<Object?> unhandled,
  }) {
    for (var i = 0; i < serializedList.length; i += 2) {
      final key = serializedList[i] as String;
      final value = serializedList[i + 1];
      switch (key) {
        case r'entities':
          final valueDes = serializers.deserialize(
            value,
            specifiedType: const FullType(BuiltList, [FullType(EvaluationEntity)]),
          ) as BuiltList<EvaluationEntity>;
          result.entities.replace(valueDes);
          break;
        case r'enableDebug':
          final valueDes = serializers.deserialize(
            value,
            specifiedType: const FullType(bool),
          ) as bool;
          result.enableDebug = valueDes;
          break;
        case r'flagIDs':
          final valueDes = serializers.deserialize(
            value,
            specifiedType: const FullType.nullable(BuiltList, [FullType(int)]),
          ) as BuiltList<int>?;
          if (valueDes == null) continue;
          result.flagIDs.replace(valueDes);
          break;
        case r'flagKeys':
          final valueDes = serializers.deserialize(
            value,
            specifiedType: const FullType.nullable(BuiltList, [FullType(String)]),
          ) as BuiltList<String>?;
          if (valueDes == null) continue;
          result.flagKeys.replace(valueDes);
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
            specifiedType: const FullType.nullable(EvaluationBatchRequestFlagTagsOperatorEnum),
          ) as EvaluationBatchRequestFlagTagsOperatorEnum?;
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
  EvaluationBatchRequest deserialize(
    Serializers serializers,
    Object serialized, {
    FullType specifiedType = FullType.unspecified,
  }) {
    final result = EvaluationBatchRequestBuilder();
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

class EvaluationBatchRequestFlagTagsOperatorEnum extends EnumClass {

  /// Determine how flagTags is used to filter flags to be evaluated.
  @BuiltValueEnumConst(wireName: r'ANY')
  static const EvaluationBatchRequestFlagTagsOperatorEnum ANY = _$evaluationBatchRequestFlagTagsOperatorEnum_ANY;
  /// Determine how flagTags is used to filter flags to be evaluated.
  @BuiltValueEnumConst(wireName: r'ALL')
  static const EvaluationBatchRequestFlagTagsOperatorEnum ALL = _$evaluationBatchRequestFlagTagsOperatorEnum_ALL;

  static Serializer<EvaluationBatchRequestFlagTagsOperatorEnum> get serializer => _$evaluationBatchRequestFlagTagsOperatorEnumSerializer;

  const EvaluationBatchRequestFlagTagsOperatorEnum._(String name): super(name);

  static BuiltSet<EvaluationBatchRequestFlagTagsOperatorEnum> get values => _$evaluationBatchRequestFlagTagsOperatorEnumValues;
  static EvaluationBatchRequestFlagTagsOperatorEnum valueOf(String name) => _$evaluationBatchRequestFlagTagsOperatorEnumValueOf(name);
}

