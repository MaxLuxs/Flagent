//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:flagent_client/src/model/segment.dart';
import 'package:built_collection/built_collection.dart';
import 'package:flagent_client/src/model/variant.dart';
import 'package:flagent_client/src/model/tag.dart';
import 'package:built_value/built_value.dart';
import 'package:built_value/serializer.dart';

part 'flag.g.dart';

/// Flag
///
/// Properties:
/// * [id] 
/// * [key] - Unique key representation of the flag
/// * [description] 
/// * [enabled] 
/// * [snapshotID] 
/// * [dataRecordsEnabled] - Enabled data records will get data logging in the metrics pipeline
/// * [entityType] - It will override the entityType in the evaluation logs if it's not empty
/// * [notes] - Flag usage details in markdown format
/// * [createdBy] 
/// * [updatedBy] 
/// * [segments] 
/// * [variants] 
/// * [tags] 
@BuiltValue()
abstract class Flag implements Built<Flag, FlagBuilder> {
  @BuiltValueField(wireName: r'id')
  int get id;

  /// Unique key representation of the flag
  @BuiltValueField(wireName: r'key')
  String get key;

  @BuiltValueField(wireName: r'description')
  String get description;

  @BuiltValueField(wireName: r'enabled')
  bool get enabled;

  @BuiltValueField(wireName: r'snapshotID')
  int? get snapshotID;

  /// Enabled data records will get data logging in the metrics pipeline
  @BuiltValueField(wireName: r'dataRecordsEnabled')
  bool get dataRecordsEnabled;

  /// It will override the entityType in the evaluation logs if it's not empty
  @BuiltValueField(wireName: r'entityType')
  String? get entityType;

  /// Flag usage details in markdown format
  @BuiltValueField(wireName: r'notes')
  String? get notes;

  @BuiltValueField(wireName: r'createdBy')
  String? get createdBy;

  @BuiltValueField(wireName: r'updatedBy')
  String? get updatedBy;

  @BuiltValueField(wireName: r'segments')
  BuiltList<Segment>? get segments;

  @BuiltValueField(wireName: r'variants')
  BuiltList<Variant>? get variants;

  @BuiltValueField(wireName: r'tags')
  BuiltList<Tag>? get tags;

  Flag._();

  factory Flag([void updates(FlagBuilder b)]) = _$Flag;

  @BuiltValueHook(initializeBuilder: true)
  static void _defaults(FlagBuilder b) => b;

  @BuiltValueSerializer(custom: true)
  static Serializer<Flag> get serializer => _$FlagSerializer();
}

class _$FlagSerializer implements PrimitiveSerializer<Flag> {
  @override
  final Iterable<Type> types = const [Flag, _$Flag];

  @override
  final String wireName = r'Flag';

  Iterable<Object?> _serializeProperties(
    Serializers serializers,
    Flag object, {
    FullType specifiedType = FullType.unspecified,
  }) sync* {
    yield r'id';
    yield serializers.serialize(
      object.id,
      specifiedType: const FullType(int),
    );
    yield r'key';
    yield serializers.serialize(
      object.key,
      specifiedType: const FullType(String),
    );
    yield r'description';
    yield serializers.serialize(
      object.description,
      specifiedType: const FullType(String),
    );
    yield r'enabled';
    yield serializers.serialize(
      object.enabled,
      specifiedType: const FullType(bool),
    );
    if (object.snapshotID != null) {
      yield r'snapshotID';
      yield serializers.serialize(
        object.snapshotID,
        specifiedType: const FullType(int),
      );
    }
    yield r'dataRecordsEnabled';
    yield serializers.serialize(
      object.dataRecordsEnabled,
      specifiedType: const FullType(bool),
    );
    if (object.entityType != null) {
      yield r'entityType';
      yield serializers.serialize(
        object.entityType,
        specifiedType: const FullType.nullable(String),
      );
    }
    if (object.notes != null) {
      yield r'notes';
      yield serializers.serialize(
        object.notes,
        specifiedType: const FullType.nullable(String),
      );
    }
    if (object.createdBy != null) {
      yield r'createdBy';
      yield serializers.serialize(
        object.createdBy,
        specifiedType: const FullType.nullable(String),
      );
    }
    if (object.updatedBy != null) {
      yield r'updatedBy';
      yield serializers.serialize(
        object.updatedBy,
        specifiedType: const FullType.nullable(String),
      );
    }
    if (object.segments != null) {
      yield r'segments';
      yield serializers.serialize(
        object.segments,
        specifiedType: const FullType(BuiltList, [FullType(Segment)]),
      );
    }
    if (object.variants != null) {
      yield r'variants';
      yield serializers.serialize(
        object.variants,
        specifiedType: const FullType(BuiltList, [FullType(Variant)]),
      );
    }
    if (object.tags != null) {
      yield r'tags';
      yield serializers.serialize(
        object.tags,
        specifiedType: const FullType(BuiltList, [FullType(Tag)]),
      );
    }
  }

