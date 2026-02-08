// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'create_variant_request.dart';

// **************************************************************************
// BuiltValueGenerator
// **************************************************************************

class _$CreateVariantRequest extends CreateVariantRequest {
  @override
  final String key;
  @override
  final BuiltMap<String, JsonObject?>? attachment;

  factory _$CreateVariantRequest(
          [void Function(CreateVariantRequestBuilder)? updates]) =>
      (CreateVariantRequestBuilder()..update(updates))._build();

  _$CreateVariantRequest._({required this.key, this.attachment}) : super._();
  @override
  CreateVariantRequest rebuild(
          void Function(CreateVariantRequestBuilder) updates) =>
      (toBuilder()..update(updates)).build();

  @override
  CreateVariantRequestBuilder toBuilder() =>
      CreateVariantRequestBuilder()..replace(this);

  @override
  bool operator ==(Object other) {
    if (identical(other, this)) return true;
    return other is CreateVariantRequest &&
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
    return (newBuiltValueToStringHelper(r'CreateVariantRequest')
          ..add('key', key)
          ..add('attachment', attachment))
        .toString();
  }
}

class CreateVariantRequestBuilder
    implements Builder<CreateVariantRequest, CreateVariantRequestBuilder> {
  _$CreateVariantRequest? _$v;

  String? _key;
  String? get key => _$this._key;
  set key(String? key) => _$this._key = key;

  MapBuilder<String, JsonObject?>? _attachment;
  MapBuilder<String, JsonObject?> get attachment =>
      _$this._attachment ??= MapBuilder<String, JsonObject?>();
  set attachment(MapBuilder<String, JsonObject?>? attachment) =>
      _$this._attachment = attachment;

  CreateVariantRequestBuilder() {
    CreateVariantRequest._defaults(this);
  }

  CreateVariantRequestBuilder get _$this {
    final $v = _$v;
    if ($v != null) {
      _key = $v.key;
      _attachment = $v.attachment?.toBuilder();
      _$v = null;
    }
    return this;
  }

  @override
  void replace(CreateVariantRequest other) {
    _$v = other as _$CreateVariantRequest;
  }

  @override
  void update(void Function(CreateVariantRequestBuilder)? updates) {
    if (updates != null) updates(this);
  }

  @override
  CreateVariantRequest build() => _build();

  _$CreateVariantRequest _build() {
    _$CreateVariantRequest _$result;
    try {
      _$result = _$v ??
          _$CreateVariantRequest._(
            key: BuiltValueNullFieldError.checkNotNull(
                key, r'CreateVariantRequest', 'key'),
            attachment: _attachment?.build(),
          );
    } catch (_) {
      late String _$failedField;
      try {
        _$failedField = 'attachment';
        _attachment?.build();
      } catch (e) {
        throw BuiltValueNestedFieldError(
            r'CreateVariantRequest', _$failedField, e.toString());
      }
      rethrow;
    }
    replace(_$result);
    return _$result;
  }
}

// ignore_for_file: deprecated_member_use_from_same_package,type=lint
