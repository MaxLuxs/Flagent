//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:built_value/built_value.dart';
import 'package:built_value/serializer.dart';

part 'create_flag_request.g.dart';

/// CreateFlagRequest
///
/// Properties:
/// * [description] 
/// * [key] - Unique key representation of the flag
/// * [template] - Template for flag creation
@BuiltValue()
abstract class CreateFlagRequest implements Built<CreateFlagRequest, CreateFlagRequestBuilder> {
  @BuiltValueField(wireName: r'description')
  String get description;

  /// Unique key representation of the flag
  @BuiltValueField(wireName: r'key')
  String? get key;

  /// Template for flag creation
  @BuiltValueField(wireName: r'template')
  String? get template;

  CreateFlagRequest._();

  factory CreateFlagRequest([void updates(CreateFlagRequestBuilder b)]) = _$CreateFlagRequest;

  @BuiltValueHook(initializeBuilder: true)
  static void _defaults(CreateFlagRequestBuilder b) => b;

  @BuiltValueSerializer(custom: true)
  static Serializer<CreateFlagRequest> get serializer => _$CreateFlagRequestSerializer();
}

class _$CreateFlagRequestSerializer implements PrimitiveSerializer<CreateFlagRequest> {
  @override
  final Iterable<Type> types = const [CreateFlagRequest, _$CreateFlagRequest];

  @override
  final String wireName = r'CreateFlagRequest';

  Iterable<Object?> _serializeProperties(
    Serializers serializers,
    CreateFlagRequest object, {
    FullType specifiedType = FullType.unspecified,
  }) sync* {
    yield r'description';
    yield serializers.serialize(
      object.description,
      specifiedType: const FullType(String),
    );
    if (object.key != null) {
      yield r'key';
      yield serializers.serialize(
        object.key,
        specifiedType: const FullType.nullable(String),
      );
    }
    if (object.template != null) {
      yield r'template';
      yield serializers.serialize(
        object.template,
        specifiedType: const FullType.nullable(String),
      );
    }
  }

  @override
  Object serialize(
    Serializers serializers,
    CreateFlagRequest object, {
    FullType specifiedType = FullType.unspecified,
  }) {
    return _serializeProperties(serializers, object, specifiedType: specifiedType).toList();
  }

  void _deserializeProperties(
    Serializers serializers,
    Object serialized, {
    FullType specifiedType = FullType.unspecified,
    required List<Object?> serializedList,
    required CreateFlagRequestBuilder result,
    required List<Object?> unhandled,
  }) {
    for (var i = 0; i < serializedList.length; i += 2) {
      final key = serializedList[i] as String;
      final value = serializedList[i + 1];
      switch (key) {
        case r'description':
          final valueDes = serializers.deserialize(
            value,
            specifiedType: const FullType(String),
          ) as String;
          result.description = valueDes;
          break;
        case r'key':
          final valueDes = serializers.deserialize(
            value,
            specifiedType: const FullType.nullable(String),
          ) as String?;
          if (valueDes == null) continue;
          result.key = valueDes;
          break;
        case r'template':
          final valueDes = serializers.deserialize(
            value,
            specifiedType: const FullType.nullable(String),
          ) as String?;
          if (valueDes == null) continue;
          result.template = valueDes;
          break;
        default:
          unhandled.add(key);
          unhandled.add(value);
          break;
      }
    }
  }

  @override
  CreateFlagRequest deserialize(
    Serializers serializers,
    Object serialized, {
    FullType specifiedType = FullType.unspecified,
  }) {
    final result = CreateFlagRequestBuilder();
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

