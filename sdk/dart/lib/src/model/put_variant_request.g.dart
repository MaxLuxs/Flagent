// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'put_variant_request.dart';

// **************************************************************************
// BuiltValueGenerator
// **************************************************************************

class _$PutVariantRequest extends PutVariantRequest {
  @override
  final String key;
  @override
  final BuiltMap<String, JsonObject?>? attachment;

  factory _$PutVariantRequest(
          [void Function(PutVariantRequestBuilder)? updates]) =>
      (PutVariantRequestBuilder()..update(updates))._build();

  _$PutVariantRequest._({required this.key, this.attachment}) : super._();
  @override
  PutVariantRequest rebuild(void Function(PutVariantRequestBuilder) updates) =>
      (toBuilder()..update(updates)).build();

  @override
  PutVariantRequestBuilder toBuilder() =>
      PutVariantRequestBuilder()..replace(this);

  @override
  bool operator ==(Object other) {
    if (identical(other, this)) return true;
    return other is PutVariantRequest &&
        key == other.key &&
        attachment == other.attachment;
  }

  @override
  int get hashCode {
    var _$hash = 0;
    _$hash = $jc(_$hash, key.hashCode);
    _$hash = $jc(_$hash, attachment.hashCode);
    _$hash = $jf(_$hash);
    return _$hash;
  }

  @override
  String toString() {
    return (newBuiltValueToStringHelper(r'PutVariantRequest')
          ..add('key', key)
          ..add('attachment', attachment))
        .toString();
  }
}

class PutVariantRequestBuilder
    implements Builder<PutVariantRequest, PutVariantRequestBuilder> {
  _$PutVariantRequest? _$v;

  String? _key;
  String? get key => _$this._key;
  set key(String? key) => _$this._key = key;

  MapBuilder<String, JsonObject?>? _attachment;
  MapBuilder<String, JsonObject?> get attachment =>
      _$this._attachment ??= MapBuilder<String, JsonObject?>();
  set attachment(MapBuilder<String, JsonObject?>? attachment) =>
      _$this._attachment = attachment;

  PutVariantRequestBuilder() {
    PutVariantRequest._defaults(this);
  }

  PutVariantRequestBuilder get _$this {
    final $v = _$v;
    if ($v != null) {
      _key = $v.key;
      _attachment = $v.attachment?.toBuilder();
      _$v = null;
    }
    return this;
  }

  @override
  void replace(PutVariantRequest other) {
    _$v = other as _$PutVariantRequest;
  }

  @override
  void update(void Function(PutVariantRequestBuilder)? updates) {
    if (updates != null) updates(this);
  }

  @override
  PutVariantRequest build() => _build();

  _$PutVariantRequest _build() {
    _$PutVariantRequest _$result;
    try {
      _$result = _$v ??
          _$PutVariantRequest._(
            key: BuiltValueNullFieldError.checkNotNull(
                key, r'PutVariantRequest', 'key'),
            attachment: _attachment?.build(),
          );
    } catch (_) {
      late String _$failedField;
      try {
        _$failedField = 'attachment';
        _attachment?.build();
      } catch (e) {
        throw BuiltValueNestedFieldError(
            r'PutVariantRequest', _$failedField, e.toString());
      }
      rethrow;
    }
    replace(_$result);
    return _$result;
  }
}

// ignore_for_file: deprecated_member_use_from_same_package,type=lint
