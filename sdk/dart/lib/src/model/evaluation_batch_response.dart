//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:flagent_client/src/model/eval_result.dart';
import 'package:built_collection/built_collection.dart';
import 'package:built_value/built_value.dart';
import 'package:built_value/serializer.dart';

part 'evaluation_batch_response.g.dart';

/// EvaluationBatchResponse
///
/// Properties:
/// * [evaluationResults] 
@BuiltValue()
abstract class EvaluationBatchResponse implements Built<EvaluationBatchResponse, EvaluationBatchResponseBuilder> {
  @BuiltValueField(wireName: r'evaluationResults')
  BuiltList<EvalResult> get evaluationResults;

  EvaluationBatchResponse._();

  factory EvaluationBatchResponse([void updates(EvaluationBatchResponseBuilder b)]) = _$EvaluationBatchResponse;

  @BuiltValueHook(initializeBuilder: true)
  static void _defaults(EvaluationBatchResponseBuilder b) => b;

  @BuiltValueSerializer(custom: true)
  static Serializer<EvaluationBatchResponse> get serializer => _$EvaluationBatchResponseSerializer();
}

class _$EvaluationBatchResponseSerializer implements PrimitiveSerializer<EvaluationBatchResponse> {
  @override
  final Iterable<Type> types = const [EvaluationBatchResponse, _$EvaluationBatchResponse];

  @override
  final String wireName = r'EvaluationBatchResponse';

  Iterable<Object?> _serializeProperties(
    Serializers serializers,
    EvaluationBatchResponse object, {
    FullType specifiedType = FullType.unspecified,
  }) sync* {
    yield r'evaluationResults';
    yield serializers.serialize(
      object.evaluationResults,
      specifiedType: const FullType(BuiltList, [FullType(EvalResult)]),
    );
  }

  @override
  Object serialize(
    Serializers serializers,
    EvaluationBatchResponse object, {
    FullType specifiedType = FullType.unspecified,
  }) {
    return _serializeProperties(serializers, object, specifiedType: specifiedType).toList();
  }

  void _deserializeProperties(
    Serializers serializers,
    Object serialized, {
    FullType specifiedType = FullType.unspecified,
    required List<Object?> serializedList,
    required EvaluationBatchResponseBuilder result,
    required List<Object?> unhandled,
  }) {
    for (var i = 0; i < serializedList.length; i += 2) {
      final key = serializedList[i] as String;
      final value = serializedList[i + 1];
      switch (key) {
        case r'evaluationResults':
          final valueDes = serializers.deserialize(
            value,
            specifiedType: const FullType(BuiltList, [FullType(EvalResult)]),
          ) as BuiltList<EvalResult>;
          result.evaluationResults.replace(valueDes);
          break;
        default:
          unhandled.add(key);
          unhandled.add(value);
          break;
      }
    }
  }

  @override
  EvaluationBatchResponse deserialize(
    Serializers serializers,
    Object serialized, {
    FullType specifiedType = FullType.unspecified,
  }) {
    final result = EvaluationBatchResponseBuilder();
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

