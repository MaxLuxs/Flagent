//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:flagent_client/src/model/distribution.dart';
import 'package:built_collection/built_collection.dart';
import 'package:flagent_client/src/model/constraint.dart';
import 'package:built_value/built_value.dart';
import 'package:built_value/serializer.dart';

part 'segment.g.dart';

/// Segment
///
/// Properties:
/// * [id] 
/// * [flagID] 
/// * [description] 
/// * [rank] 
/// * [rolloutPercent] 
/// * [constraints] 
/// * [distributions] 
@BuiltValue()
abstract class Segment implements Built<Segment, SegmentBuilder> {
  @BuiltValueField(wireName: r'id')
  int get id;

  @BuiltValueField(wireName: r'flagID')
  int get flagID;

  @BuiltValueField(wireName: r'description')
  String get description;

  @BuiltValueField(wireName: r'rank')
  int get rank;

  @BuiltValueField(wireName: r'rolloutPercent')
  int get rolloutPercent;

  @BuiltValueField(wireName: r'constraints')
  BuiltList<Constraint>? get constraints;

  @BuiltValueField(wireName: r'distributions')
  BuiltList<Distribution>? get distributions;

  Segment._();

  factory Segment([void updates(SegmentBuilder b)]) = _$Segment;

  @BuiltValueHook(initializeBuilder: true)
  static void _defaults(SegmentBuilder b) => b;

  @BuiltValueSerializer(custom: true)
  static Serializer<Segment> get serializer => _$SegmentSerializer();
}

class _$SegmentSerializer implements PrimitiveSerializer<Segment> {
  @override
  final Iterable<Type> types = const [Segment, _$Segment];

  @override
  final String wireName = r'Segment';

  Iterable<Object?> _serializeProperties(
    Serializers serializers,
    Segment object, {
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
    yield r'description';
    yield serializers.serialize(
      object.description,
      specifiedType: const FullType(String),
    );
    yield r'rank';
    yield serializers.serialize(
      object.rank,
      specifiedType: const FullType(int),
    );
    yield r'rolloutPercent';
    yield serializers.serialize(
      object.rolloutPercent,
      specifiedType: const FullType(int),
    );
    if (object.constraints != null) {
      yield r'constraints';
      yield serializers.serialize(
        object.constraints,
        specifiedType: const FullType(BuiltList, [FullType(Constraint)]),
      );
    }
    if (object.distributions != null) {
      yield r'distributions';
      yield serializers.serialize(
        object.distributions,
        specifiedType: const FullType(BuiltList, [FullType(Distribution)]),
      );
    }
  }

  @override
  Object serialize(
    Serializers serializers,
    Segment object, {
    FullType specifiedType = FullType.unspecified,
  }) {
    return _serializeProperties(serializers, object, specifiedType: specifiedType).toList();
  }

  void _deserializeProperties(
    Serializers serializers,
    Object serialized, {
    FullType specifiedType = FullType.unspecified,
    required List<Object?> serializedList,
    required SegmentBuilder result,
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
        case r'description':
          final valueDes = serializers.deserialize(
            value,
            specifiedType: const FullType(String),
          ) as String;
          result.description = valueDes;
          break;
        case r'rank':
          final valueDes = serializers.deserialize(
            value,
            specifiedType: const FullType(int),
          ) as int;
          result.rank = valueDes;
          break;
        case r'rolloutPercent':
          final valueDes = serializers.deserialize(
            value,
            specifiedType: const FullType(int),
          ) as int;
          result.rolloutPercent = valueDes;
          break;
        case r'constraints':
          final valueDes = serializers.deserialize(
            value,
            specifiedType: const FullType(BuiltList, [FullType(Constraint)]),
          ) as BuiltList<Constraint>;
          result.constraints.replace(valueDes);
          break;
        case r'distributions':
          final valueDes = serializers.deserialize(
            value,
            specifiedType: const FullType(BuiltList, [FullType(Distribution)]),
          ) as BuiltList<Distribution>;
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
  Segment deserialize(
    Serializers serializers,
    Object serialized, {
    FullType specifiedType = FullType.unspecified,
  }) {
    final result = SegmentBuilder();
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

