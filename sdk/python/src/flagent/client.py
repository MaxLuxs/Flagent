"""Flagent Python client with async/await support."""

import httpx
from typing import Dict, List, Optional, Any
from .models import (
    EvaluationResult,
    EvaluationContext,
    Flag,
    BatchEvaluationRequest,
    BatchEvaluationResponse,
)
from .exceptions import FlagentError, FlagNotFoundError, EvaluationError, NetworkError


class FlagentClient:
    """
    Async Flagent client for feature flag evaluation and management.
    
    Features:
    - Async/await API using httpx
    - Type-safe with Pydantic models
    - Automatic retry on network errors
    - Connection pooling
    
    Example:
        >>> client = FlagentClient(base_url="http://localhost:18000/api/v1")
        >>> result = await client.evaluate(
        ...     flag_key="new_feature",
        ...     entity_id="user123",
        ...     entity_context={"tier": "premium"}
        ... )
        >>> if result.is_enabled():
        ...     # Feature is enabled
    """
    
    def __init__(
        self,
        base_url: str,
        api_key: Optional[str] = None,
        timeout: float = 30.0,
        max_retries: int = 3,
    ):
        """
        Initialize Flagent client.
        
        Args:
            base_url: Base URL of Flagent server (e.g., "http://localhost:18000/api/v1")
            api_key: Optional API key for authentication
            timeout: Request timeout in seconds
            max_retries: Maximum number of retry attempts on network errors
        """
        self.base_url = base_url.rstrip("/")
        self.api_key = api_key
        self.timeout = timeout
        
        # Configure httpx client with retries
        self._client = httpx.AsyncClient(
            timeout=timeout,
            transport=httpx.AsyncHTTPTransport(retries=max_retries),
        )
        
        # Set auth header if API key provided
        if api_key:
            self._client.headers["Authorization"] = f"Bearer {api_key}"
    
    async def __aenter__(self):
        """Context manager entry."""
        return self
    
    async def __aexit__(self, exc_type, exc_val, exc_tb):
        """Context manager exit."""
        await self.close()
    
    async def close(self):
        """Close HTTP client and release resources."""
        await self._client.aclose()
    
    async def evaluate(
        self,
        flag_key: Optional[str] = None,
        flag_id: Optional[int] = None,
        entity_id: Optional[str] = None,
        entity_type: Optional[str] = None,
        entity_context: Optional[Dict[str, Any]] = None,
        enable_debug: bool = False,
    ) -> EvaluationResult:
        """
        Evaluate a feature flag.
        
        Args:
            flag_key: Flag key to evaluate (required if flag_id not provided)
            flag_id: Flag ID to evaluate (required if flag_key not provided)
            entity_id: Entity ID for consistent bucketing
            entity_type: Entity type (e.g., "user", "session")
            entity_context: Additional context for constraint matching
            enable_debug: Enable debug logging
        
        Returns:
            EvaluationResult with variant assignment
        
        Raises:
            FlagNotFoundError: If flag not found
            EvaluationError: If evaluation fails
            NetworkError: If network request fails
        
        Example:
            >>> result = await client.evaluate(
            ...     flag_key="new_checkout",
            ...     entity_id="user123",
            ...     entity_context={"region": "US", "tier": "premium"}
            ... )
            >>> print(f"Variant: {result.variant_key}")
            >>> print(f"Enabled: {result.is_enabled()}")
        """
        ctx = EvaluationContext(
            flagKey=flag_key,
            flagID=flag_id,
            entityID=entity_id,
            entityType=entity_type,
            entityContext=entity_context,
            enableDebug=enable_debug,
        )
        
        try:
            response = await self._client.post(
                f"{self.base_url}/evaluation",
                json=ctx.model_dump(by_alias=True, exclude_none=True),
            )
            response.raise_for_status()
            
            data = response.json()
            return EvaluationResult(**data)
            
        except httpx.HTTPStatusError as e:
            if e.response.status_code == 404:
                raise FlagNotFoundError(f"Flag not found: {flag_key or flag_id}")
            raise EvaluationError(f"Evaluation failed: {e}")
        except httpx.RequestError as e:
            raise NetworkError(f"Network error: {e}")
    
    async def evaluate_batch(
        self,
        entities: List[Dict[str, Any]],
        flag_keys: Optional[List[str]] = None,
        flag_ids: Optional[List[int]] = None,
        enable_debug: bool = False,
    ) -> List[EvaluationResult]:
        """
        Batch evaluate flags for multiple entities.
        
        Args:
            entities: List of entities to evaluate
            flag_keys: List of flag keys to evaluate
            flag_ids: List of flag IDs to evaluate
            enable_debug: Enable debug logging
        
        Returns:
            List of evaluation results
        
        Raises:
            EvaluationError: If evaluation fails
            NetworkError: If network request fails
        
        Example:
            >>> results = await client.evaluate_batch(
            ...     entities=[
            ...         {"entityID": "user1", "entityContext": {"tier": "free"}},
            ...         {"entityID": "user2", "entityContext": {"tier": "premium"}},
            ...     ],
            ...     flag_keys=["feature_a", "feature_b"]
            ... )
            >>> for result in results:
            ...     print(f"{result.flag_key}: {result.variant_key}")
        """
        request = BatchEvaluationRequest(
            entities=entities,
            flagKeys=flag_keys,
            flagIDs=flag_ids,
            enableDebug=enable_debug,
        )
        
        try:
            response = await self._client.post(
                f"{self.base_url}/evaluation/batch",
                json=request.model_dump(by_alias=True, exclude_none=True),
            )
            response.raise_for_status()
            
            data = response.json()
            batch_response = BatchEvaluationResponse(**data)
            return batch_response.evaluation_results
            
        except httpx.HTTPStatusError as e:
            raise EvaluationError(f"Batch evaluation failed: {e}")
        except httpx.RequestError as e:
            raise NetworkError(f"Network error: {e}")
    
    async def get_flag(self, flag_id: int) -> Flag:
        """
        Get flag by ID.
        
        Args:
            flag_id: Flag ID
        
        Returns:
            Flag model
        
        Raises:
            FlagNotFoundError: If flag not found
            NetworkError: If network request fails
        """
        try:
            response = await self._client.get(f"{self.base_url}/flags/{flag_id}")
            response.raise_for_status()
            
            data = response.json()
            return Flag(**data)
            
        except httpx.HTTPStatusError as e:
            if e.response.status_code == 404:
                raise FlagNotFoundError(f"Flag not found: {flag_id}")
            raise FlagentError(f"Failed to get flag: {e}")
        except httpx.RequestError as e:
            raise NetworkError(f"Network error: {e}")
    
    async def list_flags(
        self,
        limit: int = 100,
        offset: int = 0,
        enabled: Optional[bool] = None,
        preload: bool = True,
    ) -> List[Flag]:
        """
        List flags with optional filters.
        
        Args:
            limit: Maximum number of flags to return
            offset: Offset for pagination
            enabled: Filter by enabled status
            preload: Preload segments, variants, etc.
        
        Returns:
            List of flags
        
        Raises:
            NetworkError: If network request fails
        
        Example:
            >>> flags = await client.list_flags(limit=50, enabled=True)
            >>> for flag in flags:
            ...     print(f"{flag.key}: {flag.enabled}")
        """
        params = {
            "limit": limit,
            "offset": offset,
            "preload": preload,
        }
        if enabled is not None:
            params["enabled"] = enabled
        
        try:
            response = await self._client.get(
                f"{self.base_url}/flags",
                params=params,
            )
            response.raise_for_status()
            
            data = response.json()
            return [Flag(**item) for item in data]
            
        except httpx.HTTPStatusError as e:
            raise FlagentError(f"Failed to list flags: {e}")
        except httpx.RequestError as e:
            raise NetworkError(f"Network error: {e}")
    
    async def health_check(self) -> Dict[str, Any]:
        """
        Check server health.
        
        Returns:
            Health status dict
        
        Raises:
            NetworkError: If health check fails
        """
        try:
            response = await self._client.get(f"{self.base_url}/health")
            response.raise_for_status()
            return response.json()
        except httpx.RequestError as e:
            raise NetworkError(f"Health check failed: {e}")
