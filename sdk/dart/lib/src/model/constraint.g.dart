// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'constraint.dart';

// **************************************************************************
// BuiltValueGenerator
// **************************************************************************

const ConstraintOperator_Enum _$constraintOperatorEnum_EQ =
    const ConstraintOperator_Enum._('EQ');
const ConstraintOperator_Enum _$constraintOperatorEnum_NEQ =
    const ConstraintOperator_Enum._('NEQ');
const ConstraintOperator_Enum _$constraintOperatorEnum_LT =
    const ConstraintOperator_Enum._('LT');
const ConstraintOperator_Enum _$constraintOperatorEnum_LTE =
    const ConstraintOperator_Enum._('LTE');
const ConstraintOperator_Enum _$constraintOperatorEnum_GT =
    const ConstraintOperator_Enum._('GT');
const ConstraintOperator_Enum _$constraintOperatorEnum_GTE =
    const ConstraintOperator_Enum._('GTE');
const ConstraintOperator_Enum _$constraintOperatorEnum_EREG =
    const ConstraintOperator_Enum._('EREG');
const ConstraintOperator_Enum _$constraintOperatorEnum_NEREG =
    const ConstraintOperator_Enum._('NEREG');
const ConstraintOperator_Enum _$constraintOperatorEnum_IN =
    const ConstraintOperator_Enum._('IN');
const ConstraintOperator_Enum _$constraintOperatorEnum_NOTIN =
    const ConstraintOperator_Enum._('NOTIN');
const ConstraintOperator_Enum _$constraintOperatorEnum_CONTAINS =
    const ConstraintOperator_Enum._('CONTAINS');
const ConstraintOperator_Enum _$constraintOperatorEnum_NOTCONTAINS =
    const ConstraintOperator_Enum._('NOTCONTAINS');

ConstraintOperator_Enum _$constraintOperatorEnumValueOf(String name) {
  switch (name) {
    case 'EQ':
      return _$constraintOperatorEnum_EQ;
    case 'NEQ':
      return _$constraintOperatorEnum_NEQ;
    case 'LT':
      return _$constraintOperatorEnum_LT;
    case 'LTE':
      return _$constraintOperatorEnum_LTE;
    case 'GT':
      return _$constraintOperatorEnum_GT;
    case 'GTE':
      return _$constraintOperatorEnum_GTE;
    case 'EREG':
      return _$constraintOperatorEnum_EREG;
    case 'NEREG':
      return _$constraintOperatorEnum_NEREG;
    case 'IN':
      return _$constraintOperatorEnum_IN;
    case 'NOTIN':
      return _$constraintOperatorEnum_NOTIN;
    case 'CONTAINS':
      return _$constraintOperatorEnum_CONTAINS;
    case 'NOTCONTAINS':
      return _$constraintOperatorEnum_NOTCONTAINS;
    default:
      throw ArgumentError(name);
  }
}

final BuiltSet<ConstraintOperator_Enum> _$constraintOperatorEnumValues =
    BuiltSet<ConstraintOperator_Enum>(const <ConstraintOperator_Enum>[
  _$constraintOperatorEnum_EQ,
  _$constraintOperatorEnum_NEQ,
  _$constraintOperatorEnum_LT,
  _$constraintOperatorEnum_LTE,
  _$constraintOperatorEnum_GT,
  _$constraintOperatorEnum_GTE,
  _$constraintOperatorEnum_EREG,
  _$constraintOperatorEnum_NEREG,
  _$constraintOperatorEnum_IN,
  _$constraintOperatorEnum_NOTIN,
  _$constraintOperatorEnum_CONTAINS,
  _$constraintOperatorEnum_NOTCONTAINS,
]);

Serializer<ConstraintOperator_Enum> _$constraintOperatorEnumSerializer =
    _$ConstraintOperator_EnumSerializer();

