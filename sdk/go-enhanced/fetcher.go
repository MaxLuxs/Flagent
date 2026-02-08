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

	// Convert flags (api.Flag uses Id, Segments, Variants - value types)
	for _, serverFlag := range serverSnapshot.Flags {
		if serverFlag.Id == 0 {
			continue
		}

		localFlag := &LocalFlag{
			ID:          serverFlag.Id,
			Key:         serverFlag.Key,
			Enabled:     serverFlag.Enabled,
			Description: serverFlag.Description,
			EntityType:  serverFlag.GetEntityType(),
			Segments:    make([]*LocalSegment, 0),
			Variants:    make([]*LocalVariant, 0),
		}

		// Convert segments
		for _, serverSegment := range serverFlag.Segments {
			if serverSegment.Id == 0 {
				continue
			}

			localSegment := &LocalSegment{
				ID:             serverSegment.Id,
				FlagID:         serverFlag.Id,
				Rank:           int(serverSegment.Rank),
				RolloutPercent: int(serverSegment.RolloutPercent),
				Description:    serverSegment.Description,
				Constraints:    make([]*LocalConstraint, 0),
				Distributions:  make([]*LocalDistribution, 0),
			}

			// Convert constraints
			for _, serverConstraint := range serverSegment.Constraints {
				if serverConstraint.Id == 0 {
					continue
				}

				localConstraint := &LocalConstraint{
					ID:       serverConstraint.Id,
					Property: serverConstraint.Property,
					Operator: serverConstraint.Operator,
					Value:    serverConstraint.Value,
				}

				localSegment.Constraints = append(localSegment.Constraints, localConstraint)
			}

			// Convert distributions
			for _, serverDist := range serverSegment.Distributions {
				if serverDist.Id == 0 {
					continue
				}

				variantKey := ""
				if serverDist.VariantKey.IsSet() && serverDist.VariantKey.Get() != nil {
					variantKey = *serverDist.VariantKey.Get()
				}

				localDist := &LocalDistribution{
					ID:         serverDist.Id,
					VariantID:  serverDist.VariantID,
					VariantKey:  variantKey,
					Percent:    int(serverDist.Percent),
				}

				localSegment.Distributions = append(localSegment.Distributions, localDist)
			}

			localFlag.Segments = append(localFlag.Segments, localSegment)
		}

		// Convert variants
		for _, serverVariant := range serverFlag.Variants {
			if serverVariant.Id == 0 {
				continue
			}

			attachment := make(map[string]interface{})
			if serverVariant.Attachment != nil {
				for k, v := range serverVariant.Attachment {
					attachment[k] = v
				}
			}

			localVariant := &LocalVariant{
				ID:         serverVariant.Id,
				FlagID:     serverFlag.Id,
				Key:        serverVariant.Key,
				Attachment: attachment,
			}

			localFlag.Variants = append(localFlag.Variants, localVariant)
		}

		localSnapshot.Flags[localFlag.ID] = localFlag
	}

	return localSnapshot, nil
}

// getRevision extracts revision from server snapshot
func getRevision(snapshot *flagent.FlagSnapshot) string {
	if snapshot.Revision == nil {
		return ""
	}
	return *snapshot.Revision
}
