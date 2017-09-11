package com.coonrade.organizedpermissions;

public abstract class PermissionListener {
    public abstract void onPermissionsGranted(int requestCode);

    public void onShowPermissionsRationale(int requestCode){}

    public void onRationaleDialogDismissed(int requestCode){}

    public void onPermissionsDenied(int requestCode){}

    public void onPermissionsBlocked(int requestCode){}

    public void onBlockedDialogDismissed(int requestCode){}

    public void onPermissionsError(int requestCode, String errorMessage) {}
}
