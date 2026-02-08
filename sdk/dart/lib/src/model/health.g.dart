// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'health.dart';

// **************************************************************************
// BuiltValueGenerator
// **************************************************************************

class _$Health extends Health {
  @override
  final String? status;

  factory _$Health([void Function(HealthBuilder)? updates]) =>
      (HealthBuilder()..update(updates))._build();

  _$Health._({this.status}) : super._();
  @override
  Health rebuild(void Function(HealthBuilder) updates) =>
      (toBuilder()..update(updates)).build();

  @override
  HealthBuilder toBuilder() => HealthBuilder()..replace(this);

  @override
  bool operator ==(Object other) {
    if (identical(other, this)) return true;
    return other is Health && status == other.status;
  }

  @override
  int get hashCode {
    var _$hash = 0;
    _$hash = $jc(_$hash, status.hashCode);
    _$hash = $jf(_$hash);
    return _$hash;
  }

  @override
  String toString() {
    return (newBuiltValueToStringHelper(r'Health')..add('status', status))
        .toString();
  }
}

class HealthBuilder implements Builder<Health, HealthBuilder> {
  _$Health? _$v;

  String? _status;
  String? get status => _$this._status;
  set status(String? status) => _$this._status = status;

  HealthBuilder() {
    Health._defaults(this);
  }

  HealthBuilder get _$this {
    final $v = _$v;
    if ($v != null) {
      _status = $v.status;
      _$v = null;
    }
    return this;
  }

  @override
  void replace(Health other) {
    _$v = other as _$Health;
  }

  @override
  void update(void Function(HealthBuilder)? updates) {
    if (updates != null) updates(this);
  }

  @override
  Health build() => _build();

  _$Health _build() {
    final _$result = _$v ??
        _$Health._(
          status: status,
        );
    replace(_$result);
    return _$result;
  }
}

// ignore_for_file: deprecated_member_use_from_same_package,type=lint
