// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'create_tag_request.dart';

// **************************************************************************
// BuiltValueGenerator
// **************************************************************************

class _$CreateTagRequest extends CreateTagRequest {
  @override
  final String value;

  factory _$CreateTagRequest(
          [void Function(CreateTagRequestBuilder)? updates]) =>
      (CreateTagRequestBuilder()..update(updates))._build();

  _$CreateTagRequest._({required this.value}) : super._();
  @override
  CreateTagRequest rebuild(void Function(CreateTagRequestBuilder) updates) =>
      (toBuilder()..update(updates)).build();

  @override
  CreateTagRequestBuilder toBuilder() =>
      CreateTagRequestBuilder()..replace(this);

  @override
  bool operator ==(Object other) {
    if (identical(other, this)) return true;
    return other is CreateTagRequest && value == other.value;
  }

  @override
  int get hashCode {
    var _$hash = 0;
    _$hash = $jc(_$hash, value.hashCode);
    _$hash = $jf(_$hash);
    return _$hash;
  }

  @override
  String toString() {
    return (newBuiltValueToStringHelper(r'CreateTagRequest')
          ..add('value', value))
        .toString();
  }
}

class CreateTagRequestBuilder
    implements Builder<CreateTagRequest, CreateTagRequestBuilder> {
  _$CreateTagRequest? _$v;

  String? _value;
  String? get value => _$this._value;
  set value(String? value) => _$this._value = value;

  CreateTagRequestBuilder() {
    CreateTagRequest._defaults(this);
  }

  CreateTagRequestBuilder get _$this {
    final $v = _$v;
    if ($v != null) {
      _value = $v.value;
      _$v = null;
    }
    return this;
  }

  @override
  void replace(CreateTagRequest other) {
    _$v = other as _$CreateTagRequest;
  }

  @override
  void update(void Function(CreateTagRequestBuilder)? updates) {
    if (updates != null) updates(this);
  }

  @override
  CreateTagRequest build() => _build();

  _$CreateTagRequest _build() {
    final _$result = _$v ??
        _$CreateTagRequest._(
          value: BuiltValueNullFieldError.checkNotNull(
              value, r'CreateTagRequest', 'value'),
        );
    replace(_$result);
    return _$result;
  }
}

// ignore_for_file: deprecated_member_use_from_same_package,type=lint