class _$ConstraintOperator_EnumSerializer
    implements PrimitiveSerializer<ConstraintOperator_Enum> {
  static const Map<String, Object> _toWire = const <String, Object>{
    'EQ': 'EQ',
    'NEQ': 'NEQ',
    'LT': 'LT',
    'LTE': 'LTE',
    'GT': 'GT',
    'GTE': 'GTE',
    'EREG': 'EREG',
    'NEREG': 'NEREG',
    'IN': 'IN',
    'NOTIN': 'NOTIN',
    'CONTAINS': 'CONTAINS',
    'NOTCONTAINS': 'NOTCONTAINS',
  };
  static const Map<Object, String> _fromWire = const <Object, String>{
    'EQ': 'EQ',
    'NEQ': 'NEQ',
    'LT': 'LT',
    'LTE': 'LTE',
    'GT': 'GT',
    'GTE': 'GTE',
    'EREG': 'EREG',
    'NEREG': 'NEREG',
    'IN': 'IN',
    'NOTIN': 'NOTIN',
    'CONTAINS': 'CONTAINS',
    'NOTCONTAINS': 'NOTCONTAINS',
  };

  @override
  final Iterable<Type> types = const <Type>[ConstraintOperator_Enum];
  @override
  final String wireName = 'ConstraintOperator_Enum';

  @override
  Object serialize(Serializers serializers, ConstraintOperator_Enum object,
          {FullType specifiedType = FullType.unspecified}) =>
      _toWire[object.name] ?? object.name;

  @override
  ConstraintOperator_Enum deserialize(
          Serializers serializers, Object serialized,
          {FullType specifiedType = FullType.unspecified}) =>
      ConstraintOperator_Enum.valueOf(
          _fromWire[serialized] ?? (serialized is String ? serialized : ''));
}

class _$Constraint extends Constraint {
  @override
  final int id;
  @override
  final int segmentID;
  @override
  final String property;
  @override
  final ConstraintOperator_Enum operator_;
  @override
  final String value;

  factory _$Constraint([void Function(ConstraintBuilder)? updates]) =>
      (ConstraintBuilder()..update(updates))._build();

  _$Constraint._(
      {required this.id,
      required this.segmentID,
      required this.property,
      required this.operator_,
      required this.value})
      : super._();
  @override
  Constraint rebuild(void Function(ConstraintBuilder) updates) =>
      (toBuilder()..update(updates)).build();

  @override
  ConstraintBuilder toBuilder() => ConstraintBuilder()..replace(this);

  @override
  bool operator ==(Object other) {
    if (identical(other, this)) return true;
    return other is Constraint &&
        id == other.id &&
        segmentID == other.segmentID &&
        property == other.property &&
        operator_ == other.operator_ &&
        value == other.value;
  }

  @override
  int get hashCode {
    var _$hash = 0;
    _$hash = $jc(_$hash, id.hashCode);
    _$hash = $jc(_$hash, segmentID.hashCode);
    _$hash = $jc(_$hash, property.hashCode);
    _$hash = $jc(_$hash, operator_.hashCode);
    _$hash = $jc(_$hash, value.hashCode);
    _$hash = $jf(_$hash);
    return _$hash;
  }

  @override
  String toString() {
    return (newBuiltValueToStringHelper(r'Constraint')
          ..add('id', id)
          ..add('segmentID', segmentID)
          ..add('property', property)
          ..add('operator_', operator_)
          ..add('value', value))
        .toString();
  }
}

class ConstraintBuilder implements Builder<Constraint, ConstraintBuilder> {
  _$Constraint? _$v;

  int? _id;
  int? get id => _$this._id;
  set id(int? id) => _$this._id = id;

  int? _segmentID;
  int? get segmentID => _$this._segmentID;
  set segmentID(int? segmentID) => _$this._segmentID = segmentID;

  String? _property;
  String? get property => _$this._property;
  set property(String? property) => _$this._property = property;

  ConstraintOperator_Enum? _operator_;
  ConstraintOperator_Enum? get operator_ => _$this._operator_;
  set operator_(ConstraintOperator_Enum? operator_) =>
      _$this._operator_ = operator_;

  String? _value;
  String? get value => _$this._value;
  set value(String? value) => _$this._value = value;

  ConstraintBuilder() {
    Constraint._defaults(this);
  }

  ConstraintBuilder get _$this {
    final $v = _$v;
    if ($v != null) {
      _id = $v.id;
      _segmentID = $v.segmentID;
      _property = $v.property;
      _operator_ = $v.operator_;
      _value = $v.value;
      _$v = null;
    }
    return this;
  }

  @override
  void replace(Constraint other) {
    _$v = other as _$Constraint;
  }

  @override
  void update(void Function(ConstraintBuilder)? updates) {
    if (updates != null) updates(this);
  }

  @override
  Constraint build() => _build();

  _$Constraint _build() {
    final _$result = _$v ??
        _$Constraint._(
          id: BuiltValueNullFieldError.checkNotNull(id, r'Constraint', 'id'),
          segmentID: BuiltValueNullFieldError.checkNotNull(
              segmentID, r'Constraint', 'segmentID'),
          property: BuiltValueNullFieldError.checkNotNull(
              property, r'Constraint', 'property'),
          operator_: BuiltValueNullFieldError.checkNotNull(
              operator_, r'Constraint', 'operator_'),
          value: BuiltValueNullFieldError.checkNotNull(
              value, r'Constraint', 'value'),
        );
    replace(_$result);
    return _$result;
  }
}

// ignore_for_file: deprecated_member_use_from_same_package,type=lint
