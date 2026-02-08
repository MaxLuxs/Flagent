// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'create_segment_request.dart';

// **************************************************************************
// BuiltValueGenerator
// **************************************************************************

class _$CreateSegmentRequest extends CreateSegmentRequest {
  @override
  final String description;
  @override
  final int rolloutPercent;

  factory _$CreateSegmentRequest(
          [void Function(CreateSegmentRequestBuilder)? updates]) =>
      (CreateSegmentRequestBuilder()..update(updates))._build();

  _$CreateSegmentRequest._(
      {required this.description, required this.rolloutPercent})
      : super._();
  @override
  CreateSegmentRequest rebuild(
          void Function(CreateSegmentRequestBuilder) updates) =>
      (toBuilder()..update(updates)).build();

  @override
  CreateSegmentRequestBuilder toBuilder() =>
      CreateSegmentRequestBuilder()..replace(this);

  @override
  bool operator ==(Object other) {
    if (identical(other, this)) return true;
    return other is CreateSegmentRequest &&
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
    return (newBuiltValueToStringHelper(r'CreateSegmentRequest')
          ..add('description', description)
          ..add('rolloutPercent', rolloutPercent))
        .toString();
  }
}

class CreateSegmentRequestBuilder
    implements Builder<CreateSegmentRequest, CreateSegmentRequestBuilder> {
  _$CreateSegmentRequest? _$v;

  String? _description;
  String? get description => _$this._description;
  set description(String? description) => _$this._description = description;

  int? _rolloutPercent;
  int? get rolloutPercent => _$this._rolloutPercent;
  set rolloutPercent(int? rolloutPercent) =>
      _$this._rolloutPercent = rolloutPercent;

  CreateSegmentRequestBuilder() {
    CreateSegmentRequest._defaults(this);
  }

  CreateSegmentRequestBuilder get _$this {
    final $v = _$v;
    if ($v != null) {
      _description = $v.description;
      _rolloutPercent = $v.rolloutPercent;
      _$v = null;
    }
    return this;
  }

  @override
  void replace(CreateSegmentRequest other) {
    _$v = other as _$CreateSegmentRequest;
  }

  @override
  void update(void Function(CreateSegmentRequestBuilder)? updates) {
    if (updates != null) updates(this);
  }

  @override
  CreateSegmentRequest build() => _build();

  _$CreateSegmentRequest _build() {
    final _$result = _$v ??
        _$CreateSegmentRequest._(
          description: BuiltValueNullFieldError.checkNotNull(
              description, r'CreateSegmentRequest', 'description'),
          rolloutPercent: BuiltValueNullFieldError.checkNotNull(
              rolloutPercent, r'CreateSegmentRequest', 'rolloutPercent'),
        );
    replace(_$result);
    return _$result;
  }
}

// ignore_for_file: deprecated_member_use_from_same_package,type=lint
