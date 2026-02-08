// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'info.dart';

// **************************************************************************
// BuiltValueGenerator
// **************************************************************************

class _$Info extends Info {
  @override
  final String? version;
  @override
  final String? buildTime;
  @override
  final String? gitCommit;

  factory _$Info([void Function(InfoBuilder)? updates]) =>
      (InfoBuilder()..update(updates))._build();

  _$Info._({this.version, this.buildTime, this.gitCommit}) : super._();
  @override
  Info rebuild(void Function(InfoBuilder) updates) =>
      (toBuilder()..update(updates)).build();

  @override
  InfoBuilder toBuilder() => InfoBuilder()..replace(this);

  @override
  bool operator ==(Object other) {
    if (identical(other, this)) return true;
    return other is Info &&
        version == other.version &&
        buildTime == other.buildTime &&
        gitCommit == other.gitCommit;
  }

  @override
  int get hashCode {
    var _$hash = 0;
    _$hash = $jc(_$hash, version.hashCode);
    _$hash = $jc(_$hash, buildTime.hashCode);
    _$hash = $jc(_$hash, gitCommit.hashCode);
    _$hash = $jf(_$hash);
    return _$hash;
  }

  @override
  String toString() {
    return (newBuiltValueToStringHelper(r'Info')
          ..add('version', version)
          ..add('buildTime', buildTime)
          ..add('gitCommit', gitCommit))
        .toString();
  }
}

class InfoBuilder implements Builder<Info, InfoBuilder> {
  _$Info? _$v;

  String? _version;
  String? get version => _$this._version;
  set version(String? version) => _$this._version = version;

  String? _buildTime;
  String? get buildTime => _$this._buildTime;
  set buildTime(String? buildTime) => _$this._buildTime = buildTime;

  String? _gitCommit;
  String? get gitCommit => _$this._gitCommit;
  set gitCommit(String? gitCommit) => _$this._gitCommit = gitCommit;

  InfoBuilder() {
    Info._defaults(this);
  }

  InfoBuilder get _$this {
    final $v = _$v;
    if ($v != null) {
      _version = $v.version;
      _buildTime = $v.buildTime;
      _gitCommit = $v.gitCommit;
      _$v = null;
    }
    return this;
  }

  @override
  void replace(Info other) {
    _$v = other as _$Info;
  }

  @override
  void update(void Function(InfoBuilder)? updates) {
    if (updates != null) updates(this);
  }

  @override
  Info build() => _build();

  _$Info _build() {
    final _$result = _$v ??
        _$Info._(
          version: version,
          buildTime: buildTime,
          gitCommit: gitCommit,
        );
    replace(_$result);
    return _$result;
  }
}

// ignore_for_file: deprecated_member_use_from_same_package,type=lint
