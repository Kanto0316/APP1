package com.netk.mvolacash.admin.activation;

import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.PublicKey;
import java.security.Signature;

/** A portable, versioned activation and its ECDSA signature. */
public final class ActivationResponse {
    public static final String DOMAIN = "MVOLACASH";
    public static final String VERSION_1 = "1";
    public static final String VERSION_2 = "2";
    /** Canonical V2 representation of a permanent license's absent expiration. */
    public static final String NO_EXPIRATION = "NONE";
    private static final String PREFIX = "MVACT1";

    private final String protocolVersion;
    private final LicenseType licenseType;
    private final String installationId;
    private final long issuedAt;
    private final Long expiresAt;
    private final byte[] signature;

    /** Creates the historical V1 permanent response. */
    public ActivationResponse(String installationId, byte[] signature) {
        this(VERSION_1, LicenseType.PERMANENT, installationId, 0, null, signature);
    }

    public static ActivationResponse v2(LicenseType type, String installationId, long issuedAt,
            Long expiresAt, byte[] signature) {
        validateV2Dates(type, issuedAt, expiresAt);
        return new ActivationResponse(VERSION_2, type, installationId, issuedAt, expiresAt,
                signature);
    }

    private ActivationResponse(String version, LicenseType type, String installationId,
            long issuedAt, Long expiresAt, byte[] signature) {
        this.protocolVersion = version;
        this.licenseType = type;
        this.installationId = installationId;
        this.issuedAt = issuedAt;
        this.expiresAt = expiresAt;
        this.signature = signature.clone();
    }

    public String getInstallationId() { return installationId; }
    public String getProtocolVersion() { return protocolVersion; }
    /** Returns the wire value and preserves the V1 API contract. */
    public String getLicenseType() { return licenseType.name(); }
    public LicenseType getLicenseTypeEnum() { return licenseType; }
    public long getIssuedAt() { return issuedAt; }
    public Long getExpiresAt() { return expiresAt; }
    public byte[] getSignature() { return signature.clone(); }

    public byte[] getCanonicalData() {
        if (VERSION_1.equals(protocolVersion)) return canonicalDataV1(installationId);
        return canonicalDataV2(licenseType, installationId, issuedAt, expiresAt);
    }

    /** Unchanged canonical bytes for all existing and newly issued permanent V1 licenses. */
    public static byte[] canonicalData(String installationId) {
        return canonicalDataV1(installationId);
    }

    public static byte[] canonicalDataV1(String installationId) {
        return (DOMAIN + "|" + VERSION_1 + "|PERMANENT|" + installationId)
                .getBytes(StandardCharsets.UTF_8);
    }

    public static byte[] canonicalDataV2(LicenseType type, String installationId, long issuedAt,
            Long expiresAt) {
        validateV2Dates(type, issuedAt, expiresAt);
        String expiration = expiresAt == null ? NO_EXPIRATION : Long.toString(expiresAt);
        return (DOMAIN + "|" + VERSION_2 + "|" + type.name() + "|" + installationId + "|"
                + issuedAt + "|" + expiration).getBytes(StandardCharsets.UTF_8);
    }

    public String getActivationCode() {
        return PREFIX + "." + Base64Url.encode(getCanonicalData()) + "."
                + Base64Url.encode(signature);
    }

    /** Decodes both the original V1 payload and the extended V2 payload. */
    public static ActivationResponse decode(String code) {
        if (code == null) throw new IllegalArgumentException("Code d’activation absent");
        String[] parts = code.trim().split("\\.", -1);
        if (parts.length != 3 || !PREFIX.equals(parts[0])) {
            throw new IllegalArgumentException("Format d’activation incorrect");
        }
        String canonical = new String(Base64Url.decode(parts[1]), StandardCharsets.UTF_8);
        String[] fields = canonical.split("\\|", -1);
        byte[] signature = Base64Url.decode(parts[2]);
        if (fields.length == 4 && DOMAIN.equals(fields[0]) && VERSION_1.equals(fields[1])
                && LicenseType.PERMANENT.name().equals(fields[2])) {
            validateInstallationId(fields[3]);
            return new ActivationResponse(fields[3], signature);
        }
        if (fields.length == 6 && DOMAIN.equals(fields[0]) && VERSION_2.equals(fields[1])) {
            LicenseType type;
            long issuedAt;
            Long expiresAt;
            try {
                type = LicenseType.valueOf(fields[2]);
                issuedAt = Long.parseLong(fields[4]);
                expiresAt = NO_EXPIRATION.equals(fields[5]) ? null : Long.parseLong(fields[5]);
            } catch (IllegalArgumentException e) {
                throw new IllegalArgumentException("Données d’activation incorrectes", e);
            }
            validateInstallationId(fields[3]);
            return v2(type, fields[3], issuedAt, expiresAt, signature);
        }
        throw new IllegalArgumentException("Données d’activation incorrectes");
    }

    public boolean verify(PublicKey publicKey) throws GeneralSecurityException {
        Signature verifier = Signature.getInstance("SHA256withECDSA");
        verifier.initVerify(publicKey);
        verifier.update(getCanonicalData());
        return verifier.verify(signature);
    }

    private static void validateInstallationId(String installationId) {
        ActivationRequest request = new ActivationRequest(installationId);
        if (!request.isValid() || !request.getInstallationId().equals(installationId)) {
            throw new IllegalArgumentException("Identité d’installation incorrecte");
        }
    }

    private static void validateV2Dates(LicenseType type, long issuedAt, Long expiresAt) {
        if (type == null || issuedAt < 0) {
            throw new IllegalArgumentException("Données d’activation incorrectes");
        }
        if (type == LicenseType.PERMANENT) {
            if (expiresAt != null) throw new IllegalArgumentException("Expiration permanente");
        } else if (expiresAt == null || expiresAt <= issuedAt) {
            throw new IllegalArgumentException("Expiration temporaire incorrecte");
        }
    }
}
