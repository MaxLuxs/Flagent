import React from 'react';
import { render, screen } from '@testing-library/react';
import { FlagentProvider } from '../FlagentProvider';

describe('FlagentProvider', () => {
  it('renders children', () => {
    const manager = {} as any;

    render(
      <FlagentProvider manager={manager}>
        <span data-testid="child">Child content</span>
      </FlagentProvider>
    );

    expect(screen.getByTestId('child').textContent).toBe('Child content');
  });
});
