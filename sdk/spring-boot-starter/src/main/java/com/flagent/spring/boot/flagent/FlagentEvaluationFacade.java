package com.flagent.spring.boot.flagent;

import com.flagent.client.ApiException;
import com.flagent.client.api.EvaluationApi;
import com.flagent.client.model.EvalContext;
import com.flagent.client.model.EvalResult;

/**
 * Facade for flag evaluation with optional in-memory cache (Caffeine).
 * When cache is enabled, results are cached by (flagKey/flagID, entityID, entityType).
 */
public class FlagentEvaluationFacade {

    private final EvaluationApi evaluationApi;
    private final com.github.benmanes.caffeine.cache.Cache<String, EvalResult> cache;

    public FlagentEvaluationFacade(EvaluationApi evaluationApi) {
        this.evaluationApi = evaluationApi;
        this.cache = null;
    }

    public FlagentEvaluationFacade(EvaluationApi evaluationApi,
                                   com.github.benmanes.caffeine.cache.Cache<String, EvalResult> cache) {
        this.evaluationApi = evaluationApi;
        this.cache = cache;
    }

    /**
     * Evaluate flag for the given context. Uses cache when enabled and not expired.
     */
    public EvalResult evaluate(EvalContext context) throws ApiException {
        if (cache != null) {
            String key = cacheKey(context);
            EvalResult cached = cache.getIfPresent(key);
            if (cached != null) {
                return cached;
            }
        }
        EvalResult result = evaluationApi.postEvaluation(context);
        if (cache != null) {
            cache.put(cacheKey(context), result);
        }
        return result;
    }

    private static String cacheKey(EvalContext ctx) {
        String flag = ctx.getFlagID() != null ? String.valueOf(ctx.getFlagID()) : (ctx.getFlagKey() != null ? ctx.getFlagKey() : "");
        String entityId = ctx.getEntityID() != null ? ctx.getEntityID() : "";
        String entityType = ctx.getEntityType() != null ? ctx.getEntityType() : "";
        return flag + "_" + entityId + "_" + entityType;
    }
}
