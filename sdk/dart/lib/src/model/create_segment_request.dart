//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:built_value/built_value.dart';
import 'package:built_value/serializer.dart';

part 'create_segment_request.g.dart';

/// CreateSegmentRequest
///
/// Properties:
/// * [description] 
/// * [rolloutPercent] 
@BuiltValue()
abstract class CreateSegmentRequest implements Built<CreateSegmentRequest, CreateSegmentRequestBuilder> {
  @BuiltValueField(wireName: r'description')
  String get description;

  @BuiltValueField(wireName: r'rolloutPercent')
  int get rolloutPercent;

  CreateSegmentRequest._();

  factory CreateSegmentRequest([void updates(CreateSegmentRequestBuilder b)]) = _$CreateSegmentRequest;

  @BuiltValueHook(initializeBuilder: true)
  static void _defaults(CreateSegmentRequestBuilder b) => b;

  @BuiltValueSerializer(custom: true)
  static Serializer<CreateSegmentRequest> get serializer => _$CreateSegmentRequestSerializer();
}

class _$CreateSegmentRequestSerializer implements PrimitiveSerializer<CreateSegmentRequest> {
  @override
  final Iterable<Type> types = const [CreateSegmentRequest, _$CreateSegmentRequest];

  @override
  final String wireName = r'CreateSegmentRequest';

  Iterable<Object?> _serializeProperties(
    Serializers serializers,
    CreateSegmentRequest object, {
    FullType specifiedType = FullType.unspecified,
  }) sync* {
    yield r'description';
    yield serializers.serialize(
      object.description,
      specifiedType: const FullType(String),
    );
    yield r'rolloutPercent';
    yield serializers.serialize(
      object.rolloutPercent,
      specifiedType: const FullType(int),
    );
  }

  @override
  Object serialize(
    Serializers serializers,
    CreateSegmentRequest object, {
    FullType specifiedType = FullType.unspecified,
  }) {
    return _serializeProperties(serializers, object, specifiedType: specifiedType).toList();
  }

  void _deserializeProperties(
    Serializers serializers,
    Object serialized, {
    FullType specifiedType = FullType.unspecified,
    required List<Object?> serializedList,
    required CreateSegmentRequestBuilder result,
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
        case r'rolloutPercent':
          final valueDes = serializers.deserialize(
            value,
            specifiedType: const FullType(int),
          ) as int;
          result.rolloutPercent = valueDes;
          break;
        default:
          unhandled.add(key);
          unhandled.add(value);
          break;
      }
    }
  }

  @override
  CreateSegmentRequest deserialize(
    Serializers serializers,
    Object serialized, {
    FullType specifiedType = FullType.unspecified,
  }) {
    final result = CreateSegmentRequestBuilder();
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

