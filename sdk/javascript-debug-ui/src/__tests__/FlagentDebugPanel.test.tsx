import React from 'react';
import { render, screen, waitFor, fireEvent } from '@testing-library/react';
import { FlagentDebugPanel } from '../FlagentDebugPanel';
import { FlagentProvider } from '../FlagentProvider';

describe('FlagentDebugPanel', () => {
  it('renders collapsed panel with Flagent title', () => {
    const manager = { evaluate: jest.fn() } as any;
    render(
      <FlagentProvider manager={manager}>
        <FlagentDebugPanel flagKeys={[]} />
      </FlagentProvider>
    );
    expect(screen.getByRole('button', { name: /Flagent/ })).toBeTruthy();
  });

  it('shows "Pass flagKeys prop" when flagKeys empty', () => {
    const manager = { evaluate: jest.fn() } as any;
    render(
      <FlagentProvider manager={manager}>
        <FlagentDebugPanel flagKeys={[]} />
      </FlagentProvider>
    );
    fireEvent.click(screen.getByRole('button', { name: /Flagent/ }));
    expect(screen.getByText('Pass flagKeys prop to inspect')).toBeTruthy();
  });

  it('evaluates and displays flag states', async () => {
    const manager = {
      evaluate: jest.fn().mockResolvedValue({ variantKey: 'control' }),
    } as any;
    render(
      <FlagentProvider manager={manager}>
        <FlagentDebugPanel flagKeys={['test_flag']} />
      </FlagentProvider>
    );
    fireEvent.click(screen.getByRole('button', { name: /Flagent/ }));
    await waitFor(() => {
      expect(screen.getByText('test_flag')).toBeTruthy();
      expect(screen.getByText('on')).toBeTruthy();
    });
  });

  it('returns null without FlagentProvider', () => {
    const { container } = render(<FlagentDebugPanel flagKeys={['x']} />);
    expect(container.firstChild).toBeNull();
  });
});
