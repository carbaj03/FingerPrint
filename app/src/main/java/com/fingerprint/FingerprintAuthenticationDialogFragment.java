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

import android.app.Dialog;
import android.content.Context;
import android.hardware.fingerprint.FingerprintManager;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

import static android.hardware.fingerprint.FingerprintManager.CryptoObject;


public class FingerprintAuthenticationDialogFragment extends DialogFragment
        implements FingerprintUiHelper.Callback {

    public interface Callback {
        void onPurchased(CryptoObject cryptoObject);
        void onUsePassword();
    }

    public static FingerprintAuthenticationDialogFragment createInstance() {
        return new FingerprintAuthenticationDialogFragment();
    }

    public static final String TAG = FingerprintAuthenticationDialogFragment.class.getSimpleName();

    @BindView(R.id.btnCancel) Button btnCancel;
    @BindView(R.id.btnSecond) Button btnSecond;
    @BindView(R.id.tvFingerprintStatus) TextView tvFingerStatus;

    private CryptoObject cryptoObject;
    private FingerprintUiHelper fingerprintUiHelper;
    private FingerprintManager fingerpritnManager;

    public Callback callback;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(DialogFragment.STYLE_NORMAL, android.R.style.Theme_Material_Light_Dialog);
        setRetainInstance(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.dialog_fingerprint, container, false);

        ButterKnife.bind(this, view);

        fingerprintUiHelper = new FingerprintUiHelper(fingerpritnManager, tvFingerStatus, this);

        setTitle();
        setFooter();

        return view;
    }

    private void setFooter() {
        btnCancel.setText(R.string.cancel);
        btnSecond.setText(R.string.use_password);
    }

    private void setTitle() {
        getDialog().setTitle(getString(R.string.sign_in));
    }

    @Override
    public void onResume() {
        super.onResume();
        fingerprintUiHelper.startListening(cryptoObject);
    }

    @Override
    public void onPause() {
        super.onPause();
        fingerprintUiHelper.stopListening();
    }

    @Override
    public void onDestroyView() {
        Dialog dialog = getDialog();
        if (dialog != null && getRetainInstance())
            dialog.setDismissMessage(null);

        super.onDestroyView();
    }

    private void goToDialogPassword() {
        callback.onUsePassword();
        fingerprintUiHelper.stopListening();
        dismiss();
    }

    @Override
    public void onAuthenticated() {
        callback.onPurchased(cryptoObject);
        fingerprintUiHelper.stopListening();
        dismiss();
    }

    public void setCryptoObject(CryptoObject cryptoObject) {
        this.cryptoObject = cryptoObject;
    }

    public void setFingerpritnManager(FingerprintManager fingerpritnManager) {
        this.fingerpritnManager = fingerpritnManager;
    }

    @Override
    public void onError() {
//        goToDialogPassword();
    }

    @OnClick(R.id.btnCancel)
    public void onClickCancel(){
        dismiss();
    }

    @OnClick(R.id.btnSecond)
    public void onClickSecondDialog(){
        goToDialogPassword();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        try {
            callback = (Callback) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString()
                    + " must implement ConfigListener");
        }
    }
}