  @override
  Object serialize(
    Serializers serializers,
    Flag object, {
    FullType specifiedType = FullType.unspecified,
  }) {
    return _serializeProperties(serializers, object, specifiedType: specifiedType).toList();
  }

  void _deserializeProperties(
    Serializers serializers,
    Object serialized, {
    FullType specifiedType = FullType.unspecified,
    required List<Object?> serializedList,
    required FlagBuilder result,
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
        case r'key':
          final valueDes = serializers.deserialize(
            value,
            specifiedType: const FullType(String),
          ) as String;
          result.key = valueDes;
          break;
        case r'description':
          final valueDes = serializers.deserialize(
            value,
            specifiedType: const FullType(String),
          ) as String;
          result.description = valueDes;
          break;
        case r'enabled':
          final valueDes = serializers.deserialize(
            value,
            specifiedType: const FullType(bool),
          ) as bool;
          result.enabled = valueDes;
          break;
        case r'snapshotID':
          final valueDes = serializers.deserialize(
            value,
            specifiedType: const FullType(int),
          ) as int;
          result.snapshotID = valueDes;
          break;
        case r'dataRecordsEnabled':
          final valueDes = serializers.deserialize(
            value,
            specifiedType: const FullType(bool),
          ) as bool;
          result.dataRecordsEnabled = valueDes;
          break;
        case r'entityType':
          final valueDes = serializers.deserialize(
            value,
            specifiedType: const FullType.nullable(String),
          ) as String?;
          if (valueDes == null) continue;
          result.entityType = valueDes;
          break;
        case r'notes':
          final valueDes = serializers.deserialize(
            value,
            specifiedType: const FullType.nullable(String),
          ) as String?;
          if (valueDes == null) continue;
          result.notes = valueDes;
          break;
        case r'createdBy':
          final valueDes = serializers.deserialize(
            value,
            specifiedType: const FullType.nullable(String),
          ) as String?;
          if (valueDes == null) continue;
          result.createdBy = valueDes;
          break;
        case r'updatedBy':
          final valueDes = serializers.deserialize(
            value,
            specifiedType: const FullType.nullable(String),
          ) as String?;
          if (valueDes == null) continue;
          result.updatedBy = valueDes;
          break;
        case r'segments':
          final valueDes = serializers.deserialize(
            value,
            specifiedType: const FullType(BuiltList, [FullType(Segment)]),
          ) as BuiltList<Segment>;
          result.segments.replace(valueDes);
          break;
        case r'variants':
          final valueDes = serializers.deserialize(
            value,
            specifiedType: const FullType(BuiltList, [FullType(Variant)]),
          ) as BuiltList<Variant>;
          result.variants.replace(valueDes);
          break;
        case r'tags':
          final valueDes = serializers.deserialize(
            value,
            specifiedType: const FullType(BuiltList, [FullType(Tag)]),
          ) as BuiltList<Tag>;
          result.tags.replace(valueDes);
          break;
        default:
          unhandled.add(key);
          unhandled.add(value);
          break;
      }
    }
  }

  @override
  Flag deserialize(
    Serializers serializers,
    Object serialized, {
    FullType specifiedType = FullType.unspecified,
  }) {
    final result = FlagBuilder();
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

