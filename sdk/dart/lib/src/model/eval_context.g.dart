// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'eval_context.dart';

// **************************************************************************
// BuiltValueGenerator
// **************************************************************************

const EvalContextFlagTagsOperatorEnum _$evalContextFlagTagsOperatorEnum_ANY =
    const EvalContextFlagTagsOperatorEnum._('ANY');
const EvalContextFlagTagsOperatorEnum _$evalContextFlagTagsOperatorEnum_ALL =
    const EvalContextFlagTagsOperatorEnum._('ALL');

EvalContextFlagTagsOperatorEnum _$evalContextFlagTagsOperatorEnumValueOf(
    String name) {
  switch (name) {
    case 'ANY':
      return _$evalContextFlagTagsOperatorEnum_ANY;
    case 'ALL':
      return _$evalContextFlagTagsOperatorEnum_ALL;
    default:
      throw ArgumentError(name);
  }
}

final BuiltSet<EvalContextFlagTagsOperatorEnum>
    _$evalContextFlagTagsOperatorEnumValues = BuiltSet<
        EvalContextFlagTagsOperatorEnum>(const <EvalContextFlagTagsOperatorEnum>[
  _$evalContextFlagTagsOperatorEnum_ANY,
  _$evalContextFlagTagsOperatorEnum_ALL,
]);

Serializer<EvalContextFlagTagsOperatorEnum>
    _$evalContextFlagTagsOperatorEnumSerializer =
    _$EvalContextFlagTagsOperatorEnumSerializer();

class _$EvalContextFlagTagsOperatorEnumSerializer
    implements PrimitiveSerializer<EvalContextFlagTagsOperatorEnum> {
  static const Map<String, Object> _toWire = const <String, Object>{
    'ANY': 'ANY',
    'ALL': 'ALL',
  };
  static const Map<Object, String> _fromWire = const <Object, String>{
    'ANY': 'ANY',
    'ALL': 'ALL',
  };

  @override
  final Iterable<Type> types = const <Type>[EvalContextFlagTagsOperatorEnum];
  @override
  final String wireName = 'EvalContextFlagTagsOperatorEnum';

  @override
  Object serialize(
          Serializers serializers, EvalContextFlagTagsOperatorEnum object,
          {FullType specifiedType = FullType.unspecified}) =>
      _toWire[object.name] ?? object.name;

  @override
  EvalContextFlagTagsOperatorEnum deserialize(
          Serializers serializers, Object serialized,
          {FullType specifiedType = FullType.unspecified}) =>
      EvalContextFlagTagsOperatorEnum.valueOf(
          _fromWire[serialized] ?? (serialized is String ? serialized : ''));
}

class _$EvalContext extends EvalContext {
  @override
  final String? entityID;
  @override
  final String? entityType;
  @override
  final BuiltMap<String, JsonObject?>? entityContext;
  @override
  final bool? enableDebug;
  @override
  final int? flagID;
  @override
  final String? flagKey;
  @override
  final BuiltList<String>? flagTags;
  @override
  final EvalContextFlagTagsOperatorEnum? flagTagsOperator;

  factory _$EvalContext([void Function(EvalContextBuilder)? updates]) =>
      (EvalContextBuilder()..update(updates))._build();

  _$EvalContext._(
      {this.entityID,
      this.entityType,
      this.entityContext,
      this.enableDebug,
      this.flagID,
      this.flagKey,
      this.flagTags,
      this.flagTagsOperator})
      : super._();
  @override
  EvalContext rebuild(void Function(EvalContextBuilder) updates) =>
      (toBuilder()..update(updates)).build();

  @override
  EvalContextBuilder toBuilder() => EvalContextBuilder()..replace(this);

  @override
  bool operator ==(Object other) {
    if (identical(other, this)) return true;
    return other is EvalContext &&
        entityID == other.entityID &&
        entityType == other.entityType &&
        entityContext == other.entityContext &&
        enableDebug == other.enableDebug &&
        flagID == other.flagID &&
        flagKey == other.flagKey &&
        flagTags == other.flagTags &&
        flagTagsOperator == other.flagTagsOperator;
  }

