"""Flagent Python client with async/await support."""

from typing import Any, Dict, List, Optional

from flagent._generated import ApiClient, Configuration
from flagent._generated.api import EvaluationApi, ExportApi, FlagApi, HealthApi
from flagent._generated.exceptions import (
    ApiException,
    NotFoundException,
    ServiceException,
)
from flagent._generated.models import (
    EvalContext,
    EvalResult,
    EvaluationBatchRequest,
    EvaluationEntity,
    Flag,
)
from .models import EvaluationResult
from .exceptions import FlagentError, FlagNotFoundError, EvaluationError, NetworkError


def create_client(
    base_url: str,
    api_key: Optional[str] = None,
    timeout: float = 30.0,
    max_retries: int = 3,
) -> "FlagentClient":
    """
    Create a Flagent client (recommended entry point).

    Same as ``FlagentClient(base_url, ...)``; use this for consistency with
    other SDKs (Kotlin, JS, Go) that use create/builder.

    Args:
        base_url: Flagent API base URL (e.g. "https://api.example.com/api/v1").
        api_key: Optional Bearer token for auth.
        timeout: Request timeout in seconds.
        max_retries: Max retries on network errors.

    Returns:
        FlagentClient instance.
    """
    return FlagentClient(
        base_url=base_url,
        api_key=api_key,
        timeout=timeout,
        max_retries=max_retries,
    )


class FlagentClient:
    """
    Async Flagent client for feature flag evaluation and management.

    Wraps generated OpenAPI client with convenient API and error handling.
    """

    def __init__(
        self,
        base_url: str,
        api_key: Optional[str] = None,
        timeout: float = 30.0,
        max_retries: int = 3,
    ):
        """Initialize Flagent client."""
        self.base_url = base_url.rstrip("/")
        self.api_key = api_key
        self.timeout = timeout

        config = Configuration(
            host=self.base_url,
            api_key={"bearerAuth": api_key} if api_key else None,
            api_key_prefix={"bearerAuth": "Bearer"},
            retries=max_retries,
        )
        self._api_client = ApiClient(configuration=config)
        self._evaluation_api = EvaluationApi(self._api_client)
        self._flag_api = FlagApi(self._api_client)
        self._health_api = HealthApi(self._api_client)
        self._export_api = ExportApi(self._api_client)

    async def __aenter__(self):
        """Context manager entry."""
        return self

    async def __aexit__(self, exc_type, exc_val, exc_tb):
        """Context manager exit."""
        await self.close()

    async def close(self):
        """Close HTTP client and release resources."""
        await self._api_client.close()

    def _to_evaluation_result(self, result: EvalResult) -> EvaluationResult:
        """Convert generated EvalResult to our EvaluationResult."""
        return EvaluationResult.model_validate(result.model_dump(by_alias=True))

    def _convert_error(self, e: Exception, context: str = "") -> FlagentError:
        """Convert generated exceptions to our exception types."""
        if isinstance(e, NotFoundException):
            return FlagNotFoundError(f"{context}: {e}" if context else str(e))
        if isinstance(e, ApiException):
            if e.status and e.status >= 500:
                return NetworkError(f"{context}: {e}" if context else str(e))
            return EvaluationError(f"{context}: {e}" if context else str(e))
        if isinstance(e, (ConnectionError, OSError, TimeoutError)):
            return NetworkError(f"{context}: {e}" if context else str(e))
        return EvaluationError(f"{context}: {e}" if context else str(e))

    async def evaluate(
        self,
        flag_key: Optional[str] = None,
        flag_id: Optional[int] = None,
        entity_id: Optional[str] = None,
        entity_type: Optional[str] = None,
        entity_context: Optional[Dict[str, Any]] = None,
        enable_debug: bool = False,
    ) -> EvaluationResult:
        """Evaluate a feature flag."""
        ctx = EvalContext(
            flag_key=flag_key,
            flag_id=flag_id,
            entity_id=entity_id,
            entity_type=entity_type,
            entity_context=entity_context,
            enable_debug=enable_debug,
        )
        try:
            result = await self._evaluation_api.post_evaluation(
                eval_context=ctx,
                _request_timeout=self.timeout,
            )
            return self._to_evaluation_result(result)
        except Exception as e:
            if isinstance(e, NotFoundException):
                raise FlagNotFoundError(
                    f"Flag not found: {flag_key or flag_id}"
                ) from e
            raise self._convert_error(e, "Evaluation failed") from e

    async def is_enabled(
        self,
        flag_key: str,
        entity_id: Optional[str] = None,
        entity_type: Optional[str] = None,
        entity_context: Optional[Dict[str, Any]] = None,
    ) -> bool:
        """Return True if the flag evaluates to an enabled variant (variant_key is not null)."""
        result = await self.evaluate(
            flag_key=flag_key,
            entity_id=entity_id,
            entity_type=entity_type,
            entity_context=entity_context,
            enable_debug=False,
        )
        return result.is_enabled()

    async def evaluate_batch(
        self,
        entities: List[Dict[str, Any]],
        flag_keys: Optional[List[str]] = None,
        flag_ids: Optional[List[int]] = None,
        enable_debug: bool = False,
    ) -> List[EvaluationResult]:
        """Batch evaluate flags for multiple entities."""
        eval_entities = [
            EvaluationEntity(
                entity_id=e.get("entityID"),
                entity_type=e.get("entityType"),
                entity_context=e.get("entityContext"),
            )
            for e in entities
        ]
        request = EvaluationBatchRequest(
            entities=eval_entities,
            flag_keys=flag_keys,
            flag_ids=flag_ids,
            enable_debug=enable_debug,
        )
        try:
            response = await self._evaluation_api.post_evaluation_batch(
                evaluation_batch_request=request,
                _request_timeout=self.timeout,
            )
            return [
                self._to_evaluation_result(r) for r in response.evaluation_results
            ]
        except Exception as e:
            raise self._convert_error(e, "Batch evaluation failed") from e

    async def get_flag(self, flag_id: int) -> Flag:
        """Get flag by ID."""
        try:
            return await self._flag_api.get_flag(
                flag_id=flag_id,
                _request_timeout=self.timeout,
            )
        except NotFoundException as e:
            raise FlagNotFoundError(f"Flag not found: {flag_id}") from e
        except Exception as e:
            raise self._convert_error(e, "Failed to get flag") from e

    async def list_flags(
        self,
        limit: int = 100,
        offset: int = 0,
        enabled: Optional[bool] = None,
        preload: bool = True,
    ) -> List[Flag]:
        """List flags with optional filters."""
        try:
            return await self._flag_api.find_flags(
                limit=limit,
                offset=offset,
                enabled=enabled,
                preload=preload,
                _request_timeout=self.timeout,
            )
        except Exception as e:
            raise self._convert_error(e, "Failed to list flags") from e

    async def health_check(self) -> Dict[str, Any]:
        """Check server health."""
        try:
            health = await self._health_api.get_health(
                _request_timeout=self.timeout,
            )
            return {"status": health.status}
        except Exception as e:
            raise self._convert_error(e, "Health check failed") from e
