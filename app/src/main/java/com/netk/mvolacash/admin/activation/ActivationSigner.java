package com.netk.mvolacash.admin.activation;

import java.security.GeneralSecurityException;

/** Contract for an activation authority. Implementations must never expose the private key. */
public interface ActivationSigner {
    /** Keeps the historical V1 permanent issuance behavior. */
    ActivationResponse sign(ActivationRequest request) throws GeneralSecurityException;
    ActivationResponse sign(ActivationRequest request, LicenseType type, long issuedAt)
            throws GeneralSecurityException;
    String getPublicKeyBase64() throws GeneralSecurityException;
}
