// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'flag_snapshot.dart';

// **************************************************************************
// BuiltValueGenerator
// **************************************************************************

class _$FlagSnapshot extends FlagSnapshot {
  @override
  final int id;
  @override
  final String? updatedBy;
  @override
  final Flag flag;
  @override
  final DateTime updatedAt;

  factory _$FlagSnapshot([void Function(FlagSnapshotBuilder)? updates]) =>
      (FlagSnapshotBuilder()..update(updates))._build();

  _$FlagSnapshot._(
      {required this.id,
      this.updatedBy,
      required this.flag,
      required this.updatedAt})
      : super._();
  @override
  FlagSnapshot rebuild(void Function(FlagSnapshotBuilder) updates) =>
      (toBuilder()..update(updates)).build();

  @override
  FlagSnapshotBuilder toBuilder() => FlagSnapshotBuilder()..replace(this);

  @override
  bool operator ==(Object other) {
    if (identical(other, this)) return true;
    return other is FlagSnapshot &&
        id == other.id &&
        updatedBy == other.updatedBy &&
        flag == other.flag &&
        updatedAt == other.updatedAt;
  }

  @override
  int get hashCode {
    var _$hash = 0;
    _$hash = $jc(_$hash, id.hashCode);
    _$hash = $jc(_$hash, updatedBy.hashCode);
    _$hash = $jc(_$hash, flag.hashCode);
    _$hash = $jc(_$hash, updatedAt.hashCode);
    _$hash = $jf(_$hash);
    return _$hash;
  }

  @override
  String toString() {
    return (newBuiltValueToStringHelper(r'FlagSnapshot')
          ..add('id', id)
          ..add('updatedBy', updatedBy)
          ..add('flag', flag)
          ..add('updatedAt', updatedAt))
        .toString();
  }
}

class FlagSnapshotBuilder
    implements Builder<FlagSnapshot, FlagSnapshotBuilder> {
  _$FlagSnapshot? _$v;

  int? _id;
  int? get id => _$this._id;
  set id(int? id) => _$this._id = id;

  String? _updatedBy;
  String? get updatedBy => _$this._updatedBy;
  set updatedBy(String? updatedBy) => _$this._updatedBy = updatedBy;

  FlagBuilder? _flag;
  FlagBuilder get flag => _$this._flag ??= FlagBuilder();
  set flag(FlagBuilder? flag) => _$this._flag = flag;

  DateTime? _updatedAt;
  DateTime? get updatedAt => _$this._updatedAt;
  set updatedAt(DateTime? updatedAt) => _$this._updatedAt = updatedAt;

  FlagSnapshotBuilder() {
    FlagSnapshot._defaults(this);
  }

  FlagSnapshotBuilder get _$this {
    final $v = _$v;
    if ($v != null) {
      _id = $v.id;
      _updatedBy = $v.updatedBy;
      _flag = $v.flag.toBuilder();
      _updatedAt = $v.updatedAt;
      _$v = null;
    }
    return this;
  }

  @override
  void replace(FlagSnapshot other) {
    _$v = other as _$FlagSnapshot;
  }

  @override
  void update(void Function(FlagSnapshotBuilder)? updates) {
    if (updates != null) updates(this);
  }

  @override
  FlagSnapshot build() => _build();

  _$FlagSnapshot _build() {
    _$FlagSnapshot _$result;
    try {
      _$result = _$v ??
          _$FlagSnapshot._(
            id: BuiltValueNullFieldError.checkNotNull(
                id, r'FlagSnapshot', 'id'),
            updatedBy: updatedBy,
            flag: flag.build(),
            updatedAt: BuiltValueNullFieldError.checkNotNull(
                updatedAt, r'FlagSnapshot', 'updatedAt'),
          );
    } catch (_) {
      late String _$failedField;
      try {
        _$failedField = 'flag';
        flag.build();
      } catch (e) {
        throw BuiltValueNestedFieldError(
            r'FlagSnapshot', _$failedField, e.toString());
      }
      rethrow;
    }
    replace(_$result);
    return _$result;
  }
}

// ignore_for_file: deprecated_member_use_from_same_package,type=lint
