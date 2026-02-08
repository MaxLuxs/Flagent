//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:built_collection/built_collection.dart';
import 'package:built_value/json_object.dart';
import 'package:built_value/built_value.dart';
import 'package:built_value/serializer.dart';

part 'put_variant_request.g.dart';

/// PutVariantRequest
///
/// Properties:
/// * [key] 
/// * [attachment] 
@BuiltValue()
abstract class PutVariantRequest implements Built<PutVariantRequest, PutVariantRequestBuilder> {
  @BuiltValueField(wireName: r'key')
  String get key;

  @BuiltValueField(wireName: r'attachment')
  BuiltMap<String, JsonObject?>? get attachment;

  PutVariantRequest._();

  factory PutVariantRequest([void updates(PutVariantRequestBuilder b)]) = _$PutVariantRequest;

  @BuiltValueHook(initializeBuilder: true)
  static void _defaults(PutVariantRequestBuilder b) => b;

  @BuiltValueSerializer(custom: true)
  static Serializer<PutVariantRequest> get serializer => _$PutVariantRequestSerializer();
}

class _$PutVariantRequestSerializer implements PrimitiveSerializer<PutVariantRequest> {
  @override
  final Iterable<Type> types = const [PutVariantRequest, _$PutVariantRequest];

  @override
  final String wireName = r'PutVariantRequest';

  Iterable<Object?> _serializeProperties(
    Serializers serializers,
    PutVariantRequest object, {
    FullType specifiedType = FullType.unspecified,
  }) sync* {
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
    PutVariantRequest object, {
    FullType specifiedType = FullType.unspecified,
  }) {
    return _serializeProperties(serializers, object, specifiedType: specifiedType).toList();
  }

  void _deserializeProperties(
    Serializers serializers,
    Object serialized, {
    FullType specifiedType = FullType.unspecified,
    required List<Object?> serializedList,
    required PutVariantRequestBuilder result,
    required List<Object?> unhandled,
  }) {
    for (var i = 0; i < serializedList.length; i += 2) {
      final key = serializedList[i] as String;
      final value = serializedList[i + 1];
      switch (key) {
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
  PutVariantRequest deserialize(
    Serializers serializers,
    Object serialized, {
    FullType specifiedType = FullType.unspecified,
  }) {
    final result = PutVariantRequestBuilder();
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

