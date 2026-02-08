// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'eval_result.dart';

// **************************************************************************
// BuiltValueGenerator
// **************************************************************************

class _$EvalResult extends EvalResult {
  @override
  final int? flagID;
  @override
  final String? flagKey;
  @override
  final int? flagSnapshotID;
  @override
  final BuiltList<String>? flagTags;
  @override
  final int? segmentID;
  @override
  final int? variantID;
  @override
  final String? variantKey;
  @override
  final BuiltMap<String, JsonObject?>? variantAttachment;
  @override
  final EvalContext? evalContext;
  @override
  final DateTime? timestamp;
  @override
  final EvalDebugLog? evalDebugLog;

  factory _$EvalResult([void Function(EvalResultBuilder)? updates]) =>
      (EvalResultBuilder()..update(updates))._build();

  _$EvalResult._(
      {this.flagID,
      this.flagKey,
      this.flagSnapshotID,
      this.flagTags,
      this.segmentID,
      this.variantID,
      this.variantKey,
      this.variantAttachment,
      this.evalContext,
      this.timestamp,
      this.evalDebugLog})
      : super._();
  @override
  EvalResult rebuild(void Function(EvalResultBuilder) updates) =>
      (toBuilder()..update(updates)).build();

  @override
  EvalResultBuilder toBuilder() => EvalResultBuilder()..replace(this);

  @override
  bool operator ==(Object other) {
    if (identical(other, this)) return true;
    return other is EvalResult &&
        flagID == other.flagID &&
        flagKey == other.flagKey &&
        flagSnapshotID == other.flagSnapshotID &&
        flagTags == other.flagTags &&
        segmentID == other.segmentID &&
        variantID == other.variantID &&
        variantKey == other.variantKey &&
        variantAttachment == other.variantAttachment &&
        evalContext == other.evalContext &&
        timestamp == other.timestamp &&
        evalDebugLog == other.evalDebugLog;
  }

  @override
  int get hashCode {
    var _$hash = 0;
    _$hash = $jc(_$hash, flagID.hashCode);
    _$hash = $jc(_$hash, flagKey.hashCode);
    _$hash = $jc(_$hash, flagSnapshotID.hashCode);
    _$hash = $jc(_$hash, flagTags.hashCode);
    _$hash = $jc(_$hash, segmentID.hashCode);
    _$hash = $jc(_$hash, variantID.hashCode);
    _$hash = $jc(_$hash, variantKey.hashCode);
    _$hash = $jc(_$hash, variantAttachment.hashCode);
    _$hash = $jc(_$hash, evalContext.hashCode);
    _$hash = $jc(_$hash, timestamp.hashCode);
    _$hash = $jc(_$hash, evalDebugLog.hashCode);
    _$hash = $jf(_$hash);
    return _$hash;
  }

  @override
  String toString() {
    return (newBuiltValueToStringHelper(r'EvalResult')
          ..add('flagID', flagID)
          ..add('flagKey', flagKey)
          ..add('flagSnapshotID', flagSnapshotID)
          ..add('flagTags', flagTags)
          ..add('segmentID', segmentID)
          ..add('variantID', variantID)
          ..add('variantKey', variantKey)
          ..add('variantAttachment', variantAttachment)
          ..add('evalContext', evalContext)
          ..add('timestamp', timestamp)
          ..add('evalDebugLog', evalDebugLog))
        .toString();
  }
}

class EvalResultBuilder implements Builder<EvalResult, EvalResultBuilder> {
  _$EvalResult? _$v;

  int? _flagID;
  int? get flagID => _$this._flagID;
  set flagID(int? flagID) => _$this._flagID = flagID;

  String? _flagKey;
  String? get flagKey => _$this._flagKey;
  set flagKey(String? flagKey) => _$this._flagKey = flagKey;

  int? _flagSnapshotID;
  int? get flagSnapshotID => _$this._flagSnapshotID;
  set flagSnapshotID(int? flagSnapshotID) =>
      _$this._flagSnapshotID = flagSnapshotID;

