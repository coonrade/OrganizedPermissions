package com.coonrade.organizedpermissions;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.support.annotation.Nullable;

import com.coonrade.organizedpermissions.util.ArrayUtil;

import java.util.ArrayList;
import java.util.List;

class PermissionUtil {

    @TargetApi(Build.VERSION_CODES.M)
    static boolean arePermissionsGranted(Context context, String... permissions) {
        for (String permission : permissions) {
            if (!isPermissionGranted(context, permission)) {
                return false;
            }
        }

        return true;
    }

    @TargetApi(Build.VERSION_CODES.M)
    static boolean isPermissionGranted(Context context, String permission) {
        return context.checkSelfPermission(permission) == PackageManager.PERMISSION_GRANTED;
        //return ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED;
    }

    static boolean isRuntimePermissionsEnabled() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.M;
    }

    /**
     * Checks to make sure that the manifest contains at least one permission
     * from each permission group that was passed in as an argument
     * An error can happen if none of the permissions from a permission group are included in the manifest
     */
    static boolean arePermissionGroupsInManifest(Context context, Integer[] permissionGroups)
    {
        String[] manifestPermissions = getManifestPermissions(context);

        // Will return null if there are no permissions in the manifest
        if (manifestPermissions != null) {
            // Check every permission group
            for (int permissionGroup : permissionGroups) {
                // If we find a permission group that has no permissions in the manifest then we automatically return false
                if (!isPermissionGroupInManifest(manifestPermissions, permissionGroup)) {
                    return false;
                }
            }

            // Only returns true if for loop is done running
            // This means every permission group has at least one permission in the manifest
            return true;
        }

        return false;
    }

    /**
     * Checks if a permission group has at least one permission in the manifest
     */
    private static boolean isPermissionGroupInManifest(String[] manifestPermissions, int permissionGroup) {
        String[] permissions = getPermissionsFromGroup(permissionGroup);

        for (String permission : permissions) {
            if(isPermissionInManifest(manifestPermissions, permission)) {
                return true;
            }
        }

        return false;
    }

    /**
     * Checks if a specific permission is in the manifest
     */
    private static boolean isPermissionInManifest(String[] manifestPermissions, String permission) {
        for (String manifestPermission : manifestPermissions) {
            if (manifestPermission.equals(permission)) {
                return true;
            }
        }

        return false;
    }

    /**
     * Returns list of all permissions in the manifest
     */
    @Nullable
    private static String[] getManifestPermissions(Context context) {
        try {
            PackageInfo info = context.getPackageManager().getPackageInfo(context.getPackageName(), PackageManager.GET_PERMISSIONS);
            return info.requestedPermissions;
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    /**
     * Gives an array of all permissions that are part of permission group
     */
    private static String[] getPermissionsFromGroup(@PermissionGroup int permissionGroup) {
        switch (permissionGroup) {
            case PermissionGroup.CALENDAR:
                return PermissionList.CALENDAR;
            case PermissionGroup.CAMERA:
                return PermissionList.CAMERA;
            case PermissionGroup.CONTACTS:
                return PermissionList.CONTACTS;
            case PermissionGroup.LOCATION:
                return PermissionList.LOCATION;
            case PermissionGroup.MICROPHONE:
                return PermissionList.MICROPHONE;
            case PermissionGroup.PHONE:
                return PermissionList.PHONE;
            case PermissionGroup.SENSORS:
                return PermissionList.SENSORS;
            case PermissionGroup.SMS:
                return PermissionList.SMS;
            case PermissionGroup.STORAGE:
            default:
                return PermissionList.STORAGE;
        }
    }

    /**
     * Takes in the readable permission groups as array
     * and returns an array of permissions equivalent to those passed
     * but ones that will be used by OS to understand which permissions to grant
     */
    static String[] formatPermissionGroups(@PermissionGroup Integer[] permissions) {
        List<String> formattedPermissions = new ArrayList<>();

        for (@PermissionGroup int permission : permissions) {
            formattedPermissions.add(getPermissionFromGroup(permission));
        }

        return ArrayUtil.listToStringArray(formattedPermissions);
    }

    /**
     * If any permission in a permission group is granted, all other permissions in the same group will be automatically granted as well
     * Therefore, only one of the permissions is needed from the group
     * To be consistent this will return the first permission from a permission group
     */
    private static String getPermissionFromGroup(@PermissionGroup int permissionGroup) {
        return getPermissionsFromGroup(permissionGroup)[0];
    }
}
