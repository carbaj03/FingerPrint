/*
 * Copyright (C) 2015 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License
 */

package com.fingerprint;

import android.annotation.SuppressLint;
import android.hardware.fingerprint.FingerprintManager;
import android.os.CancellationSignal;
import android.widget.TextView;

@SuppressWarnings("ResourceType")
@SuppressLint("NewApi")
public class FingerprintUiHelper extends FingerprintManager.AuthenticationCallback {

    public interface Callback {
        void onAuthenticated();
        void onError();
    }

    private static final long ERROR_TIMEOUT_MILLIS = 1600;
    private static final long SUCCESS_DELAY_MILLIS = 1300;

    private final FingerprintManager fingerprintManager;
    private final TextView tvInfo;
    private final Callback callback;

    private CancellationSignal cancellationSignal;

    private boolean selfCancelled;

    FingerprintUiHelper(FingerprintManager fingerprintManager,
                        TextView tvInfo,
                        Callback callback) {
        this.fingerprintManager = fingerprintManager;
        this.tvInfo = tvInfo;
        this.callback = callback;
    }

    public boolean isFingerprintAuthAvailable() {
        return fingerprintManager.isHardwareDetected()
                && fingerprintManager.hasEnrolledFingerprints();
    }

    public void startListening(FingerprintManager.CryptoObject cryptoObject) {
        if (!isFingerprintAuthAvailable())
            return;

        cancellationSignal = new CancellationSignal();
        selfCancelled = false;

        fingerprintManager.authenticate(cryptoObject, cancellationSignal, 0, this, null);
        tvInfo.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_fp_40px, 0, 0, 0);
    }

    public void stopListening() {
        if (cancellationSignal != null) {
            selfCancelled = true;
            cancellationSignal.cancel();
            cancellationSignal = null;
        }
    }

    @Override
    public void onAuthenticationError(int errMsgId, CharSequence errString) {
        if (!selfCancelled) {
            showError(errString);
            tvInfo.postDelayed(new Runnable() {
                @Override
                public void run() {
                    callback.onError();
                }
            }, ERROR_TIMEOUT_MILLIS);
        }
    }

    @Override
    public void onAuthenticationHelp(int helpMsgId, CharSequence helpString) {
        showError(helpString);
    }

    @Override
    public void onAuthenticationFailed() {
        showError(tvInfo.getResources().getString(R.string.fingerprint_not_recognized));
    }

    @Override
    public void onAuthenticationSucceeded(FingerprintManager.AuthenticationResult result) {
        tvInfo.removeCallbacks(mResetErrorTextRunnable);
        tvInfo.setTextColor(tvInfo.getResources().getColor(R.color.success_color, null));
        tvInfo.setText(tvInfo.getResources().getString(R.string.fingerprint_success));

        tvInfo.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_fingerprint_success, 0, 0, 0);
        tvInfo.postDelayed(new Runnable() {
            @Override
            public void run() {
                callback.onAuthenticated();
            }
        }, SUCCESS_DELAY_MILLIS);
    }

    private void showError(CharSequence error) {
        tvInfo.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_fingerprint_error, 0, 0, 0);
        tvInfo.setText(error);
        tvInfo.setTextColor(tvInfo.getResources().getColor(R.color.warning_color, null));
        tvInfo.removeCallbacks(mResetErrorTextRunnable);
        tvInfo.postDelayed(mResetErrorTextRunnable, ERROR_TIMEOUT_MILLIS);
    }

    private Runnable mResetErrorTextRunnable = new Runnable() {
        @Override
        public void run() {
            tvInfo.setTextColor(tvInfo.getResources().getColor(R.color.hint_color, null));
            tvInfo.setText(tvInfo.getResources().getString(R.string.fingerprint_hint));
            tvInfo.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_fp_40px, 0, 0, 0);
        }
    };
}
