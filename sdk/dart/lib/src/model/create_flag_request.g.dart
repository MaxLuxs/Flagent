// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'create_flag_request.dart';

// **************************************************************************
// BuiltValueGenerator
// **************************************************************************

class _$CreateFlagRequest extends CreateFlagRequest {
  @override
  final String description;
  @override
  final String? key;
  @override
  final String? template;

  factory _$CreateFlagRequest(
          [void Function(CreateFlagRequestBuilder)? updates]) =>
      (CreateFlagRequestBuilder()..update(updates))._build();

  _$CreateFlagRequest._({required this.description, this.key, this.template})
      : super._();
  @override
  CreateFlagRequest rebuild(void Function(CreateFlagRequestBuilder) updates) =>
      (toBuilder()..update(updates)).build();

  @override
  CreateFlagRequestBuilder toBuilder() =>
      CreateFlagRequestBuilder()..replace(this);

  @override
  bool operator ==(Object other) {
    if (identical(other, this)) return true;
    return other is CreateFlagRequest &&
        description == other.description &&
        key == other.key &&
        template == other.template;
  }

  @override
  int get hashCode {
    var _$hash = 0;
    _$hash = $jc(_$hash, description.hashCode);
    _$hash = $jc(_$hash, key.hashCode);
    _$hash = $jc(_$hash, template.hashCode);
    _$hash = $jf(_$hash);
    return _$hash;
  }

  @override
  String toString() {
    return (newBuiltValueToStringHelper(r'CreateFlagRequest')
          ..add('description', description)
          ..add('key', key)
          ..add('template', template))
        .toString();
  }
}

class CreateFlagRequestBuilder
    implements Builder<CreateFlagRequest, CreateFlagRequestBuilder> {
  _$CreateFlagRequest? _$v;

  String? _description;
  String? get description => _$this._description;
  set description(String? description) => _$this._description = description;

  String? _key;
  String? get key => _$this._key;
  set key(String? key) => _$this._key = key;

  String? _template;
  String? get template => _$this._template;
  set template(String? template) => _$this._template = template;

  CreateFlagRequestBuilder() {
    CreateFlagRequest._defaults(this);
  }

  CreateFlagRequestBuilder get _$this {
    final $v = _$v;
    if ($v != null) {
      _description = $v.description;
      _key = $v.key;
      _template = $v.template;
      _$v = null;
    }
    return this;
  }

  @override
  void replace(CreateFlagRequest other) {
    _$v = other as _$CreateFlagRequest;
  }

  @override
  void update(void Function(CreateFlagRequestBuilder)? updates) {
    if (updates != null) updates(this);
  }

  @override
  CreateFlagRequest build() => _build();

  _$CreateFlagRequest _build() {
    final _$result = _$v ??
        _$CreateFlagRequest._(
          description: BuiltValueNullFieldError.checkNotNull(
              description, r'CreateFlagRequest', 'description'),
          key: key,
          template: template,
        );
    replace(_$result);
    return _$result;
  }
}

// ignore_for_file: deprecated_member_use_from_same_package,type=lint
