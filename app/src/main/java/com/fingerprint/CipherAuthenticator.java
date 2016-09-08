package com.fingerprint;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.KeyguardManager;
import android.hardware.fingerprint.FingerprintManager;
import android.os.Build;
import android.security.keystore.KeyGenParameterSpec;

import java.io.IOException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;

import static android.hardware.fingerprint.FingerprintManager.*;
import static android.security.keystore.KeyProperties.*;

/**
 * Created by alejandro on 8/09/16.
 */
@SuppressLint("NewApi")
@SuppressWarnings("ResourceType")
public class CipherAuthenticator {

    public static final String CIPHER_DEFAULT =
            KEY_ALGORITHM_AES + "/" + BLOCK_MODE_CBC + "/" + ENCRYPTION_PADDING_PKCS7;

    private static final String KEY_NAME_DEFAULT = "key_default";
    private static final String ANDROID_KEY_STORE = "AndroidKeyStore";

    private KeyStore keyStore;
    private KeyGenerator keyGenerator;

    private Cipher cipher;
    private String keyName = KEY_NAME_DEFAULT;

    private FingerprintManager fingerprintManager;
    private KeyguardManager keyguardManager;

    public CipherAuthenticator(Activity activity) {
        if (isAvailableSdkFingerprint()) {
            this.fingerprintManager = activity.getSystemService(FingerprintManager.class);
            this.keyguardManager = activity.getSystemService(KeyguardManager.class);
        }
    }

    public void create() throws CustomException {

        try {
            if (!isAvailableSdkFingerprint())
                throw new CustomException("No available");

            createKeyStore();

            createKeyGenerator();

            createCipher();

            checkSetUpFingerprint();

            checkHasEnrolledFingerprints();

            createKey(KEY_NAME_DEFAULT, true);

        }catch (CustomException e){
            e.printStackTrace();
            throw e;
        }
    }

    private void createKeyStore() throws CustomException {
        try {
            keyStore = KeyStore.getInstance(ANDROID_KEY_STORE);
        } catch (KeyStoreException e) {
            throw new CustomException("Failed to get an instance of KeyStore");
        }
    }

    private void createKeyGenerator() throws CustomException {
        try {
            keyGenerator = KeyGenerator.getInstance(KEY_ALGORITHM_AES, ANDROID_KEY_STORE);
        } catch (NoSuchAlgorithmException | NoSuchProviderException e) {
            throw new CustomException("Failed to get an instance of KeyGenerator");
        }
    }

    private void createCipher() throws CustomException {
        try {
            cipher = Cipher.getInstance(CIPHER_DEFAULT);
        } catch (NoSuchAlgorithmException | NoSuchPaddingException e) {
            throw new CustomException("Failded to create fingerprintManager cipher");
        }
    }

    private void checkSetUpFingerprint() throws CustomException {
        if (!keyguardManager.isKeyguardSecure()) {
            throw new CustomException("Secure lock screen hasn't set up.\n"
                    + "Go to 'Settings -> Security -> Fingerprint' to set up fingerprintManager fingerprint");
        }
    }

    private void checkHasEnrolledFingerprints() throws CustomException {
        if (!fingerprintManager.hasEnrolledFingerprints()) {
            throw new CustomException("Has not Enrolled Fingerprints.\n"
                + "Go to 'Settings -> Security -> Fingerprint' and register at least one fingerprint");
        }
    }

    private void createKey(String keyName, boolean invalidatedByBiometricEnrollment) throws CustomException {
        try {
            keyStore.load(null);

            KeyGenParameterSpec.Builder builder = new KeyGenParameterSpec.Builder(
                    keyName, PURPOSE_ENCRYPT | PURPOSE_DECRYPT)
                    .setBlockModes(BLOCK_MODE_CBC)
                    .setUserAuthenticationRequired(true)
                    .setEncryptionPaddings(ENCRYPTION_PADDING_PKCS7);

            if (isInvalidate())
                builder.setInvalidatedByBiometricEnrollment(invalidatedByBiometricEnrollment);


            KeyGenParameterSpec keyGenParameterSpec = builder.build();
            keyGenerator.init(keyGenParameterSpec);
            keyGenerator.generateKey();

        } catch (NoSuchAlgorithmException | InvalidAlgorithmParameterException
                | CertificateException | IOException e) {
            throw new CustomException("Failed created key");
        }
    }

    public void init() throws CustomException {
        try{
            if(keyStore != null && cipher != null) {
                keyStore.load(null);
                SecretKey key = (SecretKey) keyStore.getKey(keyName, null);
                cipher.init(Cipher.ENCRYPT_MODE, key);
            }
        } catch (KeyStoreException | CertificateException | UnrecoverableKeyException | IOException
                | NoSuchAlgorithmException | InvalidKeyException e) {
            e.printStackTrace();
            throw new CustomException("Failed to init Cipher");
        }

    }

    public boolean isFingerprintAuthAvailable() {
        return isAvailableSdkFingerprint() &&
                fingerprintManager.isHardwareDetected() &&
                fingerprintManager.hasEnrolledFingerprints();
    }

    public CryptoObject getCrypto() {
        return new CryptoObject(cipher);
    }

    public FingerprintManager getFingerprintManager() {
        return fingerprintManager;
    }

    private boolean isAvailableSdkFingerprint() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.M;
    }

    private boolean isInvalidate() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.N;
    }
}
