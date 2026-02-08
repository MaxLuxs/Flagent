"""Tests for Flagent models."""

import pytest
from pydantic import ValidationError

from flagent.models import (
    Constraint,
    Distribution,
    EvaluationContext,
    EvaluationResult,
    Flag,
    Segment,
    Variant,
    BatchEvaluationRequest,
    BatchEvaluationResponse,
)


class TestEvaluationResult:
    def test_is_enabled_when_variant_assigned(self):
        result = EvaluationResult(variant_key="control")
        assert result.is_enabled() is True

    def test_is_enabled_when_no_variant(self):
        result = EvaluationResult(variant_key=None)
        assert result.is_enabled() is False

    def test_get_attachment_value_exists(self):
        result = EvaluationResult(
            variant_key="control",
            variant_attachment={"config": "value"},
        )
        assert result.get_attachment_value("config") == "value"

    def test_get_attachment_value_missing_returns_default(self):
        result = EvaluationResult(variant_key="control", variant_attachment={})
        assert result.get_attachment_value("missing", "default") == "default"

    def test_get_attachment_value_no_attachment(self):
        result = EvaluationResult(variant_key="control")
        assert result.get_attachment_value("key") is None


class TestEvaluationContext:
    def test_from_dict_with_alias(self):
        ctx = EvaluationContext(
            flag_key="test",
            entity_id="user1",
            entity_context={"tier": "premium"},
        )
        assert ctx.flag_key == "test"
        assert ctx.entity_id == "user1"
        assert ctx.entity_context == {"tier": "premium"}

    def test_model_dump_excludes_none(self):
        ctx = EvaluationContext(flag_key="test")
        d = ctx.model_dump(by_alias=True, exclude_none=True)
        assert "flagKey" in d
        assert "entityID" not in d or d.get("entityID") is not None


class TestFlag:
    def test_minimal_flag(self):
        flag = Flag(
            id=1,
            key="test_flag",
            description="",
            enabled=True,
            data_records_enabled=False,
        )
        assert flag.key == "test_flag"
        assert flag.enabled is True
        assert flag.segments is None or flag.segments == []
        assert flag.variants is None or flag.variants == []

    def test_flag_with_segments(self):
        segment = Segment(
            id=1,
            flag_id=1,
            description="",
            rank=0,
            rollout_percent=100,
        )
        flag = Flag(
            id=1,
            key="test",
            description="",
            enabled=True,
            data_records_enabled=False,
            segments=[segment],
        )
        assert len(flag.segments) == 1
        assert flag.segments[0].rank == 0


class TestBatchEvaluationRequest:
    def test_minimal_request(self):
        from flagent._generated.models.evaluation_entity import EvaluationEntity

        req = BatchEvaluationRequest(
            entities=[EvaluationEntity(entity_id="u1")],
        )
        assert len(req.entities) == 1
        assert req.flag_keys is None

    def test_with_flag_keys(self):
        from flagent._generated.models.evaluation_entity import EvaluationEntity

        req = BatchEvaluationRequest(
            entities=[EvaluationEntity(entity_id="u1")],
            flag_keys=["f1", "f2"],
        )
        assert req.flag_keys == ["f1", "f2"]
