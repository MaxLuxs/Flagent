// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'set_flag_enabled_request.dart';

// **************************************************************************
// BuiltValueGenerator
// **************************************************************************

class _$SetFlagEnabledRequest extends SetFlagEnabledRequest {
  @override
  final bool enabled;

  factory _$SetFlagEnabledRequest(
          [void Function(SetFlagEnabledRequestBuilder)? updates]) =>
      (SetFlagEnabledRequestBuilder()..update(updates))._build();

  _$SetFlagEnabledRequest._({required this.enabled}) : super._();
  @override
  SetFlagEnabledRequest rebuild(
          void Function(SetFlagEnabledRequestBuilder) updates) =>
      (toBuilder()..update(updates)).build();

  @override
  SetFlagEnabledRequestBuilder toBuilder() =>
      SetFlagEnabledRequestBuilder()..replace(this);

  @override
  bool operator ==(Object other) {
    if (identical(other, this)) return true;
    return other is SetFlagEnabledRequest && enabled == other.enabled;
  }

  @override
  int get hashCode {
    var _$hash = 0;
    _$hash = $jc(_$hash, enabled.hashCode);
    _$hash = $jf(_$hash);
    return _$hash;
  }

  @override
  String toString() {
    return (newBuiltValueToStringHelper(r'SetFlagEnabledRequest')
          ..add('enabled', enabled))
        .toString();
  }
}

class SetFlagEnabledRequestBuilder
    implements Builder<SetFlagEnabledRequest, SetFlagEnabledRequestBuilder> {
  _$SetFlagEnabledRequest? _$v;

  bool? _enabled;
  bool? get enabled => _$this._enabled;
  set enabled(bool? enabled) => _$this._enabled = enabled;

  SetFlagEnabledRequestBuilder() {
    SetFlagEnabledRequest._defaults(this);
  }

  SetFlagEnabledRequestBuilder get _$this {
    final $v = _$v;
    if ($v != null) {
      _enabled = $v.enabled;
      _$v = null;
    }
    return this;
  }

  @override
  void replace(SetFlagEnabledRequest other) {
    _$v = other as _$SetFlagEnabledRequest;
  }

  @override
  void update(void Function(SetFlagEnabledRequestBuilder)? updates) {
    if (updates != null) updates(this);
  }

  @override
  SetFlagEnabledRequest build() => _build();

  _$SetFlagEnabledRequest _build() {
    final _$result = _$v ??
        _$SetFlagEnabledRequest._(
          enabled: BuiltValueNullFieldError.checkNotNull(
              enabled, r'SetFlagEnabledRequest', 'enabled'),
        );
    replace(_$result);
    return _$result;
  }
}

// ignore_for_file: deprecated_member_use_from_same_package,type=lint
