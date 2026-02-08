// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'distribution_request.dart';

// **************************************************************************
// BuiltValueGenerator
// **************************************************************************

class _$DistributionRequest extends DistributionRequest {
  @override
  final int variantID;
  @override
  final String? variantKey;
  @override
  final int percent;

  factory _$DistributionRequest(
          [void Function(DistributionRequestBuilder)? updates]) =>
      (DistributionRequestBuilder()..update(updates))._build();

  _$DistributionRequest._(
      {required this.variantID, this.variantKey, required this.percent})
      : super._();
  @override
  DistributionRequest rebuild(
          void Function(DistributionRequestBuilder) updates) =>
      (toBuilder()..update(updates)).build();

  @override
  DistributionRequestBuilder toBuilder() =>
      DistributionRequestBuilder()..replace(this);

  @override
  bool operator ==(Object other) {
    if (identical(other, this)) return true;
    return other is DistributionRequest &&
        variantID == other.variantID &&
        variantKey == other.variantKey &&
        percent == other.percent;
  }

  @override
  int get hashCode {
    var _$hash = 0;
    _$hash = $jc(_$hash, variantID.hashCode);
    _$hash = $jc(_$hash, variantKey.hashCode);
    _$hash = $jc(_$hash, percent.hashCode);
    _$hash = $jf(_$hash);
    return _$hash;
  }

  @override
  String toString() {
    return (newBuiltValueToStringHelper(r'DistributionRequest')
          ..add('variantID', variantID)
          ..add('variantKey', variantKey)
          ..add('percent', percent))
        .toString();
  }
}

class DistributionRequestBuilder
    implements Builder<DistributionRequest, DistributionRequestBuilder> {
  _$DistributionRequest? _$v;

  int? _variantID;
  int? get variantID => _$this._variantID;
  set variantID(int? variantID) => _$this._variantID = variantID;

  String? _variantKey;
  String? get variantKey => _$this._variantKey;
  set variantKey(String? variantKey) => _$this._variantKey = variantKey;

  int? _percent;
  int? get percent => _$this._percent;
  set percent(int? percent) => _$this._percent = percent;

  DistributionRequestBuilder() {
    DistributionRequest._defaults(this);
  }

  DistributionRequestBuilder get _$this {
    final $v = _$v;
    if ($v != null) {
      _variantID = $v.variantID;
      _variantKey = $v.variantKey;
      _percent = $v.percent;
      _$v = null;
    }
    return this;
  }

  @override
  void replace(DistributionRequest other) {
    _$v = other as _$DistributionRequest;
  }

  @override
  void update(void Function(DistributionRequestBuilder)? updates) {
    if (updates != null) updates(this);
  }

  @override
  DistributionRequest build() => _build();

  _$DistributionRequest _build() {
    final _$result = _$v ??
        _$DistributionRequest._(
          variantID: BuiltValueNullFieldError.checkNotNull(
              variantID, r'DistributionRequest', 'variantID'),
          variantKey: variantKey,
          percent: BuiltValueNullFieldError.checkNotNull(
              percent, r'DistributionRequest', 'percent'),
        );
    replace(_$result);
    return _$result;
  }
}

// ignore_for_file: deprecated_member_use_from_same_package,type=lint
