//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:built_value/built_value.dart';
import 'package:built_value/serializer.dart';

part 'put_flag_request.g.dart';

/// PutFlagRequest
///
/// Properties:
/// * [description] 
/// * [key] 
/// * [dataRecordsEnabled] - Enabled data records will get data logging in the metrics pipeline
/// * [entityType] - It will overwrite entityType into evaluation logs if it's not empty
/// * [notes] 
@BuiltValue()
abstract class PutFlagRequest implements Built<PutFlagRequest, PutFlagRequestBuilder> {
  @BuiltValueField(wireName: r'description')
  String? get description;

  @BuiltValueField(wireName: r'key')
  String? get key;

  /// Enabled data records will get data logging in the metrics pipeline
  @BuiltValueField(wireName: r'dataRecordsEnabled')
  bool? get dataRecordsEnabled;

  /// It will overwrite entityType into evaluation logs if it's not empty
  @BuiltValueField(wireName: r'entityType')
  String? get entityType;

  @BuiltValueField(wireName: r'notes')
  String? get notes;

  PutFlagRequest._();

  factory PutFlagRequest([void updates(PutFlagRequestBuilder b)]) = _$PutFlagRequest;

  @BuiltValueHook(initializeBuilder: true)
  static void _defaults(PutFlagRequestBuilder b) => b;

  @BuiltValueSerializer(custom: true)
  static Serializer<PutFlagRequest> get serializer => _$PutFlagRequestSerializer();
}

class _$PutFlagRequestSerializer implements PrimitiveSerializer<PutFlagRequest> {
  @override
  final Iterable<Type> types = const [PutFlagRequest, _$PutFlagRequest];

  @override
  final String wireName = r'PutFlagRequest';

  Iterable<Object?> _serializeProperties(
    Serializers serializers,
    PutFlagRequest object, {
    FullType specifiedType = FullType.unspecified,
  }) sync* {
    if (object.description != null) {
      yield r'description';
      yield serializers.serialize(
        object.description,
        specifiedType: const FullType.nullable(String),
      );
    }
    if (object.key != null) {
      yield r'key';
      yield serializers.serialize(
        object.key,
        specifiedType: const FullType.nullable(String),
      );
    }
    if (object.dataRecordsEnabled != null) {
      yield r'dataRecordsEnabled';
      yield serializers.serialize(
        object.dataRecordsEnabled,
        specifiedType: const FullType.nullable(bool),
      );
    }
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
  }

  @override
  Object serialize(
    Serializers serializers,
    PutFlagRequest object, {
    FullType specifiedType = FullType.unspecified,
  }) {
    return _serializeProperties(serializers, object, specifiedType: specifiedType).toList();
  }

  void _deserializeProperties(
    Serializers serializers,
    Object serialized, {
    FullType specifiedType = FullType.unspecified,
    required List<Object?> serializedList,
    required PutFlagRequestBuilder result,
    required List<Object?> unhandled,
  }) {
    for (var i = 0; i < serializedList.length; i += 2) {
      final key = serializedList[i] as String;
      final value = serializedList[i + 1];
      switch (key) {
        case r'description':
          final valueDes = serializers.deserialize(
            value,
            specifiedType: const FullType.nullable(String),
          ) as String?;
          if (valueDes == null) continue;
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
        case r'dataRecordsEnabled':
          final valueDes = serializers.deserialize(
            value,
            specifiedType: const FullType.nullable(bool),
          ) as bool?;
          if (valueDes == null) continue;
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
        default:
          unhandled.add(key);
          unhandled.add(value);
          break;
      }
    }
  }

  @override
  PutFlagRequest deserialize(
    Serializers serializers,
    Object serialized, {
    FullType specifiedType = FullType.unspecified,
  }) {
    final result = PutFlagRequestBuilder();
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

