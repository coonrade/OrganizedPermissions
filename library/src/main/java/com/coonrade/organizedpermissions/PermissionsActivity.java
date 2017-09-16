package com.coonrade.organizedpermissions;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.app.AlertDialog;

@TargetApi(Build.VERSION_CODES.M)
public class PermissionsActivity extends Activity {

    AlertDialog rationaleDialog, blockedDialog;

    final static String EXTRA_PERMISSIONS = "permissions";
    final static String EXTRA_REQUEST_CODE = "request_code";
    final static String EXTRA_ENABLE_RATIONALE_DIALOG = "enable_rationale_dialog";
    final static String EXTRA_RATIONALE_DIALOG_TITLE = "rationale_dialog_title";
    final static String EXTRA_RATIONALE_DIALOG_MESSAGE = "rationale_dialog_message";
    final static String EXTRA_ENABLE_BLOCKED_DIALOG = "enable_blocked_dialog";
    final static String EXTRA_BLOCKED_DIALOG_TITLE = "blocked_dialog_title";
    final static String EXTRA_BLOCKED_DIALOG_MESSAGE = "blocked_dialog_message";
    final static String EXTRA_ENABLE_BACKGROUND_DIM = "enable_background_dim";

    // We can assume that this activity can only be called once before it is finished
    // Therefore we can have this be stored here instead of an array
    static PermissionListener permissionListener;

    private String[] permissions;
    private int requestCode;
    private boolean enableRationaleDialog, enableBlockedDialog;
    private String rationaleDialogTitle, rationaleDialogMessage,
            blockedDialogTitle, blockedDialogMessage;
    private boolean enableBackgroundDim;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        Intent intent = getIntent();

        // enable/disable dim
        enableBackgroundDim = intent.getBooleanExtra(EXTRA_ENABLE_BACKGROUND_DIM, true);
        if (!enableBackgroundDim) setTheme(R.style.PermissionsTheme_DimDisabled);

        super.onCreate(savedInstanceState);

        permissions = intent.getStringArrayExtra(EXTRA_PERMISSIONS);
        requestCode = intent.getIntExtra(EXTRA_REQUEST_CODE, 0);
        enableRationaleDialog = intent.getBooleanExtra(EXTRA_ENABLE_RATIONALE_DIALOG, false);
        enableBlockedDialog = intent.getBooleanExtra(EXTRA_ENABLE_BLOCKED_DIALOG, true);
        rationaleDialogTitle = intent.getStringExtra(EXTRA_RATIONALE_DIALOG_TITLE);
        rationaleDialogMessage = intent.getStringExtra(EXTRA_RATIONALE_DIALOG_MESSAGE);
        blockedDialogTitle = intent.getStringExtra(EXTRA_BLOCKED_DIALOG_TITLE);
        blockedDialogMessage = intent.getStringExtra(EXTRA_BLOCKED_DIALOG_MESSAGE);

