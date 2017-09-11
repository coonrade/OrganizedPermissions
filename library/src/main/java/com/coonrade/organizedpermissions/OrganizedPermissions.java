package com.coonrade.organizedpermissions;

import android.content.Context;
import android.content.Intent;

import com.coonrade.organizedpermissions.util.ArrayUtil;
import com.coonrade.organizedpermissions.util.ContextProvider;

import java.util.ArrayList;
import java.util.List;

public class OrganizedPermissions {
    private Context context;
    private PermissionListener permissionListener;
    private Integer[] permissionGroups;

    // Rationale dialog is NOT enabled by default
    // Blocked dialog is enabled by default
    private boolean enableRationaleDialog = false, enableBlockedDialog = true;

    private String rationaleDialogTitle = "", rationaleDialogMessage = "",
            blockedDialogTitle = "", blockedDialogMessage = "";

    // Dim can be turned on when a dialog, for requesting permissions, comes up
    // It is turned on by default
    private boolean enableBackgroundDim = true;

    private static final String DEFAULT_RATIONALE_DIALOG_TITLE = "Permissions Request";

    private static final String DEFAULT_RATIONALE_DIALOG_MESSAGE =
            "These permissions are needed for the app to function properly";

    private static final String DEFAULT_BLOCKED_DIALOG_TITLE = "Permissions Have Been Blocked";

    private static final String DEFAULT_BLOCKED_DIALOG_MESSAGE =
            "Please go to settings to enable the needed permissions";

    private static final int DEFAULT_REQUEST_CODE = 0;

    private static final String ERROR_MESSAGE_MISSING_PERMISSIONS_IN_MANIFEST = "Permission(s) are missing in the manifest";

    @SuppressWarnings("unused")
    public static OrganizedPermissions init(@PermissionGroup int permission, @PermissionGroup int... permissions) {
        return new OrganizedPermissions(mergePermissionWithPermissions(permission, permissions));
    }

    @SuppressWarnings("unused")
    public static OrganizedPermissions init(Context context, @PermissionGroup int permission, @PermissionGroup int... permissions) {
        return new OrganizedPermissions(context, mergePermissionWithPermissions(permission, permissions));
    }

    /**
     * In order to create a compile check to require at least one permission
     * but also let permissions be passed as varargs,
     * we must split them into two args in the init() func
     * This takes both of them and merges into one
     */
    private static Integer[] mergePermissionWithPermissions(@PermissionGroup int permissionGroup, @PermissionGroup int... permissionGroups) {
        List<Integer> mergedPermissions = new ArrayList<>();
        mergedPermissions.add(permissionGroup);

        for (int permissionGroupFromArray : permissionGroups) {
            mergedPermissions.add(permissionGroupFromArray);
        }

        return ArrayUtil.listToIntegerArray(mergedPermissions);
    }

    private OrganizedPermissions(Integer[] permissions) {
        context = ContextProvider.get();
        permissionGroups = permissions;
    }

    private OrganizedPermissions(Context context, Integer[] permissions) {
        this.context = context;
        permissionGroups = permissions;
    }

    @SuppressWarnings("unused")
    public OrganizedPermissions setListener(PermissionListener listener) {
        permissionListener = listener;
        return this;
    }

    @SuppressWarnings("unused")
    public OrganizedPermissions enableRationaleDialog(boolean enableRationaleDialog) {
        this.enableRationaleDialog = enableRationaleDialog;
        return this;
    }

    @SuppressWarnings("unused")
    public OrganizedPermissions rationaleDialogTitle(String title) {
        rationaleDialogTitle = title;
        return this;
    }

    @SuppressWarnings("unused")
    public OrganizedPermissions rationaleDialogMessage(String message) {
        rationaleDialogMessage = message;
        return this;
    }

    // Set true if you want to show dialog that can send the user to app's system settings
    @SuppressWarnings("unused")
    public OrganizedPermissions enableBlockedDialog(boolean enableBlockedDialog) {
        this.enableBlockedDialog = enableBlockedDialog;
        return this;
    }

    @SuppressWarnings("unused")
    public OrganizedPermissions blockedDialogTitle(String title) {
        blockedDialogTitle = title;
        return this;
    }

    @SuppressWarnings("unused")
    public OrganizedPermissions blockedDialogMessage(String message) {
        blockedDialogMessage = message;
        return this;
    }