  @override
  int get hashCode {
    var _$hash = 0;
    _$hash = $jc(_$hash, entityID.hashCode);
    _$hash = $jc(_$hash, entityType.hashCode);
    _$hash = $jc(_$hash, entityContext.hashCode);
    _$hash = $jc(_$hash, enableDebug.hashCode);
    _$hash = $jc(_$hash, flagID.hashCode);
    _$hash = $jc(_$hash, flagKey.hashCode);
    _$hash = $jc(_$hash, flagTags.hashCode);
    _$hash = $jc(_$hash, flagTagsOperator.hashCode);
    _$hash = $jf(_$hash);
    return _$hash;
  }

  @override
  String toString() {
    return (newBuiltValueToStringHelper(r'EvalContext')
          ..add('entityID', entityID)
          ..add('entityType', entityType)
          ..add('entityContext', entityContext)
          ..add('enableDebug', enableDebug)
          ..add('flagID', flagID)
          ..add('flagKey', flagKey)
          ..add('flagTags', flagTags)
          ..add('flagTagsOperator', flagTagsOperator))
        .toString();
  }
}

class EvalContextBuilder implements Builder<EvalContext, EvalContextBuilder> {
  _$EvalContext? _$v;

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

  bool? _enableDebug;
  bool? get enableDebug => _$this._enableDebug;
  set enableDebug(bool? enableDebug) => _$this._enableDebug = enableDebug;

  int? _flagID;
  int? get flagID => _$this._flagID;
  set flagID(int? flagID) => _$this._flagID = flagID;

  String? _flagKey;
  String? get flagKey => _$this._flagKey;
  set flagKey(String? flagKey) => _$this._flagKey = flagKey;

  ListBuilder<String>? _flagTags;
  ListBuilder<String> get flagTags =>
      _$this._flagTags ??= ListBuilder<String>();
  set flagTags(ListBuilder<String>? flagTags) => _$this._flagTags = flagTags;

  EvalContextFlagTagsOperatorEnum? _flagTagsOperator;
  EvalContextFlagTagsOperatorEnum? get flagTagsOperator =>
      _$this._flagTagsOperator;
  set flagTagsOperator(EvalContextFlagTagsOperatorEnum? flagTagsOperator) =>
      _$this._flagTagsOperator = flagTagsOperator;

  EvalContextBuilder() {
    EvalContext._defaults(this);
  }

  EvalContextBuilder get _$this {
    final $v = _$v;
    if ($v != null) {
      _entityID = $v.entityID;
      _entityType = $v.entityType;
      _entityContext = $v.entityContext?.toBuilder();
      _enableDebug = $v.enableDebug;
      _flagID = $v.flagID;
      _flagKey = $v.flagKey;
      _flagTags = $v.flagTags?.toBuilder();
      _flagTagsOperator = $v.flagTagsOperator;
      _$v = null;
    }
    return this;
  }

  @override
  void replace(EvalContext other) {
    _$v = other as _$EvalContext;
  }

  @override
  void update(void Function(EvalContextBuilder)? updates) {
    if (updates != null) updates(this);
  }

  @override
  EvalContext build() => _build();

  _$EvalContext _build() {
    _$EvalContext _$result;
    try {
      _$result = _$v ??
          _$EvalContext._(
            entityID: entityID,
            entityType: entityType,
            entityContext: _entityContext?.build(),
            enableDebug: enableDebug,
            flagID: flagID,
            flagKey: flagKey,
            flagTags: _flagTags?.build(),
            flagTagsOperator: flagTagsOperator,
          );
    } catch (_) {
      late String _$failedField;
      try {
        _$failedField = 'entityContext';
        _entityContext?.build();

        _$failedField = 'flagTags';
        _flagTags?.build();
      } catch (e) {
        throw BuiltValueNestedFieldError(
            r'EvalContext', _$failedField, e.toString());
      }
      rethrow;
    }
    replace(_$result);
    return _$result;
  }
}

// ignore_for_file: deprecated_member_use_from_same_package,type=lint
