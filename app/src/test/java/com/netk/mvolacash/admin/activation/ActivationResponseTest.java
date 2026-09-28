package com.netk.mvolacash.admin.activation;

import static org.junit.Assert.*;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.Signature;
import java.security.spec.ECGenParameterSpec;
import java.util.Calendar;
import java.util.TimeZone;
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
        assertEquals("1", decoded.getProtocolVersion());
        assertEquals("PERMANENT", decoded.getLicenseType());
        assertNull(decoded.getExpiresAt());
    }

    @Test public void weekV2ExpiresExactlySevenDaysLater() throws Exception {
        long issuedAt = 1_800_000_000L;
        long expiresAt = LicenseExpiration.forLicense(
                LicenseType.WEEK, issuedAt);
        assertEquals(issuedAt + 7L * 24 * 60 * 60, expiresAt);
        ActivationResponse decoded = signedV2(LicenseType.WEEK, issuedAt, expiresAt);
        assertEquals("MVOLACASH|2|WEEK|" + ID + "|1800000000|1800604800",
                new String(decoded.getCanonicalData(), java.nio.charset.StandardCharsets.UTF_8));
        assertEquals(Long.valueOf(expiresAt), decoded.getExpiresAt());
    }

    @Test public void monthV2UsesCalendarMonth() throws Exception {
        Calendar issued = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
        issued.clear();
        issued.set(2026, Calendar.SEPTEMBER, 28, 12, 34, 56);
        long issuedAt = issued.getTimeInMillis() / 1000L;
        long expiresAt = LicenseExpiration.forLicense(
                LicenseType.MONTH, issuedAt);
        Calendar expiration = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
        expiration.setTimeInMillis(expiresAt * 1000L);
        assertEquals(2026, expiration.get(Calendar.YEAR));
        assertEquals(Calendar.OCTOBER, expiration.get(Calendar.MONTH));
        assertEquals(28, expiration.get(Calendar.DAY_OF_MONTH));
        assertEquals(12, expiration.get(Calendar.HOUR_OF_DAY));
        assertEquals(34, expiration.get(Calendar.MINUTE));
        assertEquals(56, expiration.get(Calendar.SECOND));
        assertEquals("MONTH",
                signedV2(LicenseType.MONTH, issuedAt, expiresAt).getLicenseType());
    }

    @Test public void permanentV2HasExplicitNoExpirationRepresentation() {
        assertEquals("MVOLACASH|2|PERMANENT|" + ID + "|1800000000|NONE",
                new String(ActivationResponse.canonicalDataV2(
                        LicenseType.PERMANENT, ID, 1_800_000_000L, null),
                        java.nio.charset.StandardCharsets.UTF_8));
    }

    @Test public void changingPayloadCharacterInvalidatesSignature() throws Exception {
        KeyPair pair = keyPair();
        long issuedAt = 1_800_000_000L;
        long expiresAt = issuedAt + 604_800L;
        ActivationResponse response = createSignedV2(pair, LicenseType.WEEK, issuedAt, expiresAt);
        String code = response.getActivationCode();
        String[] parts = code.split("\\.");
        String canonical = new String(Base64Url.decode(parts[1]),
                java.nio.charset.StandardCharsets.UTF_8).replace("WEEK", "MONTH");
        ActivationResponse altered = ActivationResponse.decode(parts[0] + "."
                + Base64Url.encode(canonical.getBytes(java.nio.charset.StandardCharsets.UTF_8))
                + "." + parts[2]);
        assertFalse(altered.verify(pair.getPublic()));
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

    private static ActivationResponse signedV2(LicenseType type, long issuedAt, long expiresAt)
            throws Exception {
        KeyPair pair = keyPair();
        ActivationResponse response = createSignedV2(pair, type, issuedAt, expiresAt);
        ActivationResponse decoded = ActivationResponse.decode(response.getActivationCode());
        assertTrue(decoded.verify(pair.getPublic()));
        return decoded;
    }

    private static ActivationResponse createSignedV2(KeyPair pair, LicenseType type,
            long issuedAt, long expiresAt) throws Exception {
        Signature signer = Signature.getInstance("SHA256withECDSA");
        signer.initSign(pair.getPrivate());
        signer.update(ActivationResponse.canonicalDataV2(type, ID, issuedAt, expiresAt));
        return ActivationResponse.v2(type, ID, issuedAt, expiresAt, signer.sign());
    }
}
