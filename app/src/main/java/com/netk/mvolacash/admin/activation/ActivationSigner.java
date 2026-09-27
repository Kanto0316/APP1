package com.netk.mvolacash.admin.activation;

import java.security.GeneralSecurityException;

/** Contract for an activation authority. Implementations must never expose the private key. */
public interface ActivationSigner {
    ActivationResponse sign(ActivationRequest request) throws GeneralSecurityException;
    String getPublicKeyBase64() throws GeneralSecurityException;
}
