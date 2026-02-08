//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:built_value/built_value.dart';
import 'package:built_value/serializer.dart';

part 'create_constraint_request.g.dart';

/// CreateConstraintRequest
///
/// Properties:
/// * [property] 
/// * [operator_] 
/// * [value] 
@BuiltValue()
abstract class CreateConstraintRequest implements Built<CreateConstraintRequest, CreateConstraintRequestBuilder> {
  @BuiltValueField(wireName: r'property')
  String get property;

  @BuiltValueField(wireName: r'operator')
  String get operator_;

  @BuiltValueField(wireName: r'value')
  String get value;

  CreateConstraintRequest._();

  factory CreateConstraintRequest([void updates(CreateConstraintRequestBuilder b)]) = _$CreateConstraintRequest;

  @BuiltValueHook(initializeBuilder: true)
  static void _defaults(CreateConstraintRequestBuilder b) => b;

  @BuiltValueSerializer(custom: true)
  static Serializer<CreateConstraintRequest> get serializer => _$CreateConstraintRequestSerializer();
}

class _$CreateConstraintRequestSerializer implements PrimitiveSerializer<CreateConstraintRequest> {
  @override
  final Iterable<Type> types = const [CreateConstraintRequest, _$CreateConstraintRequest];

  @override
  final String wireName = r'CreateConstraintRequest';

  Iterable<Object?> _serializeProperties(
    Serializers serializers,
    CreateConstraintRequest object, {
    FullType specifiedType = FullType.unspecified,
  }) sync* {
    yield r'property';
    yield serializers.serialize(
      object.property,
      specifiedType: const FullType(String),
    );
    yield r'operator';
    yield serializers.serialize(
      object.operator_,
      specifiedType: const FullType(String),
    );
    yield r'value';
    yield serializers.serialize(
      object.value,
      specifiedType: const FullType(String),
    );
  }

  @override
  Object serialize(
    Serializers serializers,
    CreateConstraintRequest object, {
    FullType specifiedType = FullType.unspecified,
  }) {
    return _serializeProperties(serializers, object, specifiedType: specifiedType).toList();
  }

  void _deserializeProperties(
    Serializers serializers,
    Object serialized, {
    FullType specifiedType = FullType.unspecified,
    required List<Object?> serializedList,
    required CreateConstraintRequestBuilder result,
    required List<Object?> unhandled,
  }) {
    for (var i = 0; i < serializedList.length; i += 2) {
      final key = serializedList[i] as String;
      final value = serializedList[i + 1];
      switch (key) {
        case r'property':
          final valueDes = serializers.deserialize(
            value,
            specifiedType: const FullType(String),
          ) as String;
          result.property = valueDes;
          break;
        case r'operator':
          final valueDes = serializers.deserialize(
            value,
            specifiedType: const FullType(String),
          ) as String;
          result.operator_ = valueDes;
          break;
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
  CreateConstraintRequest deserialize(
    Serializers serializers,
    Object serialized, {
    FullType specifiedType = FullType.unspecified,
  }) {
    final result = CreateConstraintRequestBuilder();
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

