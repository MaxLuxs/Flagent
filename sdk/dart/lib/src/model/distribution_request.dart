//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:built_value/built_value.dart';
import 'package:built_value/serializer.dart';

part 'distribution_request.g.dart';

/// DistributionRequest
///
/// Properties:
/// * [variantID] 
/// * [variantKey] 
/// * [percent] 
@BuiltValue()
abstract class DistributionRequest implements Built<DistributionRequest, DistributionRequestBuilder> {
  @BuiltValueField(wireName: r'variantID')
  int get variantID;

  @BuiltValueField(wireName: r'variantKey')
  String? get variantKey;

  @BuiltValueField(wireName: r'percent')
  int get percent;

  DistributionRequest._();

  factory DistributionRequest([void updates(DistributionRequestBuilder b)]) = _$DistributionRequest;

  @BuiltValueHook(initializeBuilder: true)
  static void _defaults(DistributionRequestBuilder b) => b;

  @BuiltValueSerializer(custom: true)
  static Serializer<DistributionRequest> get serializer => _$DistributionRequestSerializer();
}

class _$DistributionRequestSerializer implements PrimitiveSerializer<DistributionRequest> {
  @override
  final Iterable<Type> types = const [DistributionRequest, _$DistributionRequest];

  @override
  final String wireName = r'DistributionRequest';

  Iterable<Object?> _serializeProperties(
    Serializers serializers,
    DistributionRequest object, {
    FullType specifiedType = FullType.unspecified,
  }) sync* {
    yield r'variantID';
    yield serializers.serialize(
      object.variantID,
      specifiedType: const FullType(int),
    );
    if (object.variantKey != null) {
      yield r'variantKey';
      yield serializers.serialize(
        object.variantKey,
        specifiedType: const FullType.nullable(String),
      );
    }
    yield r'percent';
    yield serializers.serialize(
      object.percent,
      specifiedType: const FullType(int),
    );
  }

  @override
  Object serialize(
    Serializers serializers,
    DistributionRequest object, {
    FullType specifiedType = FullType.unspecified,
  }) {
    return _serializeProperties(serializers, object, specifiedType: specifiedType).toList();
  }

  void _deserializeProperties(
    Serializers serializers,
    Object serialized, {
    FullType specifiedType = FullType.unspecified,
    required List<Object?> serializedList,
    required DistributionRequestBuilder result,
    required List<Object?> unhandled,
  }) {
    for (var i = 0; i < serializedList.length; i += 2) {
      final key = serializedList[i] as String;
      final value = serializedList[i + 1];
      switch (key) {
        case r'variantID':
          final valueDes = serializers.deserialize(
            value,
            specifiedType: const FullType(int),
          ) as int;
          result.variantID = valueDes;
          break;
        case r'variantKey':
          final valueDes = serializers.deserialize(
            value,
            specifiedType: const FullType.nullable(String),
          ) as String?;
          if (valueDes == null) continue;
          result.variantKey = valueDes;
          break;
        case r'percent':
          final valueDes = serializers.deserialize(
            value,
            specifiedType: const FullType(int),
          ) as int;
          result.percent = valueDes;
          break;
        default:
          unhandled.add(key);
          unhandled.add(value);
          break;
      }
    }
  }

  @override
  DistributionRequest deserialize(
    Serializers serializers,
    Object serialized, {
    FullType specifiedType = FullType.unspecified,
  }) {
    final result = DistributionRequestBuilder();
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