  ListBuilder<String>? _flagTags;
  ListBuilder<String> get flagTags =>
      _$this._flagTags ??= ListBuilder<String>();
  set flagTags(ListBuilder<String>? flagTags) => _$this._flagTags = flagTags;

  int? _segmentID;
  int? get segmentID => _$this._segmentID;
  set segmentID(int? segmentID) => _$this._segmentID = segmentID;

  int? _variantID;
  int? get variantID => _$this._variantID;
  set variantID(int? variantID) => _$this._variantID = variantID;

  String? _variantKey;
  String? get variantKey => _$this._variantKey;
  set variantKey(String? variantKey) => _$this._variantKey = variantKey;

  MapBuilder<String, JsonObject?>? _variantAttachment;
  MapBuilder<String, JsonObject?> get variantAttachment =>
      _$this._variantAttachment ??= MapBuilder<String, JsonObject?>();
  set variantAttachment(MapBuilder<String, JsonObject?>? variantAttachment) =>
      _$this._variantAttachment = variantAttachment;

  EvalContextBuilder? _evalContext;
  EvalContextBuilder get evalContext =>
      _$this._evalContext ??= EvalContextBuilder();
  set evalContext(EvalContextBuilder? evalContext) =>
      _$this._evalContext = evalContext;

  DateTime? _timestamp;
  DateTime? get timestamp => _$this._timestamp;
  set timestamp(DateTime? timestamp) => _$this._timestamp = timestamp;

  EvalDebugLogBuilder? _evalDebugLog;
  EvalDebugLogBuilder get evalDebugLog =>
      _$this._evalDebugLog ??= EvalDebugLogBuilder();
  set evalDebugLog(EvalDebugLogBuilder? evalDebugLog) =>
      _$this._evalDebugLog = evalDebugLog;

  EvalResultBuilder() {
    EvalResult._defaults(this);
  }

  EvalResultBuilder get _$this {
    final $v = _$v;
    if ($v != null) {
      _flagID = $v.flagID;
      _flagKey = $v.flagKey;
      _flagSnapshotID = $v.flagSnapshotID;
      _flagTags = $v.flagTags?.toBuilder();
      _segmentID = $v.segmentID;
      _variantID = $v.variantID;
      _variantKey = $v.variantKey;
      _variantAttachment = $v.variantAttachment?.toBuilder();
      _evalContext = $v.evalContext?.toBuilder();
      _timestamp = $v.timestamp;
      _evalDebugLog = $v.evalDebugLog?.toBuilder();
      _$v = null;
    }
    return this;
  }

  @override
  void replace(EvalResult other) {
    _$v = other as _$EvalResult;
  }

  @override
  void update(void Function(EvalResultBuilder)? updates) {
    if (updates != null) updates(this);
  }

  @override
  EvalResult build() => _build();

  _$EvalResult _build() {
    _$EvalResult _$result;
    try {
      _$result = _$v ??
          _$EvalResult._(
            flagID: flagID,
            flagKey: flagKey,
            flagSnapshotID: flagSnapshotID,
            flagTags: _flagTags?.build(),
            segmentID: segmentID,
            variantID: variantID,
            variantKey: variantKey,
            variantAttachment: _variantAttachment?.build(),
            evalContext: _evalContext?.build(),
            timestamp: timestamp,
            evalDebugLog: _evalDebugLog?.build(),
          );
    } catch (_) {
      late String _$failedField;
      try {
        _$failedField = 'flagTags';
        _flagTags?.build();

        _$failedField = 'variantAttachment';
        _variantAttachment?.build();
        _$failedField = 'evalContext';
        _evalContext?.build();

        _$failedField = 'evalDebugLog';
        _evalDebugLog?.build();
      } catch (e) {
        throw BuiltValueNestedFieldError(
            r'EvalResult', _$failedField, e.toString());
      }
      rethrow;
    }
    replace(_$result);
    return _$result;
  }
}

// ignore_for_file: deprecated_member_use_from_same_package,type=lint
