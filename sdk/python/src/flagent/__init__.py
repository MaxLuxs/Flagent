"""
Flagent Python SDK - Feature flags and experimentation platform client.

Usage (recommended):
    >>> from flagent import create_client
    >>> client = create_client("http://localhost:18000/api/v1")
    >>> result = await client.evaluate(flag_key="new_feature", entity_id="user123")
    >>> if result.is_enabled():
    ...     # Show new feature
"""

from .client import FlagentClient, create_client
from .models import EvaluationResult
from .exceptions import (
    FlagentError,
    FlagNotFoundError,
    EvaluationError,
    NetworkError,
    InvalidConfigError,
)

# Re-export generated models for advanced usage
from flagent._generated.models import Flag, EvalContext

__version__ = "0.1.6"
__all__ = [
    "create_client",
    "FlagentClient",
    "EvaluationResult",
    "Flag",
    "EvalContext",
    "FlagentError",
    "FlagNotFoundError",
    "EvaluationError",
    "NetworkError",
    "InvalidConfigError",
]
