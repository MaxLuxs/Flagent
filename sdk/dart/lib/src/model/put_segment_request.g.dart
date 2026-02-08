// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'put_segment_request.dart';

// **************************************************************************
// BuiltValueGenerator
// **************************************************************************

class _$PutSegmentRequest extends PutSegmentRequest {
  @override
  final String description;
  @override
  final int rolloutPercent;

  factory _$PutSegmentRequest(
          [void Function(PutSegmentRequestBuilder)? updates]) =>
      (PutSegmentRequestBuilder()..update(updates))._build();

  _$PutSegmentRequest._(
      {required this.description, required this.rolloutPercent})
      : super._();
  @override
  PutSegmentRequest rebuild(void Function(PutSegmentRequestBuilder) updates) =>
      (toBuilder()..update(updates)).build();

  @override
  PutSegmentRequestBuilder toBuilder() =>
      PutSegmentRequestBuilder()..replace(this);

  @override
  bool operator ==(Object other) {
    if (identical(other, this)) return true;
    return other is PutSegmentRequest &&
        description == other.description &&
        rolloutPercent == other.rolloutPercent;
  }

  @override
  int get hashCode {
    var _$hash = 0;
    _$hash = $jc(_$hash, description.hashCode);
    _$hash = $jc(_$hash, rolloutPercent.hashCode);
    _$hash = $jf(_$hash);
    return _$hash;
  }

  @override
  String toString() {
    return (newBuiltValueToStringHelper(r'PutSegmentRequest')
          ..add('description', description)
          ..add('rolloutPercent', rolloutPercent))
        .toString();
  }
}

class PutSegmentRequestBuilder
    implements Builder<PutSegmentRequest, PutSegmentRequestBuilder> {
  _$PutSegmentRequest? _$v;

  String? _description;
  String? get description => _$this._description;
  set description(String? description) => _$this._description = description;

  int? _rolloutPercent;
  int? get rolloutPercent => _$this._rolloutPercent;
  set rolloutPercent(int? rolloutPercent) =>
      _$this._rolloutPercent = rolloutPercent;

  PutSegmentRequestBuilder() {
    PutSegmentRequest._defaults(this);
  }

  PutSegmentRequestBuilder get _$this {
    final $v = _$v;
    if ($v != null) {
      _description = $v.description;
      _rolloutPercent = $v.rolloutPercent;
      _$v = null;
    }
    return this;
  }

  @override
  void replace(PutSegmentRequest other) {
    _$v = other as _$PutSegmentRequest;
  }

  @override
  void update(void Function(PutSegmentRequestBuilder)? updates) {
    if (updates != null) updates(this);
  }

  @override
  PutSegmentRequest build() => _build();

  _$PutSegmentRequest _build() {
    final _$result = _$v ??
        _$PutSegmentRequest._(
          description: BuiltValueNullFieldError.checkNotNull(
              description, r'PutSegmentRequest', 'description'),
          rolloutPercent: BuiltValueNullFieldError.checkNotNull(
              rolloutPercent, r'PutSegmentRequest', 'rolloutPercent'),
        );
    replace(_$result);
    return _$result;
  }
}

// ignore_for_file: deprecated_member_use_from_same_package,type=lint
