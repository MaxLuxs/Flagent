"""Data models for Flagent Python SDK."""

from typing import Dict, List, Optional, Any
from pydantic import BaseModel, Field


class Constraint(BaseModel):
    """Flag constraint model."""
    
    id: Optional[int] = None
    property: str
    operator: str
    value: Optional[str] = None


class Distribution(BaseModel):
    """Variant distribution model."""
    
    id: Optional[int] = None
    variant_id: int = Field(alias="variantID")
    variant_key: str = Field(alias="variantKey")
    percent: int


class Segment(BaseModel):
    """Flag segment model."""
    
    id: Optional[int] = None
    flag_id: Optional[int] = Field(None, alias="flagID")
    rank: int
    rollout_percent: int = Field(alias="rolloutPercent")
    constraints: List[Constraint] = Field(default_factory=list)
    distributions: List[Distribution] = Field(default_factory=list)
    description: Optional[str] = None


class Variant(BaseModel):
    """Flag variant model."""
    
    id: Optional[int] = None
    flag_id: Optional[int] = Field(None, alias="flagID")
    key: str
    attachment: Optional[Dict[str, Any]] = None


class Flag(BaseModel):
    """Flag model."""
    
    id: Optional[int] = None
    key: str
    enabled: bool = True
    description: Optional[str] = None
    segments: List[Segment] = Field(default_factory=list)
    variants: List[Variant] = Field(default_factory=list)
    entity_type: Optional[str] = Field(None, alias="entityType")
    created_by: Optional[str] = Field(None, alias="createdBy")
    updated_by: Optional[str] = Field(None, alias="updatedBy")
    updated_at: Optional[int] = Field(None, alias="updatedAt")


class EvaluationContext(BaseModel):
    """Evaluation context for flag evaluation."""
    
    flag_key: Optional[str] = Field(None, alias="flagKey")
    flag_id: Optional[int] = Field(None, alias="flagID")
    entity_id: Optional[str] = Field(None, alias="entityID")
    entity_type: Optional[str] = Field(None, alias="entityType")
    entity_context: Optional[Dict[str, Any]] = Field(None, alias="entityContext")
    enable_debug: bool = Field(False, alias="enableDebug")


class EvaluationResult(BaseModel):
    """Evaluation result model."""
    
    flag_id: Optional[int] = Field(None, alias="flagID")
    flag_key: Optional[str] = Field(None, alias="flagKey")
    variant_id: Optional[int] = Field(None, alias="variantID")
    variant_key: Optional[str] = Field(None, alias="variantKey")
    variant_attachment: Optional[Dict[str, Any]] = Field(None, alias="variantAttachment")
    segment_id: Optional[int] = Field(None, alias="segmentID")
    evaluation_timestamp: Optional[int] = Field(None, alias="evaluationTimestamp")
    debug_logs: List[str] = Field(default_factory=list, alias="debugLogs")
    
    def is_enabled(self) -> bool:
        """Check if flag is enabled (has variant assigned)."""
        return self.variant_key is not None
    
    def get_attachment_value(self, key: str, default: Any = None) -> Any:
        """Get value from variant attachment."""
        if not self.variant_attachment:
            return default
        return self.variant_attachment.get(key, default)


class BatchEvaluationRequest(BaseModel):
    """Batch evaluation request model."""
    
    entities: List[Dict[str, Any]]
    flag_keys: Optional[List[str]] = Field(None, alias="flagKeys")
    flag_ids: Optional[List[int]] = Field(None, alias="flagIDs")
    enable_debug: bool = Field(False, alias="enableDebug")


class BatchEvaluationResponse(BaseModel):
    """Batch evaluation response model."""
    
    evaluation_results: List[EvaluationResult] = Field(alias="evaluationResults")
