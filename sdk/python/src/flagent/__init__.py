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

__version__ = "0.1.5"
__all__ = [
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
