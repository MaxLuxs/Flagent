// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'distribution.dart';

// **************************************************************************
// BuiltValueGenerator
// **************************************************************************

class _$Distribution extends Distribution {
  @override
  final int id;
  @override
  final int segmentID;
  @override
  final int variantID;
  @override
  final String? variantKey;
  @override
  final int percent;

  factory _$Distribution([void Function(DistributionBuilder)? updates]) =>
      (DistributionBuilder()..update(updates))._build();

  _$Distribution._(
      {required this.id,
      required this.segmentID,
      required this.variantID,
      this.variantKey,
      required this.percent})
      : super._();
  @override
  Distribution rebuild(void Function(DistributionBuilder) updates) =>
      (toBuilder()..update(updates)).build();

  @override
  DistributionBuilder toBuilder() => DistributionBuilder()..replace(this);

  @override
  bool operator ==(Object other) {
    if (identical(other, this)) return true;
    return other is Distribution &&
        id == other.id &&
        segmentID == other.segmentID &&
        variantID == other.variantID &&
        variantKey == other.variantKey &&
        percent == other.percent;
  }

  @override
  int get hashCode {
    var _$hash = 0;
    _$hash = $jc(_$hash, id.hashCode);
    _$hash = $jc(_$hash, segmentID.hashCode);
    _$hash = $jc(_$hash, variantID.hashCode);
    _$hash = $jc(_$hash, variantKey.hashCode);
    _$hash = $jc(_$hash, percent.hashCode);
    _$hash = $jf(_$hash);
    return _$hash;
  }

  @override
  String toString() {
    return (newBuiltValueToStringHelper(r'Distribution')
          ..add('id', id)
          ..add('segmentID', segmentID)
          ..add('variantID', variantID)
          ..add('variantKey', variantKey)
          ..add('percent', percent))
        .toString();
  }
}

class DistributionBuilder
    implements Builder<Distribution, DistributionBuilder> {
  _$Distribution? _$v;

  int? _id;
  int? get id => _$this._id;
  set id(int? id) => _$this._id = id;

  int? _segmentID;
  int? get segmentID => _$this._segmentID;
  set segmentID(int? segmentID) => _$this._segmentID = segmentID;

  int? _variantID;
  int? get variantID => _$this._variantID;
  set variantID(int? variantID) => _$this._variantID = variantID;

  String? _variantKey;
  String? get variantKey => _$this._variantKey;
  set variantKey(String? variantKey) => _$this._variantKey = variantKey;

  int? _percent;
  int? get percent => _$this._percent;
  set percent(int? percent) => _$this._percent = percent;

  DistributionBuilder() {
    Distribution._defaults(this);
  }

  DistributionBuilder get _$this {
    final $v = _$v;
    if ($v != null) {
      _id = $v.id;
      _segmentID = $v.segmentID;
      _variantID = $v.variantID;
      _variantKey = $v.variantKey;
      _percent = $v.percent;
      _$v = null;
    }
    return this;
  }

  @override
  void replace(Distribution other) {
    _$v = other as _$Distribution;
  }

  @override
  void update(void Function(DistributionBuilder)? updates) {
    if (updates != null) updates(this);
  }

  @override
  Distribution build() => _build();

  _$Distribution _build() {
    final _$result = _$v ??
        _$Distribution._(
          id: BuiltValueNullFieldError.checkNotNull(id, r'Distribution', 'id'),
          segmentID: BuiltValueNullFieldError.checkNotNull(
              segmentID, r'Distribution', 'segmentID'),
          variantID: BuiltValueNullFieldError.checkNotNull(
              variantID, r'Distribution', 'variantID'),
          variantKey: variantKey,
          percent: BuiltValueNullFieldError.checkNotNull(
              percent, r'Distribution', 'percent'),
        );
    replace(_$result);
    return _$result;
  }
}

// ignore_for_file: deprecated_member_use_from_same_package,type=lint
