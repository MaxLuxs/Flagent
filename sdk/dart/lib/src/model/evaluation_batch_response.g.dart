// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'evaluation_batch_response.dart';

// **************************************************************************
// BuiltValueGenerator
// **************************************************************************

class _$EvaluationBatchResponse extends EvaluationBatchResponse {
  @override
  final BuiltList<EvalResult> evaluationResults;

  factory _$EvaluationBatchResponse(
          [void Function(EvaluationBatchResponseBuilder)? updates]) =>
      (EvaluationBatchResponseBuilder()..update(updates))._build();

  _$EvaluationBatchResponse._({required this.evaluationResults}) : super._();
  @override
  EvaluationBatchResponse rebuild(
          void Function(EvaluationBatchResponseBuilder) updates) =>
      (toBuilder()..update(updates)).build();

  @override
  EvaluationBatchResponseBuilder toBuilder() =>
      EvaluationBatchResponseBuilder()..replace(this);

  @override
  bool operator ==(Object other) {
    if (identical(other, this)) return true;
    return other is EvaluationBatchResponse &&
        evaluationResults == other.evaluationResults;
  }

  @override
  int get hashCode {
    var _$hash = 0;
    _$hash = $jc(_$hash, evaluationResults.hashCode);
    _$hash = $jf(_$hash);
    return _$hash;
  }

  @override
  String toString() {
    return (newBuiltValueToStringHelper(r'EvaluationBatchResponse')
          ..add('evaluationResults', evaluationResults))
        .toString();
  }
}

class EvaluationBatchResponseBuilder
    implements
        Builder<EvaluationBatchResponse, EvaluationBatchResponseBuilder> {
  _$EvaluationBatchResponse? _$v;

  ListBuilder<EvalResult>? _evaluationResults;
  ListBuilder<EvalResult> get evaluationResults =>
      _$this._evaluationResults ??= ListBuilder<EvalResult>();
  set evaluationResults(ListBuilder<EvalResult>? evaluationResults) =>
      _$this._evaluationResults = evaluationResults;

  EvaluationBatchResponseBuilder() {
    EvaluationBatchResponse._defaults(this);
  }

  EvaluationBatchResponseBuilder get _$this {
    final $v = _$v;
    if ($v != null) {
      _evaluationResults = $v.evaluationResults.toBuilder();
      _$v = null;
    }
    return this;
  }

  @override
  void replace(EvaluationBatchResponse other) {
    _$v = other as _$EvaluationBatchResponse;
  }

  @override
  void update(void Function(EvaluationBatchResponseBuilder)? updates) {
    if (updates != null) updates(this);
  }

  @override
  EvaluationBatchResponse build() => _build();

  _$EvaluationBatchResponse _build() {
    _$EvaluationBatchResponse _$result;
    try {
      _$result = _$v ??
          _$EvaluationBatchResponse._(
            evaluationResults: evaluationResults.build(),
          );
    } catch (_) {
      late String _$failedField;
      try {
        _$failedField = 'evaluationResults';
        evaluationResults.build();
      } catch (e) {
        throw BuiltValueNestedFieldError(
            r'EvaluationBatchResponse', _$failedField, e.toString());
      }
      rethrow;
    }
    replace(_$result);
    return _$result;
  }
}

// ignore_for_file: deprecated_member_use_from_same_package,type=lint
