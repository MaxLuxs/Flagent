import { renderHook, waitFor } from '@testing-library/react';
import { useFlag } from '../useFlag';

describe('useFlag', () => {
  it('returns enabled=true when variantKey is not disabled', async () => {
    const manager = {
      evaluate: jest.fn().mockResolvedValue({ variantKey: 'control' }),
    } as any;

    const { result } = renderHook(() =>
      useFlag(manager, 'test_flag', 'user1', undefined)
    );

    expect(result.current.loading).toBe(true);
    expect(result.current.enabled).toBe(false);

    await waitFor(() => expect(result.current.loading).toBe(false));

    expect(result.current.enabled).toBe(true);
    expect(result.current.loading).toBe(false);
    expect(manager.evaluate).toHaveBeenCalledWith({
      flagKey: 'test_flag',
      entityID: 'user1',
      entityContext: undefined,
    });
  });

  it('returns enabled=false when variantKey is disabled', async () => {
    const manager = {
      evaluate: jest.fn().mockResolvedValue({ variantKey: 'disabled' }),
    } as any;

    const { result } = renderHook(() =>
      useFlag(manager, 'test_flag', 'user1', undefined)
    );

    await waitFor(() => expect(result.current.loading).toBe(false));

    expect(result.current.enabled).toBe(false);
    expect(result.current.loading).toBe(false);
  });

  it('returns enabled=false on evaluate error', async () => {
    const manager = {
      evaluate: jest.fn().mockRejectedValue(new Error('network error')),
    } as any;

    const { result } = renderHook(() =>
      useFlag(manager, 'test_flag', undefined, undefined)
    );

    await waitFor(() => expect(result.current.loading).toBe(false));

    expect(result.current.enabled).toBe(false);
    expect(result.current.loading).toBe(false);
  });

  it('passes entityContext to evaluate', async () => {
    const manager = {
      evaluate: jest.fn().mockResolvedValue({ variantKey: 'control' }),
    } as any;
    const entityContext = { region: 'EU', tier: 'premium' };

    const { result } = renderHook(() =>
      useFlag(manager, 'test_flag', 'user1', entityContext)
    );

    await waitFor(() => expect(result.current.loading).toBe(false));

    expect(manager.evaluate).toHaveBeenCalledWith({
      flagKey: 'test_flag',
      entityID: 'user1',
      entityContext,
    });
  });
});
