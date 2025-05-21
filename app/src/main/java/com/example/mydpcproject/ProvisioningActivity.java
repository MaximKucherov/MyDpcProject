package com.example.mydpcproject;

import android.app.admin.DevicePolicyManager;
import android.content.ComponentName;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

public class ProvisioningActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        DevicePolicyManager dpm = (DevicePolicyManager) getSystemService(DEVICE_POLICY_SERVICE);
        ComponentName admin = new ComponentName(this, MyDeviceAdminReceiver.class);

        if (dpm != null && dpm.isDeviceOwnerApp(getPackageName())) {
            setResult(RESULT_OK);
        } else {
            setResult(RESULT_CANCELED);
        }

        finish();
    }
}
