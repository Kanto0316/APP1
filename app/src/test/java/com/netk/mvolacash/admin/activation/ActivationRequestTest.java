package com.netk.mvolacash.admin.activation;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class ActivationRequestTest {
    @Test
    public void requestCodeIsTrimmedAndUppercased() {
        ActivationRequest request = new ActivationRequest("  abcd-1234  ");
        assertEquals("ABCD-1234", request.getRequestCode());
    }

    @Test
    public void nullRequestIsEmpty() {
        assertTrue(new ActivationRequest(null).isEmpty());
    }
}

