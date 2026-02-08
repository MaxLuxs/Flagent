// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'eval_debug_log.dart';

// **************************************************************************
// BuiltValueGenerator
// **************************************************************************

class _$EvalDebugLog extends EvalDebugLog {
  @override
  final String? msg;
  @override
  final BuiltList<SegmentDebugLog>? segmentDebugLogs;

  factory _$EvalDebugLog([void Function(EvalDebugLogBuilder)? updates]) =>
      (EvalDebugLogBuilder()..update(updates))._build();

  _$EvalDebugLog._({this.msg, this.segmentDebugLogs}) : super._();
  @override
  EvalDebugLog rebuild(void Function(EvalDebugLogBuilder) updates) =>
      (toBuilder()..update(updates)).build();

  @override
  EvalDebugLogBuilder toBuilder() => EvalDebugLogBuilder()..replace(this);

  @override
  bool operator ==(Object other) {
    if (identical(other, this)) return true;
    return other is EvalDebugLog &&
        msg == other.msg &&
        segmentDebugLogs == other.segmentDebugLogs;
  }

  @override
  int get hashCode {
    var _$hash = 0;
    _$hash = $jc(_$hash, msg.hashCode);
    _$hash = $jc(_$hash, segmentDebugLogs.hashCode);
    _$hash = $jf(_$hash);
    return _$hash;
  }

  @override
  String toString() {
    return (newBuiltValueToStringHelper(r'EvalDebugLog')
          ..add('msg', msg)
          ..add('segmentDebugLogs', segmentDebugLogs))
        .toString();
  }
}

class EvalDebugLogBuilder
    implements Builder<EvalDebugLog, EvalDebugLogBuilder> {
  _$EvalDebugLog? _$v;

  String? _msg;
  String? get msg => _$this._msg;
  set msg(String? msg) => _$this._msg = msg;

  ListBuilder<SegmentDebugLog>? _segmentDebugLogs;
  ListBuilder<SegmentDebugLog> get segmentDebugLogs =>
      _$this._segmentDebugLogs ??= ListBuilder<SegmentDebugLog>();
  set segmentDebugLogs(ListBuilder<SegmentDebugLog>? segmentDebugLogs) =>
      _$this._segmentDebugLogs = segmentDebugLogs;

  EvalDebugLogBuilder() {
    EvalDebugLog._defaults(this);
  }

  EvalDebugLogBuilder get _$this {
    final $v = _$v;
    if ($v != null) {
      _msg = $v.msg;
      _segmentDebugLogs = $v.segmentDebugLogs?.toBuilder();
      _$v = null;
    }
    return this;
  }

  @override
  void replace(EvalDebugLog other) {
    _$v = other as _$EvalDebugLog;
  }

  @override
  void update(void Function(EvalDebugLogBuilder)? updates) {
    if (updates != null) updates(this);
  }

  @override
  EvalDebugLog build() => _build();

  _$EvalDebugLog _build() {
    _$EvalDebugLog _$result;
    try {
      _$result = _$v ??
          _$EvalDebugLog._(
            msg: msg,
            segmentDebugLogs: _segmentDebugLogs?.build(),
          );
    } catch (_) {
      late String _$failedField;
      try {
        _$failedField = 'segmentDebugLogs';
        _segmentDebugLogs?.build();
      } catch (e) {
        throw BuiltValueNestedFieldError(
            r'EvalDebugLog', _$failedField, e.toString());
      }
      rethrow;
    }
    replace(_$result);
    return _$result;
  }
}

// ignore_for_file: deprecated_member_use_from_same_package,type=lint
