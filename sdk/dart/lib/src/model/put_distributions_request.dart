//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:flagent_client/src/model/distribution_request.dart';
import 'package:built_collection/built_collection.dart';
import 'package:built_value/built_value.dart';
import 'package:built_value/serializer.dart';

part 'put_distributions_request.g.dart';

/// PutDistributionsRequest
///
/// Properties:
/// * [distributions] 
@BuiltValue()
abstract class PutDistributionsRequest implements Built<PutDistributionsRequest, PutDistributionsRequestBuilder> {
  @BuiltValueField(wireName: r'distributions')
  BuiltList<DistributionRequest> get distributions;

  PutDistributionsRequest._();

  factory PutDistributionsRequest([void updates(PutDistributionsRequestBuilder b)]) = _$PutDistributionsRequest;

  @BuiltValueHook(initializeBuilder: true)
  static void _defaults(PutDistributionsRequestBuilder b) => b;

  @BuiltValueSerializer(custom: true)
  static Serializer<PutDistributionsRequest> get serializer => _$PutDistributionsRequestSerializer();
}

class _$PutDistributionsRequestSerializer implements PrimitiveSerializer<PutDistributionsRequest> {
  @override
  final Iterable<Type> types = const [PutDistributionsRequest, _$PutDistributionsRequest];

  @override
  final String wireName = r'PutDistributionsRequest';

  Iterable<Object?> _serializeProperties(
    Serializers serializers,
    PutDistributionsRequest object, {
    FullType specifiedType = FullType.unspecified,
  }) sync* {
    yield r'distributions';
    yield serializers.serialize(
      object.distributions,
      specifiedType: const FullType(BuiltList, [FullType(DistributionRequest)]),
    );
  }

  @override
  Object serialize(
    Serializers serializers,
    PutDistributionsRequest object, {
    FullType specifiedType = FullType.unspecified,
  }) {
    return _serializeProperties(serializers, object, specifiedType: specifiedType).toList();
  }

  void _deserializeProperties(
    Serializers serializers,
    Object serialized, {
    FullType specifiedType = FullType.unspecified,
    required List<Object?> serializedList,
    required PutDistributionsRequestBuilder result,
    required List<Object?> unhandled,
  }) {
    for (var i = 0; i < serializedList.length; i += 2) {
      final key = serializedList[i] as String;
      final value = serializedList[i + 1];
      switch (key) {
        case r'distributions':
          final valueDes = serializers.deserialize(
            value,
            specifiedType: const FullType(BuiltList, [FullType(DistributionRequest)]),
          ) as BuiltList<DistributionRequest>;
          result.distributions.replace(valueDes);
          break;
        default:
          unhandled.add(key);
          unhandled.add(value);
          break;
      }
    }
  }

  @override
  PutDistributionsRequest deserialize(
    Serializers serializers,
    Object serialized, {
    FullType specifiedType = FullType.unspecified,
  }) {
    final result = PutDistributionsRequestBuilder();
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

