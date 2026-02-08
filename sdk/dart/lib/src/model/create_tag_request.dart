//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:built_value/built_value.dart';
import 'package:built_value/serializer.dart';

part 'create_tag_request.g.dart';

/// CreateTagRequest
///
/// Properties:
/// * [value] 
@BuiltValue()
abstract class CreateTagRequest implements Built<CreateTagRequest, CreateTagRequestBuilder> {
  @BuiltValueField(wireName: r'value')
  String get value;

  CreateTagRequest._();

  factory CreateTagRequest([void updates(CreateTagRequestBuilder b)]) = _$CreateTagRequest;

  @BuiltValueHook(initializeBuilder: true)
  static void _defaults(CreateTagRequestBuilder b) => b;

  @BuiltValueSerializer(custom: true)
  static Serializer<CreateTagRequest> get serializer => _$CreateTagRequestSerializer();
}

class _$CreateTagRequestSerializer implements PrimitiveSerializer<CreateTagRequest> {
  @override
  final Iterable<Type> types = const [CreateTagRequest, _$CreateTagRequest];

  @override
  final String wireName = r'CreateTagRequest';

  Iterable<Object?> _serializeProperties(
    Serializers serializers,
    CreateTagRequest object, {
    FullType specifiedType = FullType.unspecified,
  }) sync* {
    yield r'value';
    yield serializers.serialize(
      object.value,
      specifiedType: const FullType(String),
    );
  }

  @override
  Object serialize(
    Serializers serializers,
    CreateTagRequest object, {
    FullType specifiedType = FullType.unspecified,
  }) {
    return _serializeProperties(serializers, object, specifiedType: specifiedType).toList();
  }

  void _deserializeProperties(
    Serializers serializers,
    Object serialized, {
    FullType specifiedType = FullType.unspecified,
    required List<Object?> serializedList,
    required CreateTagRequestBuilder result,
    required List<Object?> unhandled,
  }) {
    for (var i = 0; i < serializedList.length; i += 2) {
      final key = serializedList[i] as String;
      final value = serializedList[i + 1];
      switch (key) {
        case r'value':
          final valueDes = serializers.deserialize(
            value,
            specifiedType: const FullType(String),
          ) as String;
          result.value = valueDes;
          break;
        default:
          unhandled.add(key);
          unhandled.add(value);
          break;
      }
    }
  }

  @override
  CreateTagRequest deserialize(
    Serializers serializers,
    Object serialized, {
    FullType specifiedType = FullType.unspecified,
  }) {
    final result = CreateTagRequestBuilder();
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

