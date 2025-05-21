package com.example.mydpcproject;

import android.app.Activity;
import android.app.FragmentManager;
import android.os.Bundle;

/**
 * Activity that gets launched by the {@link
 * android.app.admin.DevicePolicyManager#ACTION_PROVISIONING_SUCCESSFUL} intent.
 */
public class ProvisioningSuccessActivity extends Activity {
  private static final String TAG = "ProvisioningSuccess";

  @Override
  public void onCreate(Bundle icicle) {
    super.onCreate(icicle);

    // Показываем экран с выбором режима использования
    getFragmentManager().beginTransaction()
            .replace(android.R.id.content, new SetupManagementFragment())
            .commit();
  }
}
