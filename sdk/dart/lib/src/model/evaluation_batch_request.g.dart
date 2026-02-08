// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'evaluation_batch_request.dart';

// **************************************************************************
// BuiltValueGenerator
// **************************************************************************

const EvaluationBatchRequestFlagTagsOperatorEnum
    _$evaluationBatchRequestFlagTagsOperatorEnum_ANY =
    const EvaluationBatchRequestFlagTagsOperatorEnum._('ANY');
const EvaluationBatchRequestFlagTagsOperatorEnum
    _$evaluationBatchRequestFlagTagsOperatorEnum_ALL =
    const EvaluationBatchRequestFlagTagsOperatorEnum._('ALL');

EvaluationBatchRequestFlagTagsOperatorEnum
    _$evaluationBatchRequestFlagTagsOperatorEnumValueOf(String name) {
  switch (name) {
    case 'ANY':
      return _$evaluationBatchRequestFlagTagsOperatorEnum_ANY;
    case 'ALL':
      return _$evaluationBatchRequestFlagTagsOperatorEnum_ALL;
    default:
      throw ArgumentError(name);
  }
}

final BuiltSet<EvaluationBatchRequestFlagTagsOperatorEnum>
    _$evaluationBatchRequestFlagTagsOperatorEnumValues = BuiltSet<
        EvaluationBatchRequestFlagTagsOperatorEnum>(const <EvaluationBatchRequestFlagTagsOperatorEnum>[
  _$evaluationBatchRequestFlagTagsOperatorEnum_ANY,
  _$evaluationBatchRequestFlagTagsOperatorEnum_ALL,
]);

Serializer<EvaluationBatchRequestFlagTagsOperatorEnum>
    _$evaluationBatchRequestFlagTagsOperatorEnumSerializer =
    _$EvaluationBatchRequestFlagTagsOperatorEnumSerializer();

class _$EvaluationBatchRequestFlagTagsOperatorEnumSerializer
    implements PrimitiveSerializer<EvaluationBatchRequestFlagTagsOperatorEnum> {
  static const Map<String, Object> _toWire = const <String, Object>{
    'ANY': 'ANY',
    'ALL': 'ALL',
  };
  static const Map<Object, String> _fromWire = const <Object, String>{
    'ANY': 'ANY',
    'ALL': 'ALL',
  };

  @override
  final Iterable<Type> types = const <Type>[
    EvaluationBatchRequestFlagTagsOperatorEnum
  ];
  @override
  final String wireName = 'EvaluationBatchRequestFlagTagsOperatorEnum';

  @override
  Object serialize(Serializers serializers,
          EvaluationBatchRequestFlagTagsOperatorEnum object,
          {FullType specifiedType = FullType.unspecified}) =>
      _toWire[object.name] ?? object.name;

  @override
  EvaluationBatchRequestFlagTagsOperatorEnum deserialize(
          Serializers serializers, Object serialized,
          {FullType specifiedType = FullType.unspecified}) =>
      EvaluationBatchRequestFlagTagsOperatorEnum.valueOf(
          _fromWire[serialized] ?? (serialized is String ? serialized : ''));
}

class _$EvaluationBatchRequest extends EvaluationBatchRequest {
  @override
  final BuiltList<EvaluationEntity> entities;
  @override
  final bool? enableDebug;
  @override
  final BuiltList<int>? flagIDs;
  @override
  final BuiltList<String>? flagKeys;
  @override
  final BuiltList<String>? flagTags;
  @override
  final EvaluationBatchRequestFlagTagsOperatorEnum? flagTagsOperator;

  factory _$EvaluationBatchRequest(
          [void Function(EvaluationBatchRequestBuilder)? updates]) =>
      (EvaluationBatchRequestBuilder()..update(updates))._build();

  _$EvaluationBatchRequest._(
      {required this.entities,
      this.enableDebug,
      this.flagIDs,
      this.flagKeys,
      this.flagTags,
      this.flagTagsOperator})
      : super._();
  @override
  EvaluationBatchRequest rebuild(
          void Function(EvaluationBatchRequestBuilder) updates) =>
      (toBuilder()..update(updates)).build();

  @override
  EvaluationBatchRequestBuilder toBuilder() =>
      EvaluationBatchRequestBuilder()..replace(this);

  @override
  bool operator ==(Object other) {
    if (identical(other, this)) return true;
    return other is EvaluationBatchRequest &&
        entities == other.entities &&
        enableDebug == other.enableDebug &&
        flagIDs == other.flagIDs &&
        flagKeys == other.flagKeys &&
        flagTags == other.flagTags &&
        flagTagsOperator == other.flagTagsOperator;
  }

