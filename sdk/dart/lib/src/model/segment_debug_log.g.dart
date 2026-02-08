// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'segment_debug_log.dart';

// **************************************************************************
// BuiltValueGenerator
// **************************************************************************

class _$SegmentDebugLog extends SegmentDebugLog {
  @override
  final int? segmentID;
  @override
  final String? msg;

  factory _$SegmentDebugLog([void Function(SegmentDebugLogBuilder)? updates]) =>
      (SegmentDebugLogBuilder()..update(updates))._build();

  _$SegmentDebugLog._({this.segmentID, this.msg}) : super._();
  @override
  SegmentDebugLog rebuild(void Function(SegmentDebugLogBuilder) updates) =>
      (toBuilder()..update(updates)).build();

  @override
  SegmentDebugLogBuilder toBuilder() => SegmentDebugLogBuilder()..replace(this);

  @override
  bool operator ==(Object other) {
    if (identical(other, this)) return true;
    return other is SegmentDebugLog &&
        segmentID == other.segmentID &&
        msg == other.msg;
  }

  @override
  int get hashCode {
    var _$hash = 0;
    _$hash = $jc(_$hash, segmentID.hashCode);
    _$hash = $jc(_$hash, msg.hashCode);
    _$hash = $jf(_$hash);
    return _$hash;
  }

  @override
  String toString() {
    return (newBuiltValueToStringHelper(r'SegmentDebugLog')
          ..add('segmentID', segmentID)
          ..add('msg', msg))
        .toString();
  }
}

class SegmentDebugLogBuilder
    implements Builder<SegmentDebugLog, SegmentDebugLogBuilder> {
  _$SegmentDebugLog? _$v;

  int? _segmentID;
  int? get segmentID => _$this._segmentID;
  set segmentID(int? segmentID) => _$this._segmentID = segmentID;

  String? _msg;
  String? get msg => _$this._msg;
  set msg(String? msg) => _$this._msg = msg;

  SegmentDebugLogBuilder() {
    SegmentDebugLog._defaults(this);
  }

  SegmentDebugLogBuilder get _$this {
    final $v = _$v;
    if ($v != null) {
      _segmentID = $v.segmentID;
      _msg = $v.msg;
      _$v = null;
    }
    return this;
  }

  @override
  void replace(SegmentDebugLog other) {
    _$v = other as _$SegmentDebugLog;
  }

  @override
  void update(void Function(SegmentDebugLogBuilder)? updates) {
    if (updates != null) updates(this);
  }

  @override
  SegmentDebugLog build() => _build();

  _$SegmentDebugLog _build() {
    final _$result = _$v ??
        _$SegmentDebugLog._(
          segmentID: segmentID,
          msg: msg,
        );
    replace(_$result);
    return _$result;
  }
}

// ignore_for_file: deprecated_member_use_from_same_package,type=lint
