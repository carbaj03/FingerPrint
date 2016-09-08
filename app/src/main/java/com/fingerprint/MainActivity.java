package com.fingerprint;

import android.content.Intent;
import android.content.SharedPreferences;
import android.hardware.fingerprint.FingerprintManager;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

import static android.hardware.fingerprint.FingerprintManager.CryptoObject;

@SuppressWarnings("ResourceType")
public class MainActivity extends AppCompatActivity
        implements FingerprintAuthenticationDialogFragment.Callback, PasswordAuthenticationDialogFragment.Callback{

    @BindView(R.id.fab) FloatingActionButton fab;
    @BindView(R.id.tvInfo) TextView tvInfo;

    private SharedPreferences sharedPreferences;

    private CipherAuthenticator cipherAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        setToolbar();

        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);

        cipherAuth = new CipherAuthenticator(this);

        try {
            cipherAuth.create();
        } catch (CustomException e) {
            tvInfo.setText(e.getMessage());
        }
    }

    private void setToolbar() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
    }

    @OnClick(R.id.fab)
    public void onClickFab(){
        tvInfo.setText("");
        try {
            cipherAuth.init();

            if(useFingerprintPreference() && cipherAuth.isFingerprintAuthAvailable())
                showDialogFingerPrint(cipherAuth.getCrypto(), cipherAuth.getFingerprintManager());
            else
                showDialogPassword();
        } catch (CustomException e) {
            tvInfo.setText(e.getMessage());
        }
    }

    private boolean useFingerprintPreference() {
        return sharedPreferences.getBoolean(getString(R.string.use_fingerprint_to_authenticate_key), true);
    }

    private void showDialogFingerPrint(CryptoObject cryptoObject, FingerprintManager fingerprintManager) {
        FingerprintAuthenticationDialogFragment fragment = FingerprintAuthenticationDialogFragment.createInstance();
        fragment.setCryptoObject(cryptoObject);
        fragment.setFingerpritnManager(fingerprintManager);
        fragment.show(getSupportFragmentManager(), FingerprintAuthenticationDialogFragment.TAG);
    }

    private void showDialogPassword() {
        PasswordAuthenticationDialogFragment fragment = PasswordAuthenticationDialogFragment.createInstance();
        fragment.show(getSupportFragmentManager(), PasswordAuthenticationDialogFragment.TAG);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
      if (item.getItemId() == R.id.action_settings) {
            Intent intent = new Intent(this, SettingsActivity.class);
            startActivity(intent);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onPurchased(CryptoObject cryptoObject) {
        tvInfo.setText(R.string.action_success);
    }

    @Override
    public void onUsePassword() {
        showDialogPassword();
    }

    @Override
    public void onClickOk() {
        tvInfo.setText(R.string.action_success);
    }
}
