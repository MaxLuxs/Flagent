import React from 'react';
import { render, screen, waitFor } from '@testing-library/react';
import { FeatureFlag } from '../FeatureFlag';
import { FlagentProvider } from '../FlagentProvider';

describe('FeatureFlag', () => {
  it('shows children when flag is enabled', async () => {
    const manager = {
      evaluate: jest.fn().mockResolvedValue({ variantKey: 'control' }),
    } as any;

    render(
      <FlagentProvider manager={manager}>
        <FeatureFlag flagKey="test_flag">
          <span data-testid="enabled-content">Enabled</span>
        </FeatureFlag>
      </FlagentProvider>
    );

    await waitFor(() => {
      expect(screen.getByTestId('enabled-content').textContent).toBe('Enabled');
    });
  });

  it('shows fallback when flag is disabled', async () => {
    const manager = {
      evaluate: jest.fn().mockResolvedValue({ variantKey: 'disabled' }),
    } as any;

    render(
      <FlagentProvider manager={manager}>
        <FeatureFlag flagKey="test_flag" fallback={<span data-testid="fallback">Disabled</span>}>
          <span data-testid="enabled-content">Enabled</span>
        </FeatureFlag>
      </FlagentProvider>
    );

    await waitFor(() => {
      expect(screen.getByTestId('fallback').textContent).toBe('Disabled');
    });
    expect(screen.queryByTestId('enabled-content')).toBeNull();
  });

  it('shows fallback during loading', () => {
    let resolve: (value: any) => void;
    const evaluatePromise = new Promise((r) => (resolve = r));
    const manager = {
      evaluate: jest.fn().mockReturnValue(evaluatePromise),
    } as any;

    render(
      <FlagentProvider manager={manager}>
        <FeatureFlag flagKey="test_flag" fallback={<span data-testid="fallback">Loading...</span>}>
          <span data-testid="enabled-content">Enabled</span>
        </FeatureFlag>
      </FlagentProvider>
    );

    expect(screen.getByTestId('fallback').textContent).toBe('Loading...');
    resolve!({ variantKey: 'control' });
  });

  it('throws when used without FlagentProvider', () => {
    expect(() => {
      render(
        <FeatureFlag flagKey="test_flag">
          <span>Content</span>
        </FeatureFlag>
      );
    }).toThrow('FeatureFlag must be used within FlagentProvider');
  });
});
