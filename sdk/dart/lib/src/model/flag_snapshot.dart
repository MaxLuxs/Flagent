//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:flagent_client/src/model/flag.dart';
import 'package:built_value/built_value.dart';
import 'package:built_value/serializer.dart';

part 'flag_snapshot.g.dart';

/// FlagSnapshot
///
/// Properties:
/// * [id] 
/// * [updatedBy] 
/// * [flag] 
/// * [updatedAt] 
@BuiltValue()
abstract class FlagSnapshot implements Built<FlagSnapshot, FlagSnapshotBuilder> {
  @BuiltValueField(wireName: r'id')
  int get id;

  @BuiltValueField(wireName: r'updatedBy')
  String? get updatedBy;

  @BuiltValueField(wireName: r'flag')
  Flag get flag;

  @BuiltValueField(wireName: r'updatedAt')
  DateTime get updatedAt;

  FlagSnapshot._();

  factory FlagSnapshot([void updates(FlagSnapshotBuilder b)]) = _$FlagSnapshot;

  @BuiltValueHook(initializeBuilder: true)
  static void _defaults(FlagSnapshotBuilder b) => b;

  @BuiltValueSerializer(custom: true)
  static Serializer<FlagSnapshot> get serializer => _$FlagSnapshotSerializer();
}

class _$FlagSnapshotSerializer implements PrimitiveSerializer<FlagSnapshot> {
  @override
  final Iterable<Type> types = const [FlagSnapshot, _$FlagSnapshot];

  @override
  final String wireName = r'FlagSnapshot';

  Iterable<Object?> _serializeProperties(
    Serializers serializers,
    FlagSnapshot object, {
    FullType specifiedType = FullType.unspecified,
  }) sync* {
    yield r'id';
    yield serializers.serialize(
      object.id,
      specifiedType: const FullType(int),
    );
    if (object.updatedBy != null) {
      yield r'updatedBy';
      yield serializers.serialize(
        object.updatedBy,
        specifiedType: const FullType.nullable(String),
      );
    }
    yield r'flag';
    yield serializers.serialize(
      object.flag,
      specifiedType: const FullType(Flag),
    );
    yield r'updatedAt';
    yield serializers.serialize(
      object.updatedAt,
      specifiedType: const FullType(DateTime),
    );
  }

  @override
  Object serialize(
    Serializers serializers,
    FlagSnapshot object, {
    FullType specifiedType = FullType.unspecified,
  }) {
    return _serializeProperties(serializers, object, specifiedType: specifiedType).toList();
  }

  void _deserializeProperties(
    Serializers serializers,
    Object serialized, {
    FullType specifiedType = FullType.unspecified,
    required List<Object?> serializedList,
    required FlagSnapshotBuilder result,
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
        case r'updatedBy':
          final valueDes = serializers.deserialize(
            value,
            specifiedType: const FullType.nullable(String),
          ) as String?;
          if (valueDes == null) continue;
          result.updatedBy = valueDes;
          break;
        case r'flag':
          final valueDes = serializers.deserialize(
            value,
            specifiedType: const FullType(Flag),
          ) as Flag;
          result.flag.replace(valueDes);
          break;
        case r'updatedAt':
          final valueDes = serializers.deserialize(
            value,
            specifiedType: const FullType(DateTime),
          ) as DateTime;
          result.updatedAt = valueDes;
          break;
        default:
          unhandled.add(key);
          unhandled.add(value);
          break;
      }
    }
  }

  @override
  FlagSnapshot deserialize(
    Serializers serializers,
    Object serialized, {
    FullType specifiedType = FullType.unspecified,
  }) {
    final result = FlagSnapshotBuilder();
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

