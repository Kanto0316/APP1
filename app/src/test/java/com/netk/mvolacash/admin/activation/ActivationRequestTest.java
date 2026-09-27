package com.netk.mvolacash.admin.activation;

import static org.junit.Assert.*;
import org.junit.Test;

public class ActivationRequestTest {
    private static final String ID = "23456789ABCDEFGHJKMNPQRSTUVWXYZ2";

    @Test public void normalizesCaseAndSeparators() {
        ActivationRequest request = new ActivationRequest(" 2345-6789-abcd-efgh-jkmn-pqrs-tuvw-xyz2 ");
        assertTrue(request.isValid());
        assertEquals(ID, request.getInstallationId());
        assertEquals("2345-6789-ABCD-EFGH-JKMN-PQRS-TUVW-XYZ2", request.getRequestCode());
    }

    @Test public void rejectsEmptyAndAmbiguousCharacters() {
        assertFalse(new ActivationRequest(null).isValid());
        assertTrue(new ActivationRequest(" ").isEmpty());
        assertFalse(new ActivationRequest("OOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOO").isValid());
    }
}
