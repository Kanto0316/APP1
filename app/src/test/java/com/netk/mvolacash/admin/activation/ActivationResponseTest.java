package com.netk.mvolacash.admin.activation;

import static org.junit.Assert.*;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.Signature;
import java.security.spec.ECGenParameterSpec;
import org.junit.Test;

public class ActivationResponseTest {
    private static final String ID = "23456789ABCDEFGHJKMNPQRSTUVWXYZ2";

    @Test public void canonicalFormIsVersionedAndDeterministic() {
        assertEquals("MVOLACASH|1|PERMANENT|" + ID,
                new String(ActivationResponse.canonicalData(ID), java.nio.charset.StandardCharsets.UTF_8));
    }

    @Test public void roundTripPreservesInstallationAndVerifies() throws Exception {
        KeyPair pair = keyPair();
        Signature signer = Signature.getInstance("SHA256withECDSA");
        signer.initSign(pair.getPrivate());
        signer.update(ActivationResponse.canonicalData(ID));
        ActivationResponse original = new ActivationResponse(ID, signer.sign());
        ActivationResponse decoded = ActivationResponse.decode(original.getActivationCode());
        assertEquals(ID, decoded.getInstallationId());
        assertEquals(original.getActivationCode(), decoded.getActivationCode());
        assertTrue(decoded.verify(pair.getPublic()));
    }

    @Test public void alteredResponseDoesNotVerify() throws Exception {
        KeyPair pair = keyPair();
        Signature signer = Signature.getInstance("SHA256withECDSA");
        signer.initSign(pair.getPrivate());
        signer.update(ActivationResponse.canonicalData(ID));
        byte[] signature = signer.sign();
        signature[signature.length - 1] ^= 1;
        assertFalse(new ActivationResponse(ID, signature).verify(pair.getPublic()));
    }

    private static KeyPair keyPair() throws Exception {
        KeyPairGenerator generator = KeyPairGenerator.getInstance("EC");
        generator.initialize(new ECGenParameterSpec("secp256r1"));
        return generator.generateKeyPair();
    }
}
