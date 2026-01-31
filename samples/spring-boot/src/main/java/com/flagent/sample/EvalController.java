package com.flagent.sample;

import com.flagent.client.ApiException;
import com.flagent.client.model.EvalContext;
import com.flagent.client.model.EvalResult;
import com.flagent.spring.boot.flagent.FlagentEvaluationFacade;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Example controller that evaluates a flag via FlagentEvaluationFacade.
 */
@RestController
public class EvalController {

    private final FlagentEvaluationFacade flagentFacade;

    public EvalController(FlagentEvaluationFacade flagentEvaluationFacade) {
        this.flagentFacade = flagentEvaluationFacade;
    }

    @GetMapping("/eval")
    public ResponseEntity<EvalResult> eval(
            @RequestParam(defaultValue = "my_flag") String flagKey,
            @RequestParam(defaultValue = "user-1") String entityId) throws ApiException {
        EvalContext context = new EvalContext()
                .flagKey(flagKey)
                .entityID(entityId);
        EvalResult result = flagentFacade.evaluate(context);
        return ResponseEntity.ok(result);
    }
}
