"""Tests for FlagentClient."""

import pytest
from unittest.mock import AsyncMock, patch

from flagent import FlagentClient, create_client
from flagent.models import EvaluationResult
from flagent.exceptions import EvaluationError, FlagNotFoundError, NetworkError
from flagent._generated.models import EvalResult
from flagent._generated.exceptions import ApiException, NotFoundException


class TestFlagentClientInit:
    def test_strips_trailing_slash(self):
        client = FlagentClient(base_url="http://localhost:18000/api/v1/")
        assert client.base_url == "http://localhost:18000/api/v1"

    def test_sets_api_key(self):
        client = FlagentClient(base_url="http://test", api_key="secret")
        assert client.api_key == "secret"


class TestCreateClient:
    def test_create_client_returns_flagent_client(self):
        client = create_client("http://localhost:18000/api/v1")
        assert isinstance(client, FlagentClient)
        assert client.base_url == "http://localhost:18000/api/v1"

    def test_create_client_with_options(self):
        client = create_client(
            "http://api.example.com/api/v1",
            api_key="key",
            timeout=10.0,
            max_retries=5,
        )
        assert client.api_key == "key"
        assert client.timeout == 10.0


class TestFlagentClientIsEnabled:
    @pytest.mark.asyncio
    async def test_is_enabled_true_when_variant_assigned(self):
        client = FlagentClient(base_url="http://test/api/v1")
        with patch.object(
            client._evaluation_api,
            "post_evaluation",
            new_callable=AsyncMock,
            return_value=EvalResult(flag_key="f", variant_key="on"),
        ):
            assert await client.is_enabled("f", entity_id="user1") is True

    @pytest.mark.asyncio
    async def test_is_enabled_false_when_no_variant(self):
        client = FlagentClient(base_url="http://test/api/v1")
        with patch.object(
            client._evaluation_api,
            "post_evaluation",
            new_callable=AsyncMock,
            return_value=EvalResult(flag_key="f", variant_key=None),
        ):
            assert await client.is_enabled("f", entity_id="user1") is False


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

    @pytest.mark.asyncio
    async def test_evaluate_api_error_raises_evaluation_error(self):
        client = FlagentClient(base_url="http://test/api/v1")
        with patch.object(
            client._evaluation_api,
            "post_evaluation",
            new_callable=AsyncMock,
            side_effect=ApiException(status=400, reason="Bad Request", body="Invalid context"),
        ):
            with pytest.raises(EvaluationError):
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

    @pytest.mark.asyncio
    async def test_evaluate_batch_error(self):
        client = FlagentClient(base_url="http://test/api/v1")
        with patch.object(
            client._evaluation_api,
            "post_evaluation_batch",
            new_callable=AsyncMock,
            side_effect=ApiException(status=400, reason="Bad Request", body="Invalid request"),
        ):
            with pytest.raises(EvaluationError):
                await client.evaluate_batch(
                    entities=[{"entityID": "user1"}],
                    flag_keys=["f1"],
                )


class TestFlagentClientGetFlag:
    @pytest.mark.asyncio
    async def test_get_flag_success(self):
        from flagent._generated.models import Flag

        client = FlagentClient(base_url="http://test/api/v1")
        mock_flag = Flag(
            id=1,
            key="test_flag",
            description="",
            enabled=True,
            data_records_enabled=False,
        )
        with patch.object(
            client._flag_api,
            "get_flag",
            new_callable=AsyncMock,
            return_value=mock_flag,
        ):
            flag = await client.get_flag(flag_id=1)
            assert flag.id == 1
            assert flag.key == "test_flag"
            assert flag.enabled is True

    @pytest.mark.asyncio
    async def test_get_flag_404_raises_flag_not_found(self):
        client = FlagentClient(base_url="http://test/api/v1")
        with patch.object(
            client._flag_api,
            "get_flag",
            new_callable=AsyncMock,
            side_effect=NotFoundException(status=404, reason="Not Found", body="Flag not found"),
        ):
            with pytest.raises(FlagNotFoundError):
                await client.get_flag(flag_id=999)


class TestFlagentClientListFlags:
    @pytest.mark.asyncio
    async def test_list_flags_success(self):
        from flagent._generated.models import Flag

        client = FlagentClient(base_url="http://test/api/v1")
        mock_flags = [
            Flag(id=1, key="f1", description="", enabled=True, data_records_enabled=False),
            Flag(id=2, key="f2", description="", enabled=False, data_records_enabled=False),
        ]
        with patch.object(
            client._flag_api,
            "find_flags",
            new_callable=AsyncMock,
            return_value=mock_flags,
        ):
            flags = await client.list_flags(limit=10, offset=0)
            assert len(flags) == 2
            assert flags[0].key == "f1"
            assert flags[1].key == "f2"


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

    @pytest.mark.asyncio
    async def test_close(self):
        client = FlagentClient(base_url="http://test/api/v1")
        with patch.object(client._api_client, "close", new_callable=AsyncMock) as mock_close:
            await client.close()
            mock_close.assert_called_once()


class TestFlagentClientConvertError:
    @pytest.mark.asyncio
    async def test_evaluate_batch_404_raises_flag_not_found(self):
        client = FlagentClient(base_url="http://test/api/v1")
        with patch.object(
            client._evaluation_api,
            "post_evaluation_batch",
            new_callable=AsyncMock,
            side_effect=NotFoundException(status=404, reason="Not Found", body="Not found"),
        ):
            with pytest.raises(FlagNotFoundError):
                await client.evaluate_batch(
                    entities=[{"entityID": "user1"}],
                    flag_keys=["f1"],
                )

    @pytest.mark.asyncio
    async def test_api_exception_500_raises_network_error(self):
        client = FlagentClient(base_url="http://test/api/v1")
        with patch.object(
            client._evaluation_api,
            "post_evaluation",
            new_callable=AsyncMock,
            side_effect=ApiException(status=503, reason="Service Unavailable", body="Unavailable"),
        ):
            with pytest.raises(NetworkError):
                await client.evaluate(flag_key="test")

    @pytest.mark.asyncio
    async def test_get_flag_api_error(self):
        client = FlagentClient(base_url="http://test/api/v1")
        with patch.object(
            client._flag_api,
            "get_flag",
            new_callable=AsyncMock,
            side_effect=ApiException(status=400, reason="Bad Request", body="Invalid"),
        ):
            with pytest.raises(EvaluationError):
                await client.get_flag(flag_id=1)

    @pytest.mark.asyncio
    async def test_list_flags_error(self):
        client = FlagentClient(base_url="http://test/api/v1")
        with patch.object(
            client._flag_api,
            "find_flags",
            new_callable=AsyncMock,
            side_effect=ApiException(status=500, reason="Server Error", body="Error"),
        ):
            with pytest.raises(NetworkError):
                await client.list_flags()

    @pytest.mark.asyncio
    async def test_health_check_error(self):
        client = FlagentClient(base_url="http://test/api/v1")
        with patch.object(
            client._health_api,
            "get_health",
            new_callable=AsyncMock,
            side_effect=TimeoutError("Request timeout"),
        ):
            with pytest.raises(NetworkError):
                await client.health_check()

    @pytest.mark.asyncio
    async def test_generic_exception_raises_evaluation_error(self):
        client = FlagentClient(base_url="http://test/api/v1")
        with patch.object(
            client._evaluation_api,
            "post_evaluation",
            new_callable=AsyncMock,
            side_effect=RuntimeError("unexpected"),
        ):
            with pytest.raises(EvaluationError):
                await client.evaluate(flag_key="test")
