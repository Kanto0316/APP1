package com.netk.mvolacash.admin.activation;

import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.PublicKey;
import java.security.Signature;

/** A portable, versioned activation and its ECDSA signature. */
public final class ActivationResponse {
    public static final String DOMAIN = "MVOLACASH";
    public static final String VERSION = "1";
    public static final String LICENSE_TYPE = "PERMANENT";
    private static final String PREFIX = "MVACT1";

    private final String installationId;
    private final byte[] signature;

    public ActivationResponse(String installationId, byte[] signature) {
        this.installationId = installationId;
        this.signature = signature.clone();
    }

    public String getInstallationId() { return installationId; }
    public String getProtocolVersion() { return VERSION; }
    public String getLicenseType() { return LICENSE_TYPE; }
    public byte[] getSignature() { return signature.clone(); }

    /** Deterministic bytes signed by Admin and reproduced by Client. */
    public byte[] getCanonicalData() {
        return canonicalData(installationId);
    }

    public static byte[] canonicalData(String installationId) {
        return (DOMAIN + "|" + VERSION + "|" + LICENSE_TYPE + "|" + installationId)
                .getBytes(StandardCharsets.UTF_8);
    }

    /** MVACT1.base64url(canonical payload).base64url(DER ECDSA signature). */
    public String getActivationCode() {
        return PREFIX + "." + Base64Url.encode(getCanonicalData()) + "."
                + Base64Url.encode(signature);
    }

    public static ActivationResponse decode(String code) {
        if (code == null) throw new IllegalArgumentException("Code d’activation absent");
        String[] parts = code.trim().split("\\.", -1);
        if (parts.length != 3 || !PREFIX.equals(parts[0])) {
            throw new IllegalArgumentException("Format d’activation incorrect");
        }
        try {
            String canonical = new String(Base64Url.decode(parts[1]), StandardCharsets.UTF_8);
            String[] fields = canonical.split("\\|", -1);
            if (fields.length != 4 || !DOMAIN.equals(fields[0]) || !VERSION.equals(fields[1])
                    || !LICENSE_TYPE.equals(fields[2])) {
                throw new IllegalArgumentException("Données d’activation incorrectes");
            }
            ActivationRequest request = new ActivationRequest(fields[3]);
            if (!request.isValid() || !request.getInstallationId().equals(fields[3])) {
                throw new IllegalArgumentException("Identité d’installation incorrecte");
            }
            return new ActivationResponse(fields[3], Base64Url.decode(parts[2]));
        } catch (IllegalArgumentException e) {
            throw e;
        }
    }

    public boolean verify(PublicKey publicKey) throws GeneralSecurityException {
        Signature verifier = Signature.getInstance("SHA256withECDSA");
        verifier.initVerify(publicKey);
        verifier.update(getCanonicalData());
        return verifier.verify(signature);
    }
}
