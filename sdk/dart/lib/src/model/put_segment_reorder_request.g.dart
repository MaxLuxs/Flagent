// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'put_segment_reorder_request.dart';

// **************************************************************************
// BuiltValueGenerator
// **************************************************************************

class _$PutSegmentReorderRequest extends PutSegmentReorderRequest {
  @override
  final BuiltList<int> segmentIDs;

  factory _$PutSegmentReorderRequest(
          [void Function(PutSegmentReorderRequestBuilder)? updates]) =>
      (PutSegmentReorderRequestBuilder()..update(updates))._build();

  _$PutSegmentReorderRequest._({required this.segmentIDs}) : super._();
  @override
  PutSegmentReorderRequest rebuild(
          void Function(PutSegmentReorderRequestBuilder) updates) =>
      (toBuilder()..update(updates)).build();

  @override
  PutSegmentReorderRequestBuilder toBuilder() =>
      PutSegmentReorderRequestBuilder()..replace(this);

  @override
  bool operator ==(Object other) {
    if (identical(other, this)) return true;
    return other is PutSegmentReorderRequest && segmentIDs == other.segmentIDs;
  }

  @override
  int get hashCode {
    var _$hash = 0;
    _$hash = $jc(_$hash, segmentIDs.hashCode);
    _$hash = $jf(_$hash);
    return _$hash;
  }

  @override
  String toString() {
    return (newBuiltValueToStringHelper(r'PutSegmentReorderRequest')
          ..add('segmentIDs', segmentIDs))
        .toString();
  }
}

class PutSegmentReorderRequestBuilder
    implements
        Builder<PutSegmentReorderRequest, PutSegmentReorderRequestBuilder> {
  _$PutSegmentReorderRequest? _$v;

  ListBuilder<int>? _segmentIDs;
  ListBuilder<int> get segmentIDs => _$this._segmentIDs ??= ListBuilder<int>();
  set segmentIDs(ListBuilder<int>? segmentIDs) =>
      _$this._segmentIDs = segmentIDs;

  PutSegmentReorderRequestBuilder() {
    PutSegmentReorderRequest._defaults(this);
  }

  PutSegmentReorderRequestBuilder get _$this {
    final $v = _$v;
    if ($v != null) {
      _segmentIDs = $v.segmentIDs.toBuilder();
      _$v = null;
    }
    return this;
  }

  @override
  void replace(PutSegmentReorderRequest other) {
    _$v = other as _$PutSegmentReorderRequest;
  }

  @override
  void update(void Function(PutSegmentReorderRequestBuilder)? updates) {
    if (updates != null) updates(this);
  }

  @override
  PutSegmentReorderRequest build() => _build();

  _$PutSegmentReorderRequest _build() {
    _$PutSegmentReorderRequest _$result;
    try {
      _$result = _$v ??
          _$PutSegmentReorderRequest._(
            segmentIDs: segmentIDs.build(),
          );
    } catch (_) {
      late String _$failedField;
      try {
        _$failedField = 'segmentIDs';
        segmentIDs.build();
      } catch (e) {
        throw BuiltValueNestedFieldError(
            r'PutSegmentReorderRequest', _$failedField, e.toString());
      }
      rethrow;
    }
    replace(_$result);
    return _$result;
  }
}

// ignore_for_file: deprecated_member_use_from_same_package,type=lint
