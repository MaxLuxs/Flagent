//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:built_value/built_value.dart';
import 'package:built_value/serializer.dart';

part 'info.g.dart';

/// Info
///
/// Properties:
/// * [version] - Application version
/// * [buildTime] - Build timestamp
/// * [gitCommit] - Git commit hash
@BuiltValue()
abstract class Info implements Built<Info, InfoBuilder> {
  /// Application version
  @BuiltValueField(wireName: r'version')
  String? get version;

  /// Build timestamp
  @BuiltValueField(wireName: r'buildTime')
  String? get buildTime;

  /// Git commit hash
  @BuiltValueField(wireName: r'gitCommit')
  String? get gitCommit;

  Info._();

  factory Info([void updates(InfoBuilder b)]) = _$Info;

  @BuiltValueHook(initializeBuilder: true)
  static void _defaults(InfoBuilder b) => b;

  @BuiltValueSerializer(custom: true)
  static Serializer<Info> get serializer => _$InfoSerializer();
}

class _$InfoSerializer implements PrimitiveSerializer<Info> {
  @override
  final Iterable<Type> types = const [Info, _$Info];

  @override
  final String wireName = r'Info';

  Iterable<Object?> _serializeProperties(
    Serializers serializers,
    Info object, {
    FullType specifiedType = FullType.unspecified,
  }) sync* {
    if (object.version != null) {
      yield r'version';
      yield serializers.serialize(
        object.version,
        specifiedType: const FullType(String),
      );
    }
    if (object.buildTime != null) {
      yield r'buildTime';
      yield serializers.serialize(
        object.buildTime,
        specifiedType: const FullType.nullable(String),
      );
    }
    if (object.gitCommit != null) {
      yield r'gitCommit';
      yield serializers.serialize(
        object.gitCommit,
        specifiedType: const FullType.nullable(String),
      );
    }
  }

  @override
  Object serialize(
    Serializers serializers,
    Info object, {
    FullType specifiedType = FullType.unspecified,
  }) {
    return _serializeProperties(serializers, object, specifiedType: specifiedType).toList();
  }

  void _deserializeProperties(
    Serializers serializers,
    Object serialized, {
    FullType specifiedType = FullType.unspecified,
    required List<Object?> serializedList,
    required InfoBuilder result,
    required List<Object?> unhandled,
  }) {
    for (var i = 0; i < serializedList.length; i += 2) {
      final key = serializedList[i] as String;
      final value = serializedList[i + 1];
      switch (key) {
        case r'version':
          final valueDes = serializers.deserialize(
            value,
            specifiedType: const FullType(String),
          ) as String;
          result.version = valueDes;
          break;
        case r'buildTime':
          final valueDes = serializers.deserialize(
            value,
            specifiedType: const FullType.nullable(String),
          ) as String?;
          if (valueDes == null) continue;
          result.buildTime = valueDes;
          break;
        case r'gitCommit':
          final valueDes = serializers.deserialize(
            value,
            specifiedType: const FullType.nullable(String),
          ) as String?;
          if (valueDes == null) continue;
          result.gitCommit = valueDes;
          break;
        default:
          unhandled.add(key);
          unhandled.add(value);
          break;
      }
    }
  }

  @override
  Info deserialize(
    Serializers serializers,
    Object serialized, {
    FullType specifiedType = FullType.unspecified,
  }) {
    final result = InfoBuilder();
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

