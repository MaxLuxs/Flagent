// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'create_constraint_request.dart';

// **************************************************************************
// BuiltValueGenerator
// **************************************************************************

class _$CreateConstraintRequest extends CreateConstraintRequest {
  @override
  final String property;
  @override
  final String operator_;
  @override
  final String value;

  factory _$CreateConstraintRequest(
          [void Function(CreateConstraintRequestBuilder)? updates]) =>
      (CreateConstraintRequestBuilder()..update(updates))._build();

  _$CreateConstraintRequest._(
      {required this.property, required this.operator_, required this.value})
      : super._();
  @override
  CreateConstraintRequest rebuild(
          void Function(CreateConstraintRequestBuilder) updates) =>
      (toBuilder()..update(updates)).build();

  @override
  CreateConstraintRequestBuilder toBuilder() =>
      CreateConstraintRequestBuilder()..replace(this);

  @override
  bool operator ==(Object other) {
    if (identical(other, this)) return true;
    return other is CreateConstraintRequest &&
        property == other.property &&
        operator_ == other.operator_ &&
        value == other.value;
  }

  @override
  int get hashCode {
    var _$hash = 0;
    _$hash = $jc(_$hash, property.hashCode);
    _$hash = $jc(_$hash, operator_.hashCode);
    _$hash = $jc(_$hash, value.hashCode);
    _$hash = $jf(_$hash);
    return _$hash;
  }

  @override
  String toString() {
    return (newBuiltValueToStringHelper(r'CreateConstraintRequest')
          ..add('property', property)
          ..add('operator_', operator_)
          ..add('value', value))
        .toString();
  }
}

class CreateConstraintRequestBuilder
    implements
        Builder<CreateConstraintRequest, CreateConstraintRequestBuilder> {
  _$CreateConstraintRequest? _$v;

  String? _property;
  String? get property => _$this._property;
  set property(String? property) => _$this._property = property;

  String? _operator_;
  String? get operator_ => _$this._operator_;
  set operator_(String? operator_) => _$this._operator_ = operator_;

  String? _value;
  String? get value => _$this._value;
  set value(String? value) => _$this._value = value;

  CreateConstraintRequestBuilder() {
    CreateConstraintRequest._defaults(this);
  }

  CreateConstraintRequestBuilder get _$this {
    final $v = _$v;
    if ($v != null) {
      _property = $v.property;
      _operator_ = $v.operator_;
      _value = $v.value;
      _$v = null;
    }
    return this;
  }

  @override
  void replace(CreateConstraintRequest other) {
    _$v = other as _$CreateConstraintRequest;
  }

  @override
  void update(void Function(CreateConstraintRequestBuilder)? updates) {
    if (updates != null) updates(this);
  }

  @override
  CreateConstraintRequest build() => _build();

  _$CreateConstraintRequest _build() {
    final _$result = _$v ??
        _$CreateConstraintRequest._(
          property: BuiltValueNullFieldError.checkNotNull(
              property, r'CreateConstraintRequest', 'property'),
          operator_: BuiltValueNullFieldError.checkNotNull(
              operator_, r'CreateConstraintRequest', 'operator_'),
          value: BuiltValueNullFieldError.checkNotNull(
              value, r'CreateConstraintRequest', 'value'),
        );
    replace(_$result);
    return _$result;
  }
}

// ignore_for_file: deprecated_member_use_from_same_package,type=lint
