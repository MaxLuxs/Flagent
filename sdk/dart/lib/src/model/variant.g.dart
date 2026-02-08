// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'variant.dart';

// **************************************************************************
// BuiltValueGenerator
// **************************************************************************

class _$Variant extends Variant {
  @override
  final int id;
  @override
  final int flagID;
  @override
  final String key;
  @override
  final BuiltMap<String, JsonObject?>? attachment;

  factory _$Variant([void Function(VariantBuilder)? updates]) =>
      (VariantBuilder()..update(updates))._build();

  _$Variant._(
      {required this.id,
      required this.flagID,
      required this.key,
      this.attachment})
      : super._();
  @override
  Variant rebuild(void Function(VariantBuilder) updates) =>
      (toBuilder()..update(updates)).build();

  @override
  VariantBuilder toBuilder() => VariantBuilder()..replace(this);

  @override
  bool operator ==(Object other) {
    if (identical(other, this)) return true;
    return other is Variant &&
        id == other.id &&
        flagID == other.flagID &&
        key == other.key &&
        attachment == other.attachment;
  }

  @override
  int get hashCode {
    var _$hash = 0;
    _$hash = $jc(_$hash, id.hashCode);
    _$hash = $jc(_$hash, flagID.hashCode);
    _$hash = $jc(_$hash, key.hashCode);
    _$hash = $jc(_$hash, attachment.hashCode);
    _$hash = $jf(_$hash);
    return _$hash;
  }

  @override
  String toString() {
    return (newBuiltValueToStringHelper(r'Variant')
          ..add('id', id)
          ..add('flagID', flagID)
          ..add('key', key)
          ..add('attachment', attachment))
        .toString();
  }
}

class VariantBuilder implements Builder<Variant, VariantBuilder> {
  _$Variant? _$v;

  int? _id;
  int? get id => _$this._id;
  set id(int? id) => _$this._id = id;

  int? _flagID;
  int? get flagID => _$this._flagID;
  set flagID(int? flagID) => _$this._flagID = flagID;

  String? _key;
  String? get key => _$this._key;
  set key(String? key) => _$this._key = key;

  MapBuilder<String, JsonObject?>? _attachment;
  MapBuilder<String, JsonObject?> get attachment =>
      _$this._attachment ??= MapBuilder<String, JsonObject?>();
  set attachment(MapBuilder<String, JsonObject?>? attachment) =>
      _$this._attachment = attachment;

  VariantBuilder() {
    Variant._defaults(this);
  }

  VariantBuilder get _$this {
    final $v = _$v;
    if ($v != null) {
      _id = $v.id;
      _flagID = $v.flagID;
      _key = $v.key;
      _attachment = $v.attachment?.toBuilder();
      _$v = null;
    }
    return this;
  }

  @override
  void replace(Variant other) {
    _$v = other as _$Variant;
  }

  @override
  void update(void Function(VariantBuilder)? updates) {
    if (updates != null) updates(this);
  }

  @override
  Variant build() => _build();

  _$Variant _build() {
    _$Variant _$result;
    try {
      _$result = _$v ??
          _$Variant._(
            id: BuiltValueNullFieldError.checkNotNull(id, r'Variant', 'id'),
            flagID: BuiltValueNullFieldError.checkNotNull(
                flagID, r'Variant', 'flagID'),
            key: BuiltValueNullFieldError.checkNotNull(key, r'Variant', 'key'),
            attachment: _attachment?.build(),
          );
    } catch (_) {
      late String _$failedField;
      try {
        _$failedField = 'attachment';
        _attachment?.build();
      } catch (e) {
        throw BuiltValueNestedFieldError(
            r'Variant', _$failedField, e.toString());
      }
      rethrow;
    }
    replace(_$result);
    return _$result;
  }
}

// ignore_for_file: deprecated_member_use_from_same_package,type=lint
