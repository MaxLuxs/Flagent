// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'put_flag_request.dart';

// **************************************************************************
// BuiltValueGenerator
// **************************************************************************

class _$PutFlagRequest extends PutFlagRequest {
  @override
  final String? description;
  @override
  final String? key;
  @override
  final bool? dataRecordsEnabled;
  @override
  final String? entityType;
  @override
  final String? notes;

  factory _$PutFlagRequest([void Function(PutFlagRequestBuilder)? updates]) =>
      (PutFlagRequestBuilder()..update(updates))._build();

  _$PutFlagRequest._(
      {this.description,
      this.key,
      this.dataRecordsEnabled,
      this.entityType,
      this.notes})
      : super._();
  @override
  PutFlagRequest rebuild(void Function(PutFlagRequestBuilder) updates) =>
      (toBuilder()..update(updates)).build();

  @override
  PutFlagRequestBuilder toBuilder() => PutFlagRequestBuilder()..replace(this);

  @override
  bool operator ==(Object other) {
    if (identical(other, this)) return true;
    return other is PutFlagRequest &&
        description == other.description &&
        key == other.key &&
        dataRecordsEnabled == other.dataRecordsEnabled &&
        entityType == other.entityType &&
        notes == other.notes;
  }

  @override
  int get hashCode {
    var _$hash = 0;
    _$hash = $jc(_$hash, description.hashCode);
    _$hash = $jc(_$hash, key.hashCode);
    _$hash = $jc(_$hash, dataRecordsEnabled.hashCode);
    _$hash = $jc(_$hash, entityType.hashCode);
    _$hash = $jc(_$hash, notes.hashCode);
    _$hash = $jf(_$hash);
    return _$hash;
  }

  @override
  String toString() {
    return (newBuiltValueToStringHelper(r'PutFlagRequest')
          ..add('description', description)
          ..add('key', key)
          ..add('dataRecordsEnabled', dataRecordsEnabled)
          ..add('entityType', entityType)
          ..add('notes', notes))
        .toString();
  }
}

class PutFlagRequestBuilder
    implements Builder<PutFlagRequest, PutFlagRequestBuilder> {
  _$PutFlagRequest? _$v;

  String? _description;
  String? get description => _$this._description;
  set description(String? description) => _$this._description = description;

  String? _key;
  String? get key => _$this._key;
  set key(String? key) => _$this._key = key;

  bool? _dataRecordsEnabled;
  bool? get dataRecordsEnabled => _$this._dataRecordsEnabled;
  set dataRecordsEnabled(bool? dataRecordsEnabled) =>
      _$this._dataRecordsEnabled = dataRecordsEnabled;

  String? _entityType;
  String? get entityType => _$this._entityType;
  set entityType(String? entityType) => _$this._entityType = entityType;

  String? _notes;
  String? get notes => _$this._notes;
  set notes(String? notes) => _$this._notes = notes;

  PutFlagRequestBuilder() {
    PutFlagRequest._defaults(this);
  }

  PutFlagRequestBuilder get _$this {
    final $v = _$v;
    if ($v != null) {
      _description = $v.description;
      _key = $v.key;
      _dataRecordsEnabled = $v.dataRecordsEnabled;
      _entityType = $v.entityType;
      _notes = $v.notes;
      _$v = null;
    }
    return this;
  }

  @override
  void replace(PutFlagRequest other) {
    _$v = other as _$PutFlagRequest;
  }

  @override
  void update(void Function(PutFlagRequestBuilder)? updates) {
    if (updates != null) updates(this);
  }

  @override
  PutFlagRequest build() => _build();

  _$PutFlagRequest _build() {
    final _$result = _$v ??
        _$PutFlagRequest._(
          description: description,
          key: key,
          dataRecordsEnabled: dataRecordsEnabled,
          entityType: entityType,
          notes: notes,
        );
    replace(_$result);
    return _$result;
  }
}

// ignore_for_file: deprecated_member_use_from_same_package,type=lint