  @override
  int get hashCode {
    var _$hash = 0;
    _$hash = $jc(_$hash, entities.hashCode);
    _$hash = $jc(_$hash, enableDebug.hashCode);
    _$hash = $jc(_$hash, flagIDs.hashCode);
    _$hash = $jc(_$hash, flagKeys.hashCode);
    _$hash = $jc(_$hash, flagTags.hashCode);
    _$hash = $jc(_$hash, flagTagsOperator.hashCode);
    _$hash = $jf(_$hash);
    return _$hash;
  }

  @override
  String toString() {
    return (newBuiltValueToStringHelper(r'EvaluationBatchRequest')
          ..add('entities', entities)
          ..add('enableDebug', enableDebug)
          ..add('flagIDs', flagIDs)
          ..add('flagKeys', flagKeys)
          ..add('flagTags', flagTags)
          ..add('flagTagsOperator', flagTagsOperator))
        .toString();
  }
}

class EvaluationBatchRequestBuilder
    implements Builder<EvaluationBatchRequest, EvaluationBatchRequestBuilder> {
  _$EvaluationBatchRequest? _$v;

  ListBuilder<EvaluationEntity>? _entities;
  ListBuilder<EvaluationEntity> get entities =>
      _$this._entities ??= ListBuilder<EvaluationEntity>();
  set entities(ListBuilder<EvaluationEntity>? entities) =>
      _$this._entities = entities;

  bool? _enableDebug;
  bool? get enableDebug => _$this._enableDebug;
  set enableDebug(bool? enableDebug) => _$this._enableDebug = enableDebug;

  ListBuilder<int>? _flagIDs;
  ListBuilder<int> get flagIDs => _$this._flagIDs ??= ListBuilder<int>();
  set flagIDs(ListBuilder<int>? flagIDs) => _$this._flagIDs = flagIDs;

  ListBuilder<String>? _flagKeys;
  ListBuilder<String> get flagKeys =>
      _$this._flagKeys ??= ListBuilder<String>();
  set flagKeys(ListBuilder<String>? flagKeys) => _$this._flagKeys = flagKeys;

  ListBuilder<String>? _flagTags;
  ListBuilder<String> get flagTags =>
      _$this._flagTags ??= ListBuilder<String>();
  set flagTags(ListBuilder<String>? flagTags) => _$this._flagTags = flagTags;

  EvaluationBatchRequestFlagTagsOperatorEnum? _flagTagsOperator;
  EvaluationBatchRequestFlagTagsOperatorEnum? get flagTagsOperator =>
      _$this._flagTagsOperator;
  set flagTagsOperator(
          EvaluationBatchRequestFlagTagsOperatorEnum? flagTagsOperator) =>
      _$this._flagTagsOperator = flagTagsOperator;

  EvaluationBatchRequestBuilder() {
    EvaluationBatchRequest._defaults(this);
  }

  EvaluationBatchRequestBuilder get _$this {
    final $v = _$v;
    if ($v != null) {
      _entities = $v.entities.toBuilder();
      _enableDebug = $v.enableDebug;
      _flagIDs = $v.flagIDs?.toBuilder();
      _flagKeys = $v.flagKeys?.toBuilder();
      _flagTags = $v.flagTags?.toBuilder();
      _flagTagsOperator = $v.flagTagsOperator;
      _$v = null;
    }
    return this;
  }

  @override
  void replace(EvaluationBatchRequest other) {
    _$v = other as _$EvaluationBatchRequest;
  }

  @override
  void update(void Function(EvaluationBatchRequestBuilder)? updates) {
    if (updates != null) updates(this);
  }

  @override
  EvaluationBatchRequest build() => _build();

  _$EvaluationBatchRequest _build() {
    _$EvaluationBatchRequest _$result;
    try {
      _$result = _$v ??
          _$EvaluationBatchRequest._(
            entities: entities.build(),
            enableDebug: enableDebug,
            flagIDs: _flagIDs?.build(),
            flagKeys: _flagKeys?.build(),
            flagTags: _flagTags?.build(),
            flagTagsOperator: flagTagsOperator,
          );
    } catch (_) {
      late String _$failedField;
      try {
        _$failedField = 'entities';
        entities.build();

        _$failedField = 'flagIDs';
        _flagIDs?.build();
        _$failedField = 'flagKeys';
        _flagKeys?.build();
        _$failedField = 'flagTags';
        _flagTags?.build();
      } catch (e) {
        throw BuiltValueNestedFieldError(
            r'EvaluationBatchRequest', _$failedField, e.toString());
      }
      rethrow;
    }
    replace(_$result);
    return _$result;
  }
}

// ignore_for_file: deprecated_member_use_from_same_package,type=lint
