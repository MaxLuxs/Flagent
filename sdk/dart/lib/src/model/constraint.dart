//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:built_collection/built_collection.dart';
import 'package:built_value/built_value.dart';
import 'package:built_value/serializer.dart';

part 'constraint.g.dart';

/// Constraint
///
/// Properties:
/// * [id] 
/// * [segmentID] 
/// * [property] 
/// * [operator_] 
/// * [value] 
@BuiltValue()
abstract class Constraint implements Built<Constraint, ConstraintBuilder> {
  @BuiltValueField(wireName: r'id')
  int get id;

  @BuiltValueField(wireName: r'segmentID')
  int get segmentID;

  @BuiltValueField(wireName: r'property')
  String get property;

  @BuiltValueField(wireName: r'operator')
  ConstraintOperator_Enum get operator_;
  // enum operator_Enum {  EQ,  NEQ,  LT,  LTE,  GT,  GTE,  EREG,  NEREG,  IN,  NOTIN,  CONTAINS,  NOTCONTAINS,  };

  @BuiltValueField(wireName: r'value')
  String get value;

  Constraint._();

  factory Constraint([void updates(ConstraintBuilder b)]) = _$Constraint;

  @BuiltValueHook(initializeBuilder: true)
  static void _defaults(ConstraintBuilder b) => b;

  @BuiltValueSerializer(custom: true)
  static Serializer<Constraint> get serializer => _$ConstraintSerializer();
}

class _$ConstraintSerializer implements PrimitiveSerializer<Constraint> {
  @override
  final Iterable<Type> types = const [Constraint, _$Constraint];

  @override
  final String wireName = r'Constraint';

  Iterable<Object?> _serializeProperties(
    Serializers serializers,
    Constraint object, {
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
    yield r'property';
    yield serializers.serialize(
      object.property,
      specifiedType: const FullType(String),
    );
    yield r'operator';
    yield serializers.serialize(
      object.operator_,
      specifiedType: const FullType(ConstraintOperator_Enum),
    );
    yield r'value';
    yield serializers.serialize(
      object.value,
      specifiedType: const FullType(String),
    );
  }

  @override
  Object serialize(
    Serializers serializers,
    Constraint object, {
    FullType specifiedType = FullType.unspecified,
  }) {
    return _serializeProperties(serializers, object, specifiedType: specifiedType).toList();
  }

  void _deserializeProperties(
    Serializers serializers,
    Object serialized, {
    FullType specifiedType = FullType.unspecified,
    required List<Object?> serializedList,
    required ConstraintBuilder result,
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
        case r'property':
          final valueDes = serializers.deserialize(
            value,
            specifiedType: const FullType(String),
          ) as String;
          result.property = valueDes;
          break;
        case r'operator':
          final valueDes = serializers.deserialize(
            value,
            specifiedType: const FullType(ConstraintOperator_Enum),
          ) as ConstraintOperator_Enum;
          result.operator_ = valueDes;
          break;
        case r'value':
          final valueDes = serializers.deserialize(
            value,
            specifiedType: const FullType(String),
          ) as String;
          result.value = valueDes;
          break;
        default:
          unhandled.add(key);
          unhandled.add(value);
          break;
      }
    }
  }

  @override
  Constraint deserialize(
    Serializers serializers,
    Object serialized, {
    FullType specifiedType = FullType.unspecified,
  }) {
    final result = ConstraintBuilder();
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

class ConstraintOperator_Enum extends EnumClass {

  @BuiltValueEnumConst(wireName: r'EQ')
  static const ConstraintOperator_Enum EQ = _$constraintOperatorEnum_EQ;
  @BuiltValueEnumConst(wireName: r'NEQ')
  static const ConstraintOperator_Enum NEQ = _$constraintOperatorEnum_NEQ;
  @BuiltValueEnumConst(wireName: r'LT')
  static const ConstraintOperator_Enum LT = _$constraintOperatorEnum_LT;
  @BuiltValueEnumConst(wireName: r'LTE')
  static const ConstraintOperator_Enum LTE = _$constraintOperatorEnum_LTE;
  @BuiltValueEnumConst(wireName: r'GT')
  static const ConstraintOperator_Enum GT = _$constraintOperatorEnum_GT;
  @BuiltValueEnumConst(wireName: r'GTE')
  static const ConstraintOperator_Enum GTE = _$constraintOperatorEnum_GTE;
  @BuiltValueEnumConst(wireName: r'EREG')
  static const ConstraintOperator_Enum EREG = _$constraintOperatorEnum_EREG;
  @BuiltValueEnumConst(wireName: r'NEREG')
  static const ConstraintOperator_Enum NEREG = _$constraintOperatorEnum_NEREG;
  @BuiltValueEnumConst(wireName: r'IN')
  static const ConstraintOperator_Enum IN = _$constraintOperatorEnum_IN;
  @BuiltValueEnumConst(wireName: r'NOTIN')
  static const ConstraintOperator_Enum NOTIN = _$constraintOperatorEnum_NOTIN;
  @BuiltValueEnumConst(wireName: r'CONTAINS')
  static const ConstraintOperator_Enum CONTAINS = _$constraintOperatorEnum_CONTAINS;
  @BuiltValueEnumConst(wireName: r'NOTCONTAINS')
  static const ConstraintOperator_Enum NOTCONTAINS = _$constraintOperatorEnum_NOTCONTAINS;

  static Serializer<ConstraintOperator_Enum> get serializer => _$constraintOperatorEnumSerializer;

  const ConstraintOperator_Enum._(String name): super(name);

  static BuiltSet<ConstraintOperator_Enum> get values => _$constraintOperatorEnumValues;
  static ConstraintOperator_Enum valueOf(String name) => _$constraintOperatorEnumValueOf(name);
}

