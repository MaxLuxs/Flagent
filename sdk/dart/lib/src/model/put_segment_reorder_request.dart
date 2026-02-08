//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:built_collection/built_collection.dart';
import 'package:built_value/built_value.dart';
import 'package:built_value/serializer.dart';

part 'put_segment_reorder_request.g.dart';

/// PutSegmentReorderRequest
///
/// Properties:
/// * [segmentIDs] 
@BuiltValue()
abstract class PutSegmentReorderRequest implements Built<PutSegmentReorderRequest, PutSegmentReorderRequestBuilder> {
  @BuiltValueField(wireName: r'segmentIDs')
  BuiltList<int> get segmentIDs;

  PutSegmentReorderRequest._();

  factory PutSegmentReorderRequest([void updates(PutSegmentReorderRequestBuilder b)]) = _$PutSegmentReorderRequest;

  @BuiltValueHook(initializeBuilder: true)
  static void _defaults(PutSegmentReorderRequestBuilder b) => b;

  @BuiltValueSerializer(custom: true)
  static Serializer<PutSegmentReorderRequest> get serializer => _$PutSegmentReorderRequestSerializer();
}

class _$PutSegmentReorderRequestSerializer implements PrimitiveSerializer<PutSegmentReorderRequest> {
  @override
  final Iterable<Type> types = const [PutSegmentReorderRequest, _$PutSegmentReorderRequest];

  @override
  final String wireName = r'PutSegmentReorderRequest';

  Iterable<Object?> _serializeProperties(
    Serializers serializers,
    PutSegmentReorderRequest object, {
    FullType specifiedType = FullType.unspecified,
  }) sync* {
    yield r'segmentIDs';
    yield serializers.serialize(
      object.segmentIDs,
      specifiedType: const FullType(BuiltList, [FullType(int)]),
    );
  }

  @override
  Object serialize(
    Serializers serializers,
    PutSegmentReorderRequest object, {
    FullType specifiedType = FullType.unspecified,
  }) {
    return _serializeProperties(serializers, object, specifiedType: specifiedType).toList();
  }

  void _deserializeProperties(
    Serializers serializers,
    Object serialized, {
    FullType specifiedType = FullType.unspecified,
    required List<Object?> serializedList,
    required PutSegmentReorderRequestBuilder result,
    required List<Object?> unhandled,
  }) {
    for (var i = 0; i < serializedList.length; i += 2) {
      final key = serializedList[i] as String;
      final value = serializedList[i + 1];
      switch (key) {
        case r'segmentIDs':
          final valueDes = serializers.deserialize(
            value,
            specifiedType: const FullType(BuiltList, [FullType(int)]),
          ) as BuiltList<int>;
          result.segmentIDs.replace(valueDes);
          break;
        default:
          unhandled.add(key);
          unhandled.add(value);
          break;
      }
    }
  }

  @override
  PutSegmentReorderRequest deserialize(
    Serializers serializers,
    Object serialized, {
    FullType specifiedType = FullType.unspecified,
  }) {
    final result = PutSegmentReorderRequestBuilder();
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

