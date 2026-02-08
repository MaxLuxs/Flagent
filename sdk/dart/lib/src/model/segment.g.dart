// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'segment.dart';

// **************************************************************************
// BuiltValueGenerator
// **************************************************************************

class _$Segment extends Segment {
  @override
  final int id;
  @override
  final int flagID;
  @override
  final String description;
  @override
  final int rank;
  @override
  final int rolloutPercent;
  @override
  final BuiltList<Constraint>? constraints;
  @override
  final BuiltList<Distribution>? distributions;

  factory _$Segment([void Function(SegmentBuilder)? updates]) =>
      (SegmentBuilder()..update(updates))._build();

  _$Segment._(
      {required this.id,
      required this.flagID,
      required this.description,
      required this.rank,
      required this.rolloutPercent,
      this.constraints,
      this.distributions})
      : super._();
  @override
  Segment rebuild(void Function(SegmentBuilder) updates) =>
      (toBuilder()..update(updates)).build();

  @override
  SegmentBuilder toBuilder() => SegmentBuilder()..replace(this);

  @override
  bool operator ==(Object other) {
    if (identical(other, this)) return true;
    return other is Segment &&
        id == other.id &&
        flagID == other.flagID &&
        description == other.description &&
        rank == other.rank &&
        rolloutPercent == other.rolloutPercent &&
        constraints == other.constraints &&
        distributions == other.distributions;
  }

  @override
  int get hashCode {
    var _$hash = 0;
    _$hash = $jc(_$hash, id.hashCode);
    _$hash = $jc(_$hash, flagID.hashCode);
    _$hash = $jc(_$hash, description.hashCode);
    _$hash = $jc(_$hash, rank.hashCode);
    _$hash = $jc(_$hash, rolloutPercent.hashCode);
    _$hash = $jc(_$hash, constraints.hashCode);
    _$hash = $jc(_$hash, distributions.hashCode);
    _$hash = $jf(_$hash);
    return _$hash;
  }

  @override
  String toString() {
    return (newBuiltValueToStringHelper(r'Segment')
          ..add('id', id)
          ..add('flagID', flagID)
          ..add('description', description)
          ..add('rank', rank)
          ..add('rolloutPercent', rolloutPercent)
          ..add('constraints', constraints)
          ..add('distributions', distributions))
        .toString();
  }
}

class SegmentBuilder implements Builder<Segment, SegmentBuilder> {
  _$Segment? _$v;

  int? _id;
  int? get id => _$this._id;
  set id(int? id) => _$this._id = id;

  int? _flagID;
  int? get flagID => _$this._flagID;
  set flagID(int? flagID) => _$this._flagID = flagID;

  String? _description;
  String? get description => _$this._description;
  set description(String? description) => _$this._description = description;

  int? _rank;
  int? get rank => _$this._rank;
  set rank(int? rank) => _$this._rank = rank;

  int? _rolloutPercent;
  int? get rolloutPercent => _$this._rolloutPercent;
  set rolloutPercent(int? rolloutPercent) =>
      _$this._rolloutPercent = rolloutPercent;

  ListBuilder<Constraint>? _constraints;
  ListBuilder<Constraint> get constraints =>
      _$this._constraints ??= ListBuilder<Constraint>();
  set constraints(ListBuilder<Constraint>? constraints) =>
      _$this._constraints = constraints;

  ListBuilder<Distribution>? _distributions;
  ListBuilder<Distribution> get distributions =>
      _$this._distributions ??= ListBuilder<Distribution>();
  set distributions(ListBuilder<Distribution>? distributions) =>
      _$this._distributions = distributions;

  SegmentBuilder() {
    Segment._defaults(this);
  }

  SegmentBuilder get _$this {
    final $v = _$v;
    if ($v != null) {
      _id = $v.id;
      _flagID = $v.flagID;
      _description = $v.description;
      _rank = $v.rank;
      _rolloutPercent = $v.rolloutPercent;
      _constraints = $v.constraints?.toBuilder();
      _distributions = $v.distributions?.toBuilder();
      _$v = null;
    }
    return this;
  }

  @override
  void replace(Segment other) {
    _$v = other as _$Segment;
  }

  @override
  void update(void Function(SegmentBuilder)? updates) {
    if (updates != null) updates(this);
  }

  @override
  Segment build() => _build();

  _$Segment _build() {
    _$Segment _$result;
    try {
      _$result = _$v ??
          _$Segment._(
            id: BuiltValueNullFieldError.checkNotNull(id, r'Segment', 'id'),
            flagID: BuiltValueNullFieldError.checkNotNull(
                flagID, r'Segment', 'flagID'),
            description: BuiltValueNullFieldError.checkNotNull(
                description, r'Segment', 'description'),
            rank:
                BuiltValueNullFieldError.checkNotNull(rank, r'Segment', 'rank'),
            rolloutPercent: BuiltValueNullFieldError.checkNotNull(
                rolloutPercent, r'Segment', 'rolloutPercent'),
            constraints: _constraints?.build(),
            distributions: _distributions?.build(),
          );
    } catch (_) {
      late String _$failedField;
      try {
        _$failedField = 'constraints';
        _constraints?.build();
        _$failedField = 'distributions';
        _distributions?.build();
      } catch (e) {
        throw BuiltValueNestedFieldError(
            r'Segment', _$failedField, e.toString());
      }
      rethrow;
    }
    replace(_$result);
    return _$result;
  }
}

// ignore_for_file: deprecated_member_use_from_same_package,type=lint
