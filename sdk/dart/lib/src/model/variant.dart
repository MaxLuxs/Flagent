//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:built_collection/built_collection.dart';
import 'package:built_value/json_object.dart';
import 'package:built_value/built_value.dart';
import 'package:built_value/serializer.dart';

part 'variant.g.dart';

/// Variant
///
/// Properties:
/// * [id] 
/// * [flagID] 
/// * [key] 
/// * [attachment] 
@BuiltValue()
abstract class Variant implements Built<Variant, VariantBuilder> {
  @BuiltValueField(wireName: r'id')
  int get id;

  @BuiltValueField(wireName: r'flagID')
  int get flagID;

  @BuiltValueField(wireName: r'key')
  String get key;

  @BuiltValueField(wireName: r'attachment')
  BuiltMap<String, JsonObject?>? get attachment;

  Variant._();

  factory Variant([void updates(VariantBuilder b)]) = _$Variant;

  @BuiltValueHook(initializeBuilder: true)
  static void _defaults(VariantBuilder b) => b;

  @BuiltValueSerializer(custom: true)
  static Serializer<Variant> get serializer => _$VariantSerializer();
}

class _$VariantSerializer implements PrimitiveSerializer<Variant> {
  @override
  final Iterable<Type> types = const [Variant, _$Variant];

  @override
  final String wireName = r'Variant';

  Iterable<Object?> _serializeProperties(
    Serializers serializers,
    Variant object, {
    FullType specifiedType = FullType.unspecified,
  }) sync* {
    yield r'id';
    yield serializers.serialize(
      object.id,
      specifiedType: const FullType(int),
    );
    yield r'flagID';
    yield serializers.serialize(
      object.flagID,
      specifiedType: const FullType(int),
    );
    yield r'key';
    yield serializers.serialize(
      object.key,
      specifiedType: const FullType(String),
    );
    if (object.attachment != null) {
      yield r'attachment';
      yield serializers.serialize(
        object.attachment,
        specifiedType: const FullType.nullable(BuiltMap, [FullType(String), FullType.nullable(JsonObject)]),
      );
    }
  }

  @override
  Object serialize(
    Serializers serializers,
    Variant object, {
    FullType specifiedType = FullType.unspecified,
  }) {
    return _serializeProperties(serializers, object, specifiedType: specifiedType).toList();
  }

  void _deserializeProperties(
    Serializers serializers,
    Object serialized, {
    FullType specifiedType = FullType.unspecified,
    required List<Object?> serializedList,
    required VariantBuilder result,
    required List<Object?> unhandled,
  }) {
    for (var i = 0; i < serializedList.length; i += 2) {
      final key = serializedList[i] as String;
      final value = serializedList[i + 1];
      switch (key) {
        case r'id':
          final valueDes = serializers.deserialize(
            value,
            specifiedType: const FullType(int),
          ) as int;
          result.id = valueDes;
          break;
        case r'flagID':
          final valueDes = serializers.deserialize(
            value,
            specifiedType: const FullType(int),
          ) as int;
          result.flagID = valueDes;
          break;
        case r'key':
          final valueDes = serializers.deserialize(
            value,
            specifiedType: const FullType(String),
          ) as String;
          result.key = valueDes;
          break;
        case r'attachment':
          final valueDes = serializers.deserialize(
            value,
            specifiedType: const FullType.nullable(BuiltMap, [FullType(String), FullType.nullable(JsonObject)]),
          ) as BuiltMap<String, JsonObject?>?;
          if (valueDes == null) continue;
          result.attachment.replace(valueDes);
          break;
        default:
          unhandled.add(key);
          unhandled.add(value);
          break;
      }
    }
  }

  @override
  Variant deserialize(
    Serializers serializers,
    Object serialized, {
    FullType specifiedType = FullType.unspecified,
  }) {
    final result = VariantBuilder();
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

