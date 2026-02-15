package com.flagent.client.model;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * Model tests for PutDistributionsRequest
 */
class PutDistributionsRequestTest {

    @Test
    void testPutDistributionsRequest() {
        DistributionRequest d = new DistributionRequest().variantID(1L).variantKey("control").percent(100L);
        List<DistributionRequest> distributions = Collections.singletonList(d);
        PutDistributionsRequest model = new PutDistributionsRequest().distributions(distributions);
        Assertions.assertNotNull(model);
        Assertions.assertEquals(1, model.getDistributions().size());
        Assertions.assertEquals(100L, model.getDistributions().get(0).getPercent());
    }

    @Test
    void distributionsTest() {
        List<DistributionRequest> list = Arrays.asList(
            new DistributionRequest().variantID(1L).percent(50L),
            new DistributionRequest().variantID(2L).percent(50L)
        );
        PutDistributionsRequest model = new PutDistributionsRequest().distributions(list);
        Assertions.assertEquals(2, model.getDistributions().size());
    }

}
