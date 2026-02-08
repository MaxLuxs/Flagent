//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_import

import 'package:one_of_serializer/any_of_serializer.dart';
import 'package:one_of_serializer/one_of_serializer.dart';
import 'package:built_collection/built_collection.dart';
import 'package:built_value/json_object.dart';
import 'package:built_value/serializer.dart';
import 'package:built_value/standard_json_plugin.dart';
import 'package:built_value/iso_8601_date_time_serializer.dart';
import 'package:flagent_client/src/date_serializer.dart';
import 'package:flagent_client/src/model/date.dart';

import 'package:flagent_client/src/model/constraint.dart';
import 'package:flagent_client/src/model/create_constraint_request.dart';
import 'package:flagent_client/src/model/create_flag_request.dart';
import 'package:flagent_client/src/model/create_segment_request.dart';
import 'package:flagent_client/src/model/create_tag_request.dart';
import 'package:flagent_client/src/model/create_variant_request.dart';
import 'package:flagent_client/src/model/distribution.dart';
import 'package:flagent_client/src/model/distribution_request.dart';
import 'package:flagent_client/src/model/error.dart';
import 'package:flagent_client/src/model/eval_context.dart';
import 'package:flagent_client/src/model/eval_debug_log.dart';
import 'package:flagent_client/src/model/eval_result.dart';
import 'package:flagent_client/src/model/evaluation_batch_request.dart';
import 'package:flagent_client/src/model/evaluation_batch_response.dart';
import 'package:flagent_client/src/model/evaluation_entity.dart';
import 'package:flagent_client/src/model/flag.dart';
import 'package:flagent_client/src/model/flag_snapshot.dart';
import 'package:flagent_client/src/model/health.dart';
import 'package:flagent_client/src/model/info.dart';
import 'package:flagent_client/src/model/put_constraint_request.dart';
import 'package:flagent_client/src/model/put_distributions_request.dart';
import 'package:flagent_client/src/model/put_flag_request.dart';
import 'package:flagent_client/src/model/put_segment_reorder_request.dart';
import 'package:flagent_client/src/model/put_segment_request.dart';
import 'package:flagent_client/src/model/put_variant_request.dart';
import 'package:flagent_client/src/model/segment.dart';
import 'package:flagent_client/src/model/segment_debug_log.dart';
import 'package:flagent_client/src/model/set_flag_enabled_request.dart';
import 'package:flagent_client/src/model/tag.dart';
import 'package:flagent_client/src/model/variant.dart';

part 'serializers.g.dart';

@SerializersFor([
  Constraint,
  CreateConstraintRequest,
  CreateFlagRequest,
  CreateSegmentRequest,
  CreateTagRequest,
  CreateVariantRequest,
  Distribution,
  DistributionRequest,
  Error,
  EvalContext,
  EvalDebugLog,
  EvalResult,
  EvaluationBatchRequest,
  EvaluationBatchResponse,
  EvaluationEntity,
  Flag,
  FlagSnapshot,
  Health,
  Info,
  PutConstraintRequest,
  PutDistributionsRequest,
  PutFlagRequest,
  PutSegmentReorderRequest,
  PutSegmentRequest,
  PutVariantRequest,
  Segment,
  SegmentDebugLog,
  SetFlagEnabledRequest,
  Tag,
  Variant,
])
Serializers serializers = (_$serializers.toBuilder()
      ..addBuilderFactory(
        const FullType(BuiltList, [FullType(Constraint)]),
        () => ListBuilder<Constraint>(),
      )
      ..addBuilderFactory(
        const FullType(BuiltList, [FullType(FlagSnapshot)]),
        () => ListBuilder<FlagSnapshot>(),
      )
      ..addBuilderFactory(
        const FullType(BuiltList, [FullType(Segment)]),
        () => ListBuilder<Segment>(),
      )
      ..addBuilderFactory(
        const FullType(BuiltList, [FullType(Flag)]),
        () => ListBuilder<Flag>(),
      )
      ..addBuilderFactory(
        const FullType(BuiltList, [FullType(Variant)]),
        () => ListBuilder<Variant>(),
      )
      ..addBuilderFactory(
        const FullType(BuiltList, [FullType(Distribution)]),
        () => ListBuilder<Distribution>(),
      )
      ..addBuilderFactory(
        const FullType(BuiltList, [FullType(String)]),
        () => ListBuilder<String>(),
      )
      ..addBuilderFactory(
        const FullType(BuiltList, [FullType(Tag)]),
        () => ListBuilder<Tag>(),
      )
      ..addBuilderFactory(
        const FullType(BuiltMap, [FullType(String), FullType(JsonObject)]),
        () => MapBuilder<String, JsonObject>(),
      )
      ..add(const OneOfSerializer())
      ..add(const AnyOfSerializer())
      ..add(const DateSerializer())
      ..add(Iso8601DateTimeSerializer())
    ).build();

Serializers standardSerializers =
    (serializers.toBuilder()..addPlugin(StandardJsonPlugin())).build();
