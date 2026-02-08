import { getPreviewContext } from '../../preview/getPreviewContext';

describe('getPreviewContext', () => {
  const originalEnv = process.env;

  beforeEach(() => {
    jest.resetModules();
    process.env = { ...originalEnv };
  });

  afterAll(() => {
    process.env = originalEnv;
  });

  it('returns null when no preview env vars', () => {
    delete process.env.VERCEL_GIT_PULL_REQUEST_ID;
    delete process.env.VERCEL_GIT_COMMIT_REF;
    delete process.env.REVIEW_ID;
    delete process.env.BRANCH;
    delete process.env.CF_PAGES_PULL_REQUEST;
    delete process.env.CF_PAGES_BRANCH;
    expect(getPreviewContext()).toBeNull();
  });

  it('returns _preview_pr and _branch for Vercel', () => {
    process.env.VERCEL_GIT_PULL_REQUEST_ID = '42';
    process.env.VERCEL_GIT_COMMIT_REF = 'feature/new-checkout';
    const ctx = getPreviewContext();
    expect(ctx).toEqual({
      _preview_pr: '42',
      _branch: 'feature/new-checkout',
      _preview_env: 'pr-42',
    });
  });

  it('returns only _branch when no PR', () => {
    process.env.VERCEL_GIT_COMMIT_REF = 'develop';
    const ctx = getPreviewContext();
    expect(ctx).toEqual({ _branch: 'develop' });
  });

  it('uses Netlify REVIEW_ID and BRANCH', () => {
    process.env.REVIEW_ID = '123';
    process.env.BRANCH = 'fix/foo';
    const ctx = getPreviewContext();
    expect(ctx).toEqual({
      _preview_pr: '123',
      _branch: 'fix/foo',
      _preview_env: 'pr-123',
    });
  });

  it('uses Cloudflare Pages vars', () => {
    process.env.CF_PAGES_PULL_REQUEST = '5';
    process.env.CF_PAGES_BRANCH = 'feature/test';
    const ctx = getPreviewContext();
    expect(ctx).toEqual({
      _preview_pr: '5',
      _branch: 'feature/test',
      _preview_env: 'pr-5',
    });
  });
});
