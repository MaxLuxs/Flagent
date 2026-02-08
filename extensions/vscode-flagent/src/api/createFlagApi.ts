import { Configuration, FlagApi } from "@flagent/client";

/**
 * Creates FlagApi instance from extension config.
 * Uses generated OpenAPI client.
 */
export function createFlagApi(baseUrl: string, apiKey?: string): FlagApi {
  const base = baseUrl.replace(/\/$/, "");
  const basePath = base.endsWith("/api/v1") ? base : `${base}/api/v1`;
  const config = new Configuration({
    basePath,
    baseOptions: {
      headers: apiKey ? { "X-API-Key": apiKey } : {},
    },
  });
  return new FlagApi(config);
}
