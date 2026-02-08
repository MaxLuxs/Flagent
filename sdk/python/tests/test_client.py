"""Tests for FlagentClient."""

import pytest
from unittest.mock import AsyncMock, patch

from flagent import FlagentClient
from flagent.models import EvaluationResult
from flagent.exceptions import FlagNotFoundError, NetworkError
from flagent._generated.models import EvalResult
from flagent._generated.exceptions import NotFoundException


class TestFlagentClientInit:
    def test_strips_trailing_slash(self):
        client = FlagentClient(base_url="http://localhost:18000/api/v1/")
        assert client.base_url == "http://localhost:18000/api/v1"

    def test_sets_api_key(self):
        client = FlagentClient(base_url="http://test", api_key="secret")
        assert client.api_key == "secret"


class TestFlagentClientEvaluate:
    @pytest.mark.asyncio
    async def test_evaluate_success(self):
        client = FlagentClient(base_url="http://test/api/v1")
        mock_result = EvalResult(flag_key="test_flag", variant_key="control")
        with patch.object(
            client._evaluation_api,
            "post_evaluation",
            new_callable=AsyncMock,
            return_value=mock_result,
        ) as mock_post:
            result = await client.evaluate(
                flag_key="test_flag",
                entity_id="user1",
            )
            assert isinstance(result, EvaluationResult)
            assert result.flag_key == "test_flag"
            assert result.variant_key == "control"
            assert result.is_enabled() is True
            mock_post.assert_called_once()

    @pytest.mark.asyncio
    async def test_evaluate_404_raises_flag_not_found(self):
        client = FlagentClient(base_url="http://test/api/v1")
        with patch.object(
            client._evaluation_api,
            "post_evaluation",
            new_callable=AsyncMock,
            side_effect=NotFoundException(status=404, reason="Not Found", body="Flag not found"),
        ):
            with pytest.raises(FlagNotFoundError):
                await client.evaluate(flag_key="missing")

    @pytest.mark.asyncio
    async def test_evaluate_network_error(self):
        client = FlagentClient(base_url="http://test/api/v1")
        with patch.object(
            client._evaluation_api,
            "post_evaluation",
            new_callable=AsyncMock,
            side_effect=ConnectionError("Connection refused"),
        ):
            with pytest.raises(NetworkError):
                await client.evaluate(flag_key="test")


class TestFlagentClientEvaluateBatch:
    @pytest.mark.asyncio
    async def test_evaluate_batch_success(self):
        from flagent._generated.models import EvaluationBatchResponse

        client = FlagentClient(base_url="http://test/api/v1")
        mock_response = EvaluationBatchResponse(
            evaluation_results=[
                EvalResult(flag_key="f1", variant_key="control"),
                EvalResult(flag_key="f2", variant_key="variant_a"),
            ]
        )
        with patch.object(
            client._evaluation_api,
            "post_evaluation_batch",
            new_callable=AsyncMock,
            return_value=mock_response,
        ):
            results = await client.evaluate_batch(
                entities=[{"entityID": "user1"}],
                flag_keys=["f1", "f2"],
            )
            assert len(results) == 2
            assert results[0].flag_key == "f1"
            assert results[1].variant_key == "variant_a"


class TestFlagentClientHealthCheck:
    @pytest.mark.asyncio
    async def test_health_check_success(self):
        from flagent._generated.models import Health

        client = FlagentClient(base_url="http://test/api/v1")
        mock_health = Health(status="ok")
        with patch.object(
            client._health_api,
            "get_health",
            new_callable=AsyncMock,
            return_value=mock_health,
        ):
            health = await client.health_check()
            assert health == {"status": "ok"}


class TestFlagentClientContextManager:
    @pytest.mark.asyncio
    async def test_async_context_manager(self):
        with patch.object(
            FlagentClient,
            "close",
            new_callable=AsyncMock,
        ):
            async with FlagentClient(base_url="http://test/api/v1") as client:
                assert client is not None
