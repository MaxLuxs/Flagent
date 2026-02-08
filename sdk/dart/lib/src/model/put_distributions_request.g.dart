// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'put_distributions_request.dart';

// **************************************************************************
// BuiltValueGenerator
// **************************************************************************

class _$PutDistributionsRequest extends PutDistributionsRequest {
  @override
  final BuiltList<DistributionRequest> distributions;

  factory _$PutDistributionsRequest(
          [void Function(PutDistributionsRequestBuilder)? updates]) =>
      (PutDistributionsRequestBuilder()..update(updates))._build();

  _$PutDistributionsRequest._({required this.distributions}) : super._();
  @override
  PutDistributionsRequest rebuild(
          void Function(PutDistributionsRequestBuilder) updates) =>
      (toBuilder()..update(updates)).build();

  @override
  PutDistributionsRequestBuilder toBuilder() =>
      PutDistributionsRequestBuilder()..replace(this);

  @override
  bool operator ==(Object other) {
    if (identical(other, this)) return true;
    return other is PutDistributionsRequest &&
        distributions == other.distributions;
  }

  @override
  int get hashCode {
    var _$hash = 0;
    _$hash = $jc(_$hash, distributions.hashCode);
    _$hash = $jf(_$hash);
    return _$hash;
  }

  @override
  String toString() {
    return (newBuiltValueToStringHelper(r'PutDistributionsRequest')
          ..add('distributions', distributions))
        .toString();
  }
}

class PutDistributionsRequestBuilder
    implements
        Builder<PutDistributionsRequest, PutDistributionsRequestBuilder> {
  _$PutDistributionsRequest? _$v;

  ListBuilder<DistributionRequest>? _distributions;
  ListBuilder<DistributionRequest> get distributions =>
      _$this._distributions ??= ListBuilder<DistributionRequest>();
  set distributions(ListBuilder<DistributionRequest>? distributions) =>
      _$this._distributions = distributions;

  PutDistributionsRequestBuilder() {
    PutDistributionsRequest._defaults(this);
  }

  PutDistributionsRequestBuilder get _$this {
    final $v = _$v;
    if ($v != null) {
      _distributions = $v.distributions.toBuilder();
      _$v = null;
    }
    return this;
  }

  @override
  void replace(PutDistributionsRequest other) {
    _$v = other as _$PutDistributionsRequest;
  }

  @override
  void update(void Function(PutDistributionsRequestBuilder)? updates) {
    if (updates != null) updates(this);
  }

  @override
  PutDistributionsRequest build() => _build();

  _$PutDistributionsRequest _build() {
    _$PutDistributionsRequest _$result;
    try {
      _$result = _$v ??
          _$PutDistributionsRequest._(
            distributions: distributions.build(),
          );
    } catch (_) {
      late String _$failedField;
      try {
        _$failedField = 'distributions';
        distributions.build();
      } catch (e) {
        throw BuiltValueNestedFieldError(
            r'PutDistributionsRequest', _$failedField, e.toString());
      }
      rethrow;
    }
    replace(_$result);
    return _$result;
  }
}

// ignore_for_file: deprecated_member_use_from_same_package,type=lint
