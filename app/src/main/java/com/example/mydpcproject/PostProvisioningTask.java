package com.example.mydpcproject;

import static android.app.admin.DevicePolicyManager.EXTRA_PROVISIONING_ADMIN_EXTRAS_BUNDLE;
import static android.app.admin.DevicePolicyManager.PERMISSION_GRANT_STATE_GRANTED;

import android.annotation.TargetApi;
import android.app.admin.DevicePolicyManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PermissionInfo;
import android.os.Build;
import android.os.PersistableBundle;
import android.util.Log;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Выполняет действия сразу после завершения QR provisioning:
 * - Выдаёт себе разрешения
 * - Устанавливает affiliationId (если есть)
 * - Запускает следующий экран (например, SetupManagementFragment)
 */
public class PostProvisioningTask {
  private static final String TAG = "PostProvisioningTask";
  private static final String POST_PROV_PREFS = "post_prov_prefs";
  private static final String KEY_POST_PROV_DONE = "key_post_prov_done";

  private final Context mContext;
  private final DevicePolicyManager mDpm;
  private final SharedPreferences mPrefs;

  public PostProvisioningTask(Context context) {
    mContext = context;
    mDpm = (DevicePolicyManager) context.getSystemService(Context.DEVICE_POLICY_SERVICE);
    mPrefs = context.getSharedPreferences(POST_PROV_PREFS, Context.MODE_PRIVATE);
  }

  public boolean performPostProvisioningOperations(Intent intent) {
    if (isPostProvisioningDone()) return false;
    markPostProvisioningDone();

    // Выдать все запрошенные dangerous permissions
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
      autoGrantPermissions();
    }

    // Применить affiliation ID (если есть)
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
      PersistableBundle extras = intent.getParcelableExtra(EXTRA_PROVISIONING_ADMIN_EXTRAS_BUNDLE);
      maybeSetAffiliationId(extras);
    }

    return true;
  }

  public Intent getPostProvisioningLaunchIntent(Intent intent) {
    if (!mDpm.isDeviceOwnerApp(mContext.getPackageName())) return null;

    Intent launch = new Intent(mContext, ProvisioningSuccessActivity.class);
    launch.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
    return launch;
  }

  private boolean isPostProvisioningDone() {
    return mPrefs.getBoolean(KEY_POST_PROV_DONE, false);
  }

  private void markPostProvisioningDone() {
    mPrefs.edit().putBoolean(KEY_POST_PROV_DONE, true).apply();
  }

  @TargetApi(Build.VERSION_CODES.O)
  private void maybeSetAffiliationId(PersistableBundle extras) {
    if (extras == null) return;

    String affiliationId = extras.getString("affiliation_id");
    if (affiliationId != null) {
      ComponentName admin = new ComponentName(mContext, MyDeviceAdminReceiver.class);
      mDpm.setAffiliationIds(admin, Collections.singleton(affiliationId));
      Log.d(TAG, "Set affiliation ID: " + affiliationId);
    }
  }

  @TargetApi(Build.VERSION_CODES.M)
  private void autoGrantPermissions() {
    String pkg = mContext.getPackageName();
    ComponentName admin = new ComponentName(mContext, MyDeviceAdminReceiver.class);

    List<String> dangerousPermissions = getDangerousPermissions(pkg);
    for (String perm : dangerousPermissions) {
      boolean granted = mDpm.setPermissionGrantState(admin, pkg, perm, PERMISSION_GRANT_STATE_GRANTED);
      Log.d(TAG, "Grant " + perm + ": " + granted);
    }
  }

  private List<String> getDangerousPermissions(String packageName) {
    List<String> result = new ArrayList<>();
    try {
      PackageInfo info = mContext.getPackageManager().getPackageInfo(packageName, PackageManager.GET_PERMISSIONS);
      if (info.requestedPermissions != null) {
        for (String perm : info.requestedPermissions) {
          PermissionInfo pi = mContext.getPackageManager().getPermissionInfo(perm, 0);
          if ((pi.protectionLevel & PermissionInfo.PROTECTION_MASK_BASE) == PermissionInfo.PROTECTION_DANGEROUS) {
            result.add(perm);
          }
        }
      }
    } catch (Exception e) {
      Log.e(TAG, "Failed to get permissions", e);
    }
    return result;
  }
}
