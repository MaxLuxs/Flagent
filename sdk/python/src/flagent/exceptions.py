"""Exceptions for Flagent Python SDK."""


class FlagentError(Exception):
    """Base exception for Flagent SDK."""
    pass


class FlagNotFoundError(FlagentError):
    """Flag not found exception."""
    pass


class EvaluationError(FlagentError):
    """Evaluation error exception."""
    pass


class NetworkError(FlagentError):
    """Network error exception."""
    pass


class InvalidConfigError(FlagentError):
    """Invalid configuration exception."""
    pass
