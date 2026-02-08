/**
 * Returns entityContext for preview/PR environments.
 * Use in Vercel, Netlify, Cloudflare Pages preview deployments.
 *
 * Reserved keys: _preview_pr, _preview_env, _branch
 *
 * @example
 * ```ts
 * const manager = new FlagentManager(config);
 * const ctx = getPreviewContext();
 * const result = await manager.evaluate({
 *   flagKey: 'new_feature',
 *   entityContext: ctx ?? undefined
 * });
 * ```
 */
export function getPreviewContext(): Record<string, string> | null {
  const pr =
    process.env.VERCEL_GIT_PULL_REQUEST_ID ??
    process.env.REVIEW_ID ??
    process.env.CF_PAGES_PULL_REQUEST;
  const branch =
    process.env.VERCEL_GIT_COMMIT_REF ??
    process.env.BRANCH ??
    process.env.CF_PAGES_BRANCH;
  if (!pr && !branch) return null;
  return {
    ...(pr && { _preview_pr: String(pr) }),
    ...(branch && { _branch: branch }),
    ...(pr && { _preview_env: `pr-${pr}` }),
  };
}
