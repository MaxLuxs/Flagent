// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'put_constraint_request.dart';

// **************************************************************************
// BuiltValueGenerator
// **************************************************************************

class _$PutConstraintRequest extends PutConstraintRequest {
  @override
  final String property;
  @override
  final String operator_;
  @override
  final String value;

  factory _$PutConstraintRequest(
          [void Function(PutConstraintRequestBuilder)? updates]) =>
      (PutConstraintRequestBuilder()..update(updates))._build();

  _$PutConstraintRequest._(
      {required this.property, required this.operator_, required this.value})
      : super._();
  @override
  PutConstraintRequest rebuild(
          void Function(PutConstraintRequestBuilder) updates) =>
      (toBuilder()..update(updates)).build();

  @override
  PutConstraintRequestBuilder toBuilder() =>
      PutConstraintRequestBuilder()..replace(this);

  @override
  bool operator ==(Object other) {
    if (identical(other, this)) return true;
    return other is PutConstraintRequest &&
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
    return (newBuiltValueToStringHelper(r'PutConstraintRequest')
          ..add('property', property)
          ..add('operator_', operator_)
          ..add('value', value))
        .toString();
  }
}

class PutConstraintRequestBuilder
    implements Builder<PutConstraintRequest, PutConstraintRequestBuilder> {
  _$PutConstraintRequest? _$v;

  String? _property;
  String? get property => _$this._property;
  set property(String? property) => _$this._property = property;

  String? _operator_;
  String? get operator_ => _$this._operator_;
  set operator_(String? operator_) => _$this._operator_ = operator_;

  String? _value;
  String? get value => _$this._value;
  set value(String? value) => _$this._value = value;

  PutConstraintRequestBuilder() {
    PutConstraintRequest._defaults(this);
  }

  PutConstraintRequestBuilder get _$this {
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
  void replace(PutConstraintRequest other) {
    _$v = other as _$PutConstraintRequest;
  }

  @override
  void update(void Function(PutConstraintRequestBuilder)? updates) {
    if (updates != null) updates(this);
  }

  @override
  PutConstraintRequest build() => _build();

  _$PutConstraintRequest _build() {
    final _$result = _$v ??
        _$PutConstraintRequest._(
          property: BuiltValueNullFieldError.checkNotNull(
              property, r'PutConstraintRequest', 'property'),
          operator_: BuiltValueNullFieldError.checkNotNull(
              operator_, r'PutConstraintRequest', 'operator_'),
          value: BuiltValueNullFieldError.checkNotNull(
              value, r'PutConstraintRequest', 'value'),
        );
    replace(_$result);
    return _$result;
  }
}

// ignore_for_file: deprecated_member_use_from_same_package,type=lint
