//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:built_collection/built_collection.dart';
import 'package:built_value/json_object.dart';
import 'package:built_value/built_value.dart';
import 'package:built_value/serializer.dart';

part 'evaluation_entity.g.dart';

/// EvaluationEntity
///
/// Properties:
/// * [entityID] 
/// * [entityType] 
/// * [entityContext] 
@BuiltValue()
abstract class EvaluationEntity implements Built<EvaluationEntity, EvaluationEntityBuilder> {
  @BuiltValueField(wireName: r'entityID')
  String? get entityID;

  @BuiltValueField(wireName: r'entityType')
  String? get entityType;

  @BuiltValueField(wireName: r'entityContext')
  BuiltMap<String, JsonObject?>? get entityContext;

  EvaluationEntity._();

  factory EvaluationEntity([void updates(EvaluationEntityBuilder b)]) = _$EvaluationEntity;

  @BuiltValueHook(initializeBuilder: true)
  static void _defaults(EvaluationEntityBuilder b) => b;

  @BuiltValueSerializer(custom: true)
  static Serializer<EvaluationEntity> get serializer => _$EvaluationEntitySerializer();
}

class _$EvaluationEntitySerializer implements PrimitiveSerializer<EvaluationEntity> {
  @override
  final Iterable<Type> types = const [EvaluationEntity, _$EvaluationEntity];

  @override
  final String wireName = r'EvaluationEntity';

  Iterable<Object?> _serializeProperties(
    Serializers serializers,
    EvaluationEntity object, {
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
  }

  @override
  Object serialize(
    Serializers serializers,
    EvaluationEntity object, {
    FullType specifiedType = FullType.unspecified,
  }) {
    return _serializeProperties(serializers, object, specifiedType: specifiedType).toList();
  }

  void _deserializeProperties(
    Serializers serializers,
    Object serialized, {
    FullType specifiedType = FullType.unspecified,
    required List<Object?> serializedList,
    required EvaluationEntityBuilder result,
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
        default:
          unhandled.add(key);
          unhandled.add(value);
          break;
      }
    }
  }

  @override
  EvaluationEntity deserialize(
    Serializers serializers,
    Object serialized, {
    FullType specifiedType = FullType.unspecified,
  }) {
    final result = EvaluationEntityBuilder();
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