    @SuppressWarnings("unused")
    public OrganizedPermissions enableBackgroundDim(boolean enableBackgroundDim) {
        this.enableBackgroundDim = enableBackgroundDim;
        return this;
    }

    @SuppressWarnings("unused")
    public void check() {
        checkPermissions(DEFAULT_REQUEST_CODE);
    }

    @SuppressWarnings("unused")
    public void check(int requestCode) {
        checkPermissions(requestCode);
    }

    private void checkPermissions(int requestCode) {
        if (PermissionUtil.isRuntimePermissionsEnabled()) {
            // Check to make sure each permission group has at least one permission in the manifest
            // If that's not the case then throw error to avoid strange runtime bugs
            if (PermissionUtil.arePermissionGroupsInManifest(context, permissionGroups)) {
                String[] permissions = PermissionUtil.formatPermissionGroups(permissionGroups);
                if (PermissionUtil.arePermissionsGranted(context, permissions)) {
                    permissionsGranted(requestCode);
                } else {
                    requestPermissionInActivity(requestCode, permissions);
                }
            } else {
                permissionsError(requestCode, ERROR_MESSAGE_MISSING_PERMISSIONS_IN_MANIFEST);
            }
        } else { // Pre Marshmallow android version, doesn't have runtime permissions
            permissionsGranted(requestCode);
        }
    }

    private void permissionsGranted(int requestCode) {
        if (permissionListener != null)
            permissionListener.onPermissionsGranted(requestCode);
    }

    private void permissionsError(int requestCode, String errorMessage) {
        if (permissionListener != null)
            permissionListener.onPermissionsError(requestCode, errorMessage);
    }

    private void requestPermissionInActivity(int requestCode, String... permissions) {
        // Prepare setListener by storing into Permissions Activity
        // Permission setListener can be null
        PermissionsActivity.permissionListener = permissionListener;

        Intent intent = new Intent(context, PermissionsActivity.class);

        intent.putExtra(PermissionsActivity.EXTRA_PERMISSIONS, permissions);
        intent.putExtra(PermissionsActivity.EXTRA_REQUEST_CODE, requestCode);

        intent.putExtra(PermissionsActivity.EXTRA_ENABLE_RATIONALE_DIALOG, enableRationaleDialog);
        intent.putExtra(PermissionsActivity.EXTRA_ENABLE_BLOCKED_DIALOG, enableBlockedDialog);

        // Checks if title/message was added for the dialogs and uses the ones provided or default
        if (!rationaleDialogTitle.isEmpty())
            putExtraRationaleDialogTitle(intent, rationaleDialogTitle);
        else
            putExtraRationaleDialogTitle(intent, DEFAULT_RATIONALE_DIALOG_TITLE);

        if (!rationaleDialogMessage.isEmpty())
            putExtraRationaleDialogMessage(intent, rationaleDialogMessage);
        else
            putExtraRationaleDialogMessage(intent, DEFAULT_RATIONALE_DIALOG_MESSAGE);

        if (!blockedDialogTitle.isEmpty())
            putExtraBlockedDialogTitle(intent, blockedDialogTitle);
        else
            putExtraBlockedDialogTitle(intent, DEFAULT_BLOCKED_DIALOG_TITLE);

        if (!blockedDialogMessage.isEmpty())
            putExtraBlockedDialogMessage(intent, blockedDialogMessage);
        else
            putExtraBlockedDialogMessage(intent, DEFAULT_BLOCKED_DIALOG_MESSAGE);

        intent.putExtra(PermissionsActivity.EXTRA_ENABLE_BACKGROUND_DIM, enableBackgroundDim);

        // Start the permissions activity
        context.startActivity(intent);
    }

    private void putExtraRationaleDialogTitle(Intent intent, String title) {
        intent.putExtra(PermissionsActivity.EXTRA_RATIONALE_DIALOG_TITLE, title);
    }

    private void putExtraRationaleDialogMessage(Intent intent, String message) {
        intent.putExtra(PermissionsActivity.EXTRA_RATIONALE_DIALOG_MESSAGE, message);
    }

    private void putExtraBlockedDialogTitle(Intent intent, String title) {
        intent.putExtra(PermissionsActivity.EXTRA_BLOCKED_DIALOG_TITLE, title);
    }

    private void putExtraBlockedDialogMessage(Intent intent, String message) {
        intent.putExtra(PermissionsActivity.EXTRA_BLOCKED_DIALOG_MESSAGE, message);
    }
}
