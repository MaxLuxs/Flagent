package com.flagent.sample;

import com.flagent.client.api.EvaluationApi;
import com.flagent.client.model.EvalContext;
import com.flagent.client.model.EvalResult;
import com.flagent.client.model.EvaluationBatchRequest;
import com.flagent.client.model.EvaluationBatchResponse;
import com.flagent.spring.boot.flagent.FlagentAutoConfiguration;
import com.flagent.spring.boot.flagent.FlagentEvaluationFacade;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = EvalController.class)
@ImportAutoConfiguration(exclude = FlagentAutoConfiguration.class)
class EvalControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private FlagentEvaluationFacade flagentFacade;

    @MockBean
    private EvaluationApi evaluationApi;

    @Test
    void eval_returnsResult() throws Exception {
        EvalResult result = new EvalResult()
                .flagID(1L)
                .flagKey("my_flag")
                .variantKey("control");
        when(flagentFacade.evaluate(any(EvalContext.class))).thenReturn(result);

        mockMvc.perform(get("/eval")
                        .param("flagKey", "my_flag")
                        .param("entityId", "user-1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.flagKey").value("my_flag"))
                .andExpect(jsonPath("$.variantKey").value("control"));
    }

    @Test
    void eval_withEntityContext_passesContext() throws Exception {
        EvalResult result = new EvalResult()
                .flagID(1L)
                .flagKey("my_flag")
                .variantKey("treatment");
        when(flagentFacade.evaluate(any(EvalContext.class))).thenReturn(result);

        mockMvc.perform(get("/eval")
                        .param("flagKey", "my_flag")
                        .param("entityId", "user-1")
                        .param("country", "US")
                        .param("tier", "premium"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.variantKey").value("treatment"));
    }

    @Test
    void evalBatch_returnsResults() throws Exception {
        EvaluationBatchResponse response = new EvaluationBatchResponse();
        response.setEvaluationResults(List.of());
        when(evaluationApi.postEvaluationBatch(any(EvaluationBatchRequest.class))).thenReturn(response);

        mockMvc.perform(post("/eval/batch")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "entities": [{"entityID": "user1", "entityType": "user"}],
                                  "flagKeys": ["my_flag"]
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.evaluationResults").isArray());
    }
}
