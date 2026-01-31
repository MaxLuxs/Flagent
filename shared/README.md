# Flagent Shared Module

Shared Kotlin Multiplatform module used by backend, frontend, ktor-flagent, and SDKs.

## Package Structure

### `flagent.api` — API contracts and models

- **constants/** — ApiConstants, ConstraintOperators, FlagTagsOperator
- **model/** — Request/Response models for REST API
  - `CommonModels.kt` — simplified models for UI lists/cards (Flag, Tag, UpdateFlagRequest)
  - `FlagModels.kt` — full API models (CreateFlagRequest, PutFlagRequest, FlagResponse, SegmentResponse, etc.)
  - `EvaluationModels.kt` — evaluation request/response
  - `InfoModels.kt` — info/health models
- **validation/** — FlagValidation
- **CoreFlagRepository.kt** — interface for flag repository (enterprise)
- **CoreSegmentService.kt** — interface for segment service (enterprise)

### `flagent.evaluator` — Evaluation engine

Evaluation logic lives here (pure Kotlin, no Ktor):

- **FlagEvaluator.kt** — main evaluator: evaluates flag for entityID, applies constraints, rollout, distributions
- **ConstraintEvaluator.kt** — evaluates constraint matching (EQ, NEQ, IN, etc.)
- **VariantSelector.kt** — selects variant from distribution

The evaluator uses `EvaluableFlag`, `EvaluableSegment`, `EvaluableConstraint`, `EvaluableDistribution` data classes for evaluation. Backend domain entities are converted to these via `SharedFlagEvaluatorAdapter`.

## Usage

- **Backend**: depends on shared for API models, evaluator, validation. Routes use `flagent.api.model.*`, services use evaluator via adapter.
- **Frontend**: depends on shared for API models, ApiClient uses CreateFlagRequest, FlagResponse, etc.
- **SDKs** (kotlin-enhanced, etc.): may depend on shared for evaluator to ensure consistent evaluation with backend.

## API vs Evaluator

| Package    | Purpose                          | Dependencies      |
|-----------|-----------------------------------|-------------------|
| api       | REST contract, validation, types | kotlinx.serialization |
| evaluator | Flag evaluation, constraints     | Kotlin stdlib     |
