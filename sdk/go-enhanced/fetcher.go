package flagentenhanced

import (
	"context"
	"fmt"
	"time"

	flagent "github.com/MaxLuxs/Flagent/sdk/go"
)

// SnapshotFetcher fetches flag snapshots from the server
type SnapshotFetcher struct {
	client *flagent.Client
}

// NewSnapshotFetcher creates a new snapshot fetcher
func NewSnapshotFetcher(client *flagent.Client) *SnapshotFetcher {
	return &SnapshotFetcher{
		client: client,
	}
}

// FetchSnapshot fetches a fresh snapshot from the server
func (f *SnapshotFetcher) FetchSnapshot(ctx context.Context, ttlMs int64) (*FlagSnapshot, error) {
	// Get snapshot from server (using export endpoint)
	serverSnapshot, err := f.client.GetSnapshot(ctx)
	if err != nil {
		return nil, fmt.Errorf("failed to fetch snapshot: %w", err)
	}

	// Convert to local snapshot format
	localSnapshot := &FlagSnapshot{
		Flags:     make(map[int64]*LocalFlag),
		FetchedAt: time.Now().UnixMilli(),
		TTLMs:     ttlMs,
		Revision:  getRevision(serverSnapshot),
	}

	// Convert flags
	for _, serverFlag := range serverSnapshot.Flags {
		if serverFlag.ID == nil {
			continue
		}

		localFlag := &LocalFlag{
			ID:          *serverFlag.ID,
			Key:         serverFlag.Key,
			Enabled:     serverFlag.Enabled,
			Description: getStringValue(serverFlag.Description),
			EntityType:  getStringValue(serverFlag.EntityType),
			Segments:    make([]*LocalSegment, 0),
			Variants:    make([]*LocalVariant, 0),
		}

		// Convert segments
		for _, serverSegment := range serverFlag.Segments {
			if serverSegment.ID == nil {
				continue
			}

			localSegment := &LocalSegment{
				ID:             *serverSegment.ID,
				FlagID:         *serverFlag.ID,
				Rank:           serverSegment.Rank,
				RolloutPercent: serverSegment.RolloutPercent,
				Description:    getStringValue(serverSegment.Description),
				Constraints:    make([]*LocalConstraint, 0),
				Distributions:  make([]*LocalDistribution, 0),
			}

			// Convert constraints
			for _, serverConstraint := range serverSegment.Constraints {
				if serverConstraint.ID == nil {
					continue
				}

				localConstraint := &LocalConstraint{
					ID:       *serverConstraint.ID,
					Property: serverConstraint.Property,
					Operator: serverConstraint.Operator,
					Value:    getStringValue(serverConstraint.Value),
				}

				localSegment.Constraints = append(localSegment.Constraints, localConstraint)
			}

			// Convert distributions
			for _, serverDist := range serverSegment.Distributions {
				if serverDist.ID == nil {
					continue
				}

				localDist := &LocalDistribution{
					ID:         *serverDist.ID,
					VariantID:  serverDist.VariantID,
					VariantKey: serverDist.VariantKey,
					Percent:    serverDist.Percent,
				}

				localSegment.Distributions = append(localSegment.Distributions, localDist)
			}

			localFlag.Segments = append(localFlag.Segments, localSegment)
		}

		// Convert variants
		for _, serverVariant := range serverFlag.Variants {
			if serverVariant.ID == nil {
				continue
			}

			localVariant := &LocalVariant{
				ID:         *serverVariant.ID,
				FlagID:     *serverFlag.ID,
				Key:        serverVariant.Key,
				Attachment: serverVariant.Attachment,
			}

			localFlag.Variants = append(localFlag.Variants, localVariant)
		}

		localSnapshot.Flags[localFlag.ID] = localFlag
	}

	return localSnapshot, nil
}

// getStringValue safely gets string value from pointer
func getStringValue(s *string) string {
	if s == nil {
		return ""
	}
	return *s
}

// getRevision extracts revision from server snapshot
func getRevision(snapshot *flagent.FlagSnapshot) string {
	if snapshot.Revision == nil {
		return ""
	}
	return *snapshot.Revision
}
