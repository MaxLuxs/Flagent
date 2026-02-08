//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:built_value/built_value.dart';
import 'package:built_value/serializer.dart';

part 'distribution.g.dart';

/// Distribution
///
/// Properties:
/// * [id] 
/// * [segmentID] 
/// * [variantID] 
/// * [variantKey] 
/// * [percent] 
@BuiltValue()
abstract class Distribution implements Built<Distribution, DistributionBuilder> {
  @BuiltValueField(wireName: r'id')
  int get id;

  @BuiltValueField(wireName: r'segmentID')
  int get segmentID;

  @BuiltValueField(wireName: r'variantID')
  int get variantID;

  @BuiltValueField(wireName: r'variantKey')
  String? get variantKey;

  @BuiltValueField(wireName: r'percent')
  int get percent;

  Distribution._();

  factory Distribution([void updates(DistributionBuilder b)]) = _$Distribution;

  @BuiltValueHook(initializeBuilder: true)
  static void _defaults(DistributionBuilder b) => b;

  @BuiltValueSerializer(custom: true)
  static Serializer<Distribution> get serializer => _$DistributionSerializer();
}

class _$DistributionSerializer implements PrimitiveSerializer<Distribution> {
  @override
  final Iterable<Type> types = const [Distribution, _$Distribution];

  @override
  final String wireName = r'Distribution';

  Iterable<Object?> _serializeProperties(
    Serializers serializers,
    Distribution object, {
    FullType specifiedType = FullType.unspecified,
  }) sync* {
    yield r'id';
    yield serializers.serialize(
      object.id,
      specifiedType: const FullType(int),
    );
    yield r'segmentID';
    yield serializers.serialize(
      object.segmentID,
      specifiedType: const FullType(int),
    );
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
    Distribution object, {
    FullType specifiedType = FullType.unspecified,
  }) {
    return _serializeProperties(serializers, object, specifiedType: specifiedType).toList();
  }

  void _deserializeProperties(
    Serializers serializers,
    Object serialized, {
    FullType specifiedType = FullType.unspecified,
    required List<Object?> serializedList,
    required DistributionBuilder result,
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
        case r'segmentID':
          final valueDes = serializers.deserialize(
            value,
            specifiedType: const FullType(int),
          ) as int;
          result.segmentID = valueDes;
          break;
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
  Distribution deserialize(
    Serializers serializers,
    Object serialized, {
    FullType specifiedType = FullType.unspecified,
  }) {
    final result = DistributionBuilder();
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

