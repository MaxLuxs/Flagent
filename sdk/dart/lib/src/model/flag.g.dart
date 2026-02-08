// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'flag.dart';

// **************************************************************************
// BuiltValueGenerator
// **************************************************************************

class _$Flag extends Flag {
  @override
  final int id;
  @override
  final String key;
  @override
  final String description;
  @override
  final bool enabled;
  @override
  final int? snapshotID;
  @override
  final bool dataRecordsEnabled;
  @override
  final String? entityType;
  @override
  final String? notes;
  @override
  final String? createdBy;
  @override
  final String? updatedBy;
  @override
  final BuiltList<Segment>? segments;
  @override
  final BuiltList<Variant>? variants;
  @override
  final BuiltList<Tag>? tags;

  factory _$Flag([void Function(FlagBuilder)? updates]) =>
      (FlagBuilder()..update(updates))._build();

  _$Flag._(
      {required this.id,
      required this.key,
      required this.description,
      required this.enabled,
      this.snapshotID,
      required this.dataRecordsEnabled,
      this.entityType,
      this.notes,
      this.createdBy,
      this.updatedBy,
      this.segments,
      this.variants,
      this.tags})
      : super._();
  @override
  Flag rebuild(void Function(FlagBuilder) updates) =>
      (toBuilder()..update(updates)).build();

  @override
  FlagBuilder toBuilder() => FlagBuilder()..replace(this);

  @override
  bool operator ==(Object other) {
    if (identical(other, this)) return true;
    return other is Flag &&
        id == other.id &&
        key == other.key &&
        description == other.description &&
        enabled == other.enabled &&
        snapshotID == other.snapshotID &&
        dataRecordsEnabled == other.dataRecordsEnabled &&
        entityType == other.entityType &&
        notes == other.notes &&
        createdBy == other.createdBy &&
        updatedBy == other.updatedBy &&
        segments == other.segments &&
        variants == other.variants &&
        tags == other.tags;
  }

  @override
  int get hashCode {
    var _$hash = 0;
    _$hash = $jc(_$hash, id.hashCode);
    _$hash = $jc(_$hash, key.hashCode);
    _$hash = $jc(_$hash, description.hashCode);
    _$hash = $jc(_$hash, enabled.hashCode);
    _$hash = $jc(_$hash, snapshotID.hashCode);
    _$hash = $jc(_$hash, dataRecordsEnabled.hashCode);
    _$hash = $jc(_$hash, entityType.hashCode);
    _$hash = $jc(_$hash, notes.hashCode);
    _$hash = $jc(_$hash, createdBy.hashCode);
    _$hash = $jc(_$hash, updatedBy.hashCode);
    _$hash = $jc(_$hash, segments.hashCode);
    _$hash = $jc(_$hash, variants.hashCode);
    _$hash = $jc(_$hash, tags.hashCode);
    _$hash = $jf(_$hash);
    return _$hash;
  }

  @override
  String toString() {
    return (newBuiltValueToStringHelper(r'Flag')
          ..add('id', id)
          ..add('key', key)
          ..add('description', description)
          ..add('enabled', enabled)
          ..add('snapshotID', snapshotID)
          ..add('dataRecordsEnabled', dataRecordsEnabled)
          ..add('entityType', entityType)
          ..add('notes', notes)
          ..add('createdBy', createdBy)
          ..add('updatedBy', updatedBy)
          ..add('segments', segments)
          ..add('variants', variants)
          ..add('tags', tags))
        .toString();
  }
}

class FlagBuilder implements Builder<Flag, FlagBuilder> {
  _$Flag? _$v;

  int? _id;
  int? get id => _$this._id;
  set id(int? id) => _$this._id = id;

  String? _key;
  String? get key => _$this._key;
  set key(String? key) => _$this._key = key;

  String? _description;
  String? get description => _$this._description;
  set description(String? description) => _$this._description = description;

  bool? _enabled;
  bool? get enabled => _$this._enabled;
  set enabled(bool? enabled) => _$this._enabled = enabled;

  int? _snapshotID;
  int? get snapshotID => _$this._snapshotID;
  set snapshotID(int? snapshotID) => _$this._snapshotID = snapshotID;

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

  String? _createdBy;
  String? get createdBy => _$this._createdBy;
  set createdBy(String? createdBy) => _$this._createdBy = createdBy;

  String? _updatedBy;
  String? get updatedBy => _$this._updatedBy;
  set updatedBy(String? updatedBy) => _$this._updatedBy = updatedBy;

  ListBuilder<Segment>? _segments;
  ListBuilder<Segment> get segments =>
      _$this._segments ??= ListBuilder<Segment>();
  set segments(ListBuilder<Segment>? segments) => _$this._segments = segments;

  ListBuilder<Variant>? _variants;
  ListBuilder<Variant> get variants =>
      _$this._variants ??= ListBuilder<Variant>();
  set variants(ListBuilder<Variant>? variants) => _$this._variants = variants;

  ListBuilder<Tag>? _tags;
  ListBuilder<Tag> get tags => _$this._tags ??= ListBuilder<Tag>();
  set tags(ListBuilder<Tag>? tags) => _$this._tags = tags;

  FlagBuilder() {
    Flag._defaults(this);
  }

  FlagBuilder get _$this {
    final $v = _$v;
    if ($v != null) {
      _id = $v.id;
      _key = $v.key;
      _description = $v.description;
      _enabled = $v.enabled;
      _snapshotID = $v.snapshotID;
      _dataRecordsEnabled = $v.dataRecordsEnabled;
      _entityType = $v.entityType;
      _notes = $v.notes;
      _createdBy = $v.createdBy;
      _updatedBy = $v.updatedBy;
      _segments = $v.segments?.toBuilder();
      _variants = $v.variants?.toBuilder();
      _tags = $v.tags?.toBuilder();
      _$v = null;
    }
    return this;
  }

  @override
  void replace(Flag other) {
    _$v = other as _$Flag;
  }

  @override
  void update(void Function(FlagBuilder)? updates) {
    if (updates != null) updates(this);
  }

  @override
  Flag build() => _build();

  _$Flag _build() {
    _$Flag _$result;
    try {
      _$result = _$v ??
          _$Flag._(
            id: BuiltValueNullFieldError.checkNotNull(id, r'Flag', 'id'),
            key: BuiltValueNullFieldError.checkNotNull(key, r'Flag', 'key'),
            description: BuiltValueNullFieldError.checkNotNull(
                description, r'Flag', 'description'),
            enabled: BuiltValueNullFieldError.checkNotNull(
                enabled, r'Flag', 'enabled'),
            snapshotID: snapshotID,
            dataRecordsEnabled: BuiltValueNullFieldError.checkNotNull(
                dataRecordsEnabled, r'Flag', 'dataRecordsEnabled'),
            entityType: entityType,
            notes: notes,
            createdBy: createdBy,
            updatedBy: updatedBy,
            segments: _segments?.build(),
            variants: _variants?.build(),
            tags: _tags?.build(),
          );
    } catch (_) {
      late String _$failedField;
      try {
        _$failedField = 'segments';
        _segments?.build();
        _$failedField = 'variants';
        _variants?.build();
        _$failedField = 'tags';
        _tags?.build();
      } catch (e) {
        throw BuiltValueNestedFieldError(r'Flag', _$failedField, e.toString());
      }
      rethrow;
    }
    replace(_$result);
    return _$result;
  }
}

// ignore_for_file: deprecated_member_use_from_same_package,type=lint
