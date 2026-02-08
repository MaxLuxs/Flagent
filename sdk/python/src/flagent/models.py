"""Wrapper models with convenience methods on top of generated models."""

from typing import Any

from flagent._generated.models.eval_result import EvalResult
from flagent._generated.models.eval_context import EvalContext
from flagent._generated.models.flag import Flag
from flagent._generated.models.constraint import Constraint
from flagent._generated.models.distribution import Distribution
from flagent._generated.models.segment import Segment
from flagent._generated.models.variant import Variant
from flagent._generated.models.evaluation_batch_request import EvaluationBatchRequest
from flagent._generated.models.evaluation_batch_response import EvaluationBatchResponse
from flagent._generated.models.evaluation_entity import EvaluationEntity

# Aliases for backward compatibility
EvaluationContext = EvalContext
BatchEvaluationRequest = EvaluationBatchRequest
BatchEvaluationResponse = EvaluationBatchResponse


class EvaluationResult(EvalResult):
    """Evaluation result with convenience methods."""

    def is_enabled(self) -> bool:
        """Check if flag is enabled (has variant assigned)."""
        return self.variant_key is not None

    def get_attachment_value(self, key: str, default: Any = None) -> Any:
        """Get value from variant attachment."""
        if not self.variant_attachment:
            return default
        return self.variant_attachment.get(key, default)


__all__ = [
    "EvaluationResult",
    "EvalContext",
    "EvaluationContext",
    "Flag",
    "Constraint",
    "Distribution",
    "Segment",
    "Variant",
    "EvaluationBatchRequest",
    "BatchEvaluationRequest",
    "EvaluationBatchResponse",
    "BatchEvaluationResponse",
    "EvaluationEntity",
]