        initRequestPermissions();
    }

    private void initRequestPermissions() {
        if (shouldShowRationale(permissions)) {
            showRationale();
        } else {
            // Permission has not been granted yet. Request it directly.
            requestPermissions(permissions, requestCode);

            // Weird fix for flicking issues (flickering = dim on to off quickly)
            // It will disable the flicking when the permissions are requested
            // with the result permissionsBlocked when the blocked dialog is disabled
            // It will not effect the dimness behind a dialog when a dialog is shown
            // You can test this by removing the line of code below and then spamming "request permissions"
            // when all permissions have been blocked and block dialog is disabled
            // This has to be placed here because if a rationale is to be shown then the flickering can happen
            // between the rationale dialog and permission dialog transition
            // By putting it here we avoid this issue
            // To elaborate more on this, as this is very confusing to grasp at first,
            // this condition statement is ONLY called when no permissions have been asked before or
            // when all condition statements have been blocked
            // The first case doesn't matter here but the second one does
            // The flickering issue comes only when nothing was shown to begin with (no dialog is shown like should show rationale)
            // This is when we disable the dimming (all permissions blocked and blocked dialog is disabled)
            // It's important to remember to enable dimming in permissionsBlocked if the blocked dialog is to be shown(also only if dimming was enabled)
            // Another important note is that permission dialogs (provided by the system) are not affected by setTheme() after super.onCreate() is called
            setTheme(R.style.PermissionsTheme_DimDisabled);
        }
    }

    /**
     * Checks to see if the permissions are blocked
     * Returns true if even one of the permissions is blocked
     * The purpose of this is to check ahead of time so that
     * instead of asking for permission, the user sees blocked dialog instead
     * (as at least one permission is blocked)
     */
    private boolean arePermissionsBlocked() {
        for(String permission: permissions){
            if(isPermissionBlocked(permission)) {
                // Permissions blocked (set to never ask again)
                return true;
            }
        }

        // Permissions are not blocked (and also none of them require to show rationale)
        return false;
    }

    /**
     * Helps arePermissionsBlocked() by simplifying the condition statement
     * This returns if a specific permission is blocked
     */
    private boolean isPermissionBlocked(String permission) {
        // If a rationale should not be shown and a permission is not granted
        // Then it can only mean that the permission was blocked
        // This assumes that permission request was asked at least once
        // since it is only utilized in onRequestPermissionResults()
        return !shouldShowRationaleCheck(permission) &&
                !PermissionUtil.isPermissionGranted(getApplicationContext(), permission);
    }

    /**
     * Checks every permission to see if it needs to show rationale
     * If it finds even just one then it has to show rationale for the entire thing
     */
    private boolean shouldShowRationale(String... permissions) {
        for (String permission : permissions) {
            if (shouldShowRationaleCheck(permission)) {
                return true;
            }
        }

        return false;
    }

    private boolean shouldShowRationaleCheck(String permission) {
        return shouldShowRequestPermissionRationale(permission);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {

        if (arePermissionsBlocked()) {
            // onRequestPermissionsResult() would not be called if permissions were blocked before
            // this activity's lifecycle
            permissionsBlocked();
        } else if (PermissionUtil.arePermissionsGranted(getApplicationContext(), permissions)) {
            // All permissions were granted
            permissionsGranted();
        } else {
            // Some or all of the permissions were blocked
            permissionsDenied();
        }
    }

    private void permissionsGranted() {
        if (isListenerProvided())
            permissionListener.onPermissionsGranted(requestCode);
        finish();
    }

    private void permissionsDenied() {
        if (isListenerProvided())
            permissionListener.onPermissionsDenied(requestCode);
        finish();
    }

    private void showRationale() {
        if (isListenerProvided())
            permissionListener.onShowPermissionsRationale(requestCode);

        // Checks if user of lib requested to show rationale dialog
        if (enableRationaleDialog) {
            showRationaleDialog();
        } else {
            requestPermissions(permissions, requestCode);
        }
    }

    /**
     * Passes in if the blocking of permissions happened just now
     * If it happened just now then this only notifies the setListener
     * If this happened in the past (not during the activity lifecycle)
     * then we have the option of showing blocked dialog
     */
    private void permissionsBlocked() {
        if (isListenerProvided())
            permissionListener.onPermissionsBlocked(requestCode);

        if (enableBlockedDialog) {
            // See notes above. when permissionsBlocked is called, the theme is disabled to avoid bug. this turns it back on if needed
            if (enableBackgroundDim)
                setTheme(R.style.PermissionsTheme_DimEnabled);
            showBlockedDialog();
        } else {
            // If the user of lib did not enable blocked dialog then we must call finish() here
            // The blocked dialog handles finish() on its own as it has to be done after interacting with the dialog
            finish();
        }
    }

    /**
     * @return true if the setListener was provided by the user of the library
     */
    private boolean isListenerProvided() {
        return permissionListener != null;
    }

    private void showRationaleDialog() {
        rationaleDialog = baseDialog(rationaleDialogTitle, rationaleDialogMessage)
                .setPositiveButton("Allow", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(@NonNull DialogInterface dialog, int which) {
                        requestPermissions(permissions, requestCode);
                    }
                })
                .setNegativeButton("Deny", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(@NonNull DialogInterface dialog, int which) {
                        rationaleDialogDismissed();
                    }
                }).show();
    }

    private void showBlockedDialog() {
        blockedDialog = baseDialog(blockedDialogTitle, blockedDialogMessage)
                .setPositiveButton("Settings", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(@NonNull DialogInterface dialog, int which) {
                        navigateToAppSystemSettings();
                    }
                })
                .setNegativeButton("Deny", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(@NonNull DialogInterface dialog, int which) {
                        blockedDialogDismissed();
                    }
                }).show();
    }

    private AlertDialog.Builder baseDialog(String title, String message) {
        return new AlertDialog.Builder(this).setTitle(title).setMessage(message).setCancelable(false);
    }

    private void rationaleDialogDismissed() {
        if (isListenerProvided())
            permissionListener.onRationaleDialogDismissed(requestCode);
        finish();
    }

    private void blockedDialogDismissed() {
        if (isListenerProvided())
            permissionListener.onBlockedDialogDismissed(requestCode);
        finish();
    }

    private void navigateToAppSystemSettings() {
        Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
        Uri uri = Uri.fromParts("package", getPackageName(), null);
        intent.setData(uri);
        startActivityForResult(intent, 0);

        // Exit this activity once the settings page is loaded
        finish();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (rationaleDialog != null && rationaleDialog.isShowing()) {
            rationaleDialog.dismiss();
        }

        if (blockedDialog != null && blockedDialog.isShowing()) {
            blockedDialog.dismiss();
        }
    }
}
