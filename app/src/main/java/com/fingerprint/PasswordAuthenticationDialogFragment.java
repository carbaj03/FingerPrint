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
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.DialogFragment;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;


public class PasswordAuthenticationDialogFragment extends DialogFragment
        implements TextView.OnEditorActionListener {

    public interface Callback {
        void onClickOk();
    }

    public static final String TAG = PasswordAuthenticationDialogFragment.class.getSimpleName();

    @BindView(R.id.btnCancel) Button btnCancel;
    @BindView(R.id.btnSecond) Button btnSecond;

    @BindView(R.id.etPassword) EditText etPassword;
    @BindView(R.id.chkUseFingerprintInFuture) CheckBox chkUseFingerprintFuture;
    @BindView(R.id.tvPasswordDescription) TextView tvPasswordDescription;
    @BindView(R.id.tvNewFingerprintEnrolledDescription) TextView tvNewFingerprintEnrolled;

    private SharedPreferences mSharedPreferences;

    public Callback callback;

    public static PasswordAuthenticationDialogFragment createInstance() {
        return new PasswordAuthenticationDialogFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setRetainInstance(true);
        setStyle(DialogFragment.STYLE_NORMAL, android.R.style.Theme_Material_Light_Dialog);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.dialog_password, container, false);

        ButterKnife.bind(this, view);

        setTitle();
        setFooter();

        etPassword.setOnEditorActionListener(this);

        return view;
    }

    private void setFooter() {
        btnCancel.setText(R.string.cancel);
        btnSecond.setText(R.string.ok);
    }

    private void setTitle() {
        getDialog().setTitle(getString(R.string.sign_in));
    }

    @Override
    public void onDestroyView() {
        Dialog dialog = getDialog();
        if (dialog != null && getRetainInstance())
            dialog.setDismissMessage(null);

        super.onDestroyView();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);

        try {
            callback = (Callback) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString()
                    + " must implement ConfigListener");
        }
    }

    private void verifyPassword() {
        if (!checkPassword(etPassword.getText().toString()))
            return;

        callback.onClickOk();
        dismiss();
    }

    private boolean checkPassword(String password) {
        return password.length() > 0;
    }

    @Override
    public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
        if (actionId == EditorInfo.IME_ACTION_GO) {
            verifyPassword();
            return true;
        }
        return false;
    }

    @OnClick(R.id.btnCancel)
    public void onClickCancel(){
        dismiss();
    }

    @OnClick(R.id.btnSecond)
    public void onClickSecondDialog(){
        verifyPassword();
    }
}
