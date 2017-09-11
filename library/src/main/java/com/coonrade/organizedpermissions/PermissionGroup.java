package com.coonrade.organizedpermissions;

import android.support.annotation.IntDef;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@SuppressWarnings("WeakerAccess")
@Retention(RetentionPolicy.SOURCE)
@IntDef({PermissionGroup.CALENDAR,
        PermissionGroup.CAMERA,
        PermissionGroup.CONTACTS,
        PermissionGroup.LOCATION,
        PermissionGroup.MICROPHONE,
        PermissionGroup.PHONE,
        PermissionGroup.SENSORS,
        PermissionGroup.SMS,
        PermissionGroup.STORAGE})
public @interface PermissionGroup {
    /**
     * Calendar group permission
     */
    int CALENDAR = 0;


    // Camera permissions
    int CAMERA = 1;


    // Contacts permissions
    int CONTACTS = 2;


    // Location permissions
    int LOCATION = 3;


    // Microphone permissions
    int MICROPHONE = 4;


    // Phone permissions
    int PHONE = 5;


    // Sensors permissions
    int SENSORS = 6;


    // SMS permissions
    int SMS = 7;


    // Storage permissions
    int STORAGE = 8;
}
