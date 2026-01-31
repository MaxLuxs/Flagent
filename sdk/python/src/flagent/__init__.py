"""
Flagent Python SDK - Feature flags and experimentation platform client.

Usage:
    >>> from flagent import FlagentClient
    >>> client = FlagentClient(base_url="http://localhost:18000/api/v1")
    >>> result = await client.evaluate(flag_key="new_feature", entity_id="user123")
    >>> if result.is_enabled():
    ...     # Show new feature
"""

from .client import FlagentClient
from .models import (
    EvaluationResult,
    EvaluationContext,
    Flag,
    Segment,
    Variant,
    Constraint,
)
from .exceptions import (
    FlagentError,
    FlagNotFoundError,
    EvaluationError,
    NetworkError,
)

__version__ = "0.1.0"
__all__ = [
    "FlagentClient",
    "EvaluationResult",
    "EvaluationContext",
    "Flag",
    "Segment",
    "Variant",
    "Constraint",
    "FlagentError",
    "FlagNotFoundError",
    "EvaluationError",
    "NetworkError",
]
