package com.netk.mvolacash.admin.activation;

import android.security.keystore.KeyGenParameterSpec;
import android.security.keystore.KeyProperties;
import android.util.Base64;

import java.security.GeneralSecurityException;
import java.security.KeyPairGenerator;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.Signature;
import java.security.spec.ECGenParameterSpec;

/** P-256 activation signer backed by the non-exportable Android Keystore private key. */
public final class AndroidKeystoreActivationSigner implements ActivationSigner {
    public static final String KEY_ALIAS = "mvolacash_activation_signing_key";
    private static final String KEYSTORE = "AndroidKeyStore";

    public AndroidKeystoreActivationSigner() throws GeneralSecurityException {
        ensureKeyExists();
    }

    private synchronized void ensureKeyExists() throws GeneralSecurityException {
        KeyStore store = loadStore();
        if (store.containsAlias(KEY_ALIAS)) return;

        KeyPairGenerator generator = KeyPairGenerator.getInstance(
                KeyProperties.KEY_ALGORITHM_EC, KEYSTORE);
        generator.initialize(new KeyGenParameterSpec.Builder(KEY_ALIAS, KeyProperties.PURPOSE_SIGN)
                .setAlgorithmParameterSpec(new ECGenParameterSpec("secp256r1"))
                .setDigests(KeyProperties.DIGEST_SHA256)
                .build());
        generator.generateKeyPair();
    }

    @Override
    public ActivationResponse sign(ActivationRequest request) throws GeneralSecurityException {
        if (request == null || !request.isValid()) {
            throw new IllegalArgumentException("Code de demande invalide");
        }
        KeyStore store = loadStore();
        PrivateKey key = (PrivateKey) store.getKey(KEY_ALIAS, null);
        if (key == null) throw new GeneralSecurityException("Clé privée Admin indisponible");
        Signature signer = Signature.getInstance("SHA256withECDSA");
        signer.initSign(key);
        signer.update(ActivationResponse.canonicalData(request.getInstallationId()));
        return new ActivationResponse(request.getInstallationId(), signer.sign());
    }

    @Override
    public String getPublicKeyBase64() throws GeneralSecurityException {
        KeyStore store = loadStore();
        if (store.getCertificate(KEY_ALIAS) == null) {
            throw new GeneralSecurityException("Clé publique Admin indisponible");
        }
        return Base64.encodeToString(store.getCertificate(KEY_ALIAS).getPublicKey().getEncoded(),
                Base64.NO_WRAP);
    }

    private static KeyStore loadStore() throws GeneralSecurityException {
        try {
            KeyStore store = KeyStore.getInstance(KEYSTORE);
            store.load(null);
            return store;
        } catch (java.io.IOException e) {
            throw new GeneralSecurityException("Impossible d’ouvrir Android Keystore", e);
        }
    }
}
