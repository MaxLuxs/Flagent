package flagent.service

import flagent.domain.entity.Constraint
import flagent.domain.repository.IConstraintRepository
import flagent.domain.repository.ISegmentRepository

/**
 * Constraint service - handles constraint business logic
 * Maps to CRUD operations from pkg/handler/crud.go
 */
class ConstraintService(
    private val constraintRepository: IConstraintRepository,
    private val segmentRepository: ISegmentRepository,
    private val flagSnapshotService: FlagSnapshotService? = null
) {
    suspend fun findConstraintsBySegmentId(segmentId: Int): List<Constraint> {
        return constraintRepository.findBySegmentId(segmentId)
    }
    
    suspend fun getConstraint(id: Int): Constraint? {
        return constraintRepository.findById(id)
    }
    
    suspend fun createConstraint(constraint: Constraint, updatedBy: String? = null): Constraint {
        constraint.validate()
        
        // Get segment to know flagId
        val segment = segmentRepository.findById(constraint.segmentId)
            ?: throw IllegalArgumentException("error finding segmentID ${constraint.segmentId}")
        
        val created = constraintRepository.create(constraint)
        
        // Save flag snapshot after creating constraint
        flagSnapshotService?.saveFlagSnapshot(segment.flagId, updatedBy)
        
        return created
    }
    
    suspend fun updateConstraint(constraint: Constraint, updatedBy: String? = null): Constraint {
        constraint.validate()
        
        // Get segment to know flagId
        val segment = segmentRepository.findById(constraint.segmentId)
            ?: throw IllegalArgumentException("error finding segmentID ${constraint.segmentId}")
        
        val updated = constraintRepository.update(constraint)
        
        // Save flag snapshot after updating constraint
        flagSnapshotService?.saveFlagSnapshot(segment.flagId, updatedBy)
        
        return updated
    }
    
    suspend fun deleteConstraint(id: Int, updatedBy: String? = null) {
        // Get constraint to know segmentId
        val constraint = constraintRepository.findById(id)
            ?: throw IllegalArgumentException("error finding constraintID $id")
        
        // Get segment to know flagId
        val segment = segmentRepository.findById(constraint.segmentId)
            ?: throw IllegalArgumentException("error finding segmentID ${constraint.segmentId}")
        
        val flagId = segment.flagId
        
        constraintRepository.delete(id)
        
        // Save flag snapshot after deleting constraint
        flagSnapshotService?.saveFlagSnapshot(flagId, updatedBy)
    }
}