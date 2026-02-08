//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:built_value/built_value.dart';
import 'package:built_value/serializer.dart';

part 'put_segment_request.g.dart';

/// PutSegmentRequest
///
/// Properties:
/// * [description] 
/// * [rolloutPercent] 
@BuiltValue()
abstract class PutSegmentRequest implements Built<PutSegmentRequest, PutSegmentRequestBuilder> {
  @BuiltValueField(wireName: r'description')
  String get description;

  @BuiltValueField(wireName: r'rolloutPercent')
  int get rolloutPercent;

  PutSegmentRequest._();

  factory PutSegmentRequest([void updates(PutSegmentRequestBuilder b)]) = _$PutSegmentRequest;

  @BuiltValueHook(initializeBuilder: true)
  static void _defaults(PutSegmentRequestBuilder b) => b;

  @BuiltValueSerializer(custom: true)
  static Serializer<PutSegmentRequest> get serializer => _$PutSegmentRequestSerializer();
}

class _$PutSegmentRequestSerializer implements PrimitiveSerializer<PutSegmentRequest> {
  @override
  final Iterable<Type> types = const [PutSegmentRequest, _$PutSegmentRequest];

  @override
  final String wireName = r'PutSegmentRequest';

  Iterable<Object?> _serializeProperties(
    Serializers serializers,
    PutSegmentRequest object, {
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
    PutSegmentRequest object, {
    FullType specifiedType = FullType.unspecified,
  }) {
    return _serializeProperties(serializers, object, specifiedType: specifiedType).toList();
  }

  void _deserializeProperties(
    Serializers serializers,
    Object serialized, {
    FullType specifiedType = FullType.unspecified,
    required List<Object?> serializedList,
    required PutSegmentRequestBuilder result,
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
  PutSegmentRequest deserialize(
    Serializers serializers,
    Object serialized, {
    FullType specifiedType = FullType.unspecified,
  }) {
    final result = PutSegmentRequestBuilder();
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

