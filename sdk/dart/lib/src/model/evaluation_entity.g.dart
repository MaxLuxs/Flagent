// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'evaluation_entity.dart';

// **************************************************************************
// BuiltValueGenerator
// **************************************************************************

class _$EvaluationEntity extends EvaluationEntity {
  @override
  final String? entityID;
  @override
  final String? entityType;
  @override
  final BuiltMap<String, JsonObject?>? entityContext;

  factory _$EvaluationEntity(
          [void Function(EvaluationEntityBuilder)? updates]) =>
      (EvaluationEntityBuilder()..update(updates))._build();

  _$EvaluationEntity._({this.entityID, this.entityType, this.entityContext})
      : super._();
  @override
  EvaluationEntity rebuild(void Function(EvaluationEntityBuilder) updates) =>
      (toBuilder()..update(updates)).build();

  @override
  EvaluationEntityBuilder toBuilder() =>
      EvaluationEntityBuilder()..replace(this);

  @override
  bool operator ==(Object other) {
    if (identical(other, this)) return true;
    return other is EvaluationEntity &&
        entityID == other.entityID &&
        entityType == other.entityType &&
        entityContext == other.entityContext;
  }

  @override
  int get hashCode {
    var _$hash = 0;
    _$hash = $jc(_$hash, entityID.hashCode);
    _$hash = $jc(_$hash, entityType.hashCode);
    _$hash = $jc(_$hash, entityContext.hashCode);
    _$hash = $jf(_$hash);
    return _$hash;
  }

  @override
  String toString() {
    return (newBuiltValueToStringHelper(r'EvaluationEntity')
          ..add('entityID', entityID)
          ..add('entityType', entityType)
          ..add('entityContext', entityContext))
        .toString();
  }
}

class EvaluationEntityBuilder
    implements Builder<EvaluationEntity, EvaluationEntityBuilder> {
  _$EvaluationEntity? _$v;

  String? _entityID;
  String? get entityID => _$this._entityID;
  set entityID(String? entityID) => _$this._entityID = entityID;

  String? _entityType;
  String? get entityType => _$this._entityType;
  set entityType(String? entityType) => _$this._entityType = entityType;

  MapBuilder<String, JsonObject?>? _entityContext;
  MapBuilder<String, JsonObject?> get entityContext =>
      _$this._entityContext ??= MapBuilder<String, JsonObject?>();
  set entityContext(MapBuilder<String, JsonObject?>? entityContext) =>
      _$this._entityContext = entityContext;

  EvaluationEntityBuilder() {
    EvaluationEntity._defaults(this);
  }

  EvaluationEntityBuilder get _$this {
    final $v = _$v;
    if ($v != null) {
      _entityID = $v.entityID;
      _entityType = $v.entityType;
      _entityContext = $v.entityContext?.toBuilder();
      _$v = null;
    }
    return this;
  }

  @override
  void replace(EvaluationEntity other) {
    _$v = other as _$EvaluationEntity;
  }

  @override
  void update(void Function(EvaluationEntityBuilder)? updates) {
    if (updates != null) updates(this);
  }

  @override
  EvaluationEntity build() => _build();

  _$EvaluationEntity _build() {
    _$EvaluationEntity _$result;
    try {
      _$result = _$v ??
          _$EvaluationEntity._(
            entityID: entityID,
            entityType: entityType,
            entityContext: _entityContext?.build(),
          );
    } catch (_) {
      late String _$failedField;
      try {
        _$failedField = 'entityContext';
        _entityContext?.build();
      } catch (e) {
        throw BuiltValueNestedFieldError(
            r'EvaluationEntity', _$failedField, e.toString());
      }
      rethrow;
    }
    replace(_$result);
    return _$result;
  }
}

// ignore_for_file: deprecated_member_use_from_same_package,type=lint
