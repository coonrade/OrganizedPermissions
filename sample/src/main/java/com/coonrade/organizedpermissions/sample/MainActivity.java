package com.coonrade.organizedpermissions.sample;

import android.content.Context;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.coonrade.organizedpermissions.OrganizedPermissions;
import com.coonrade.organizedpermissions.PermissionGroup;
import com.coonrade.organizedpermissions.PermissionListener;

public class MainActivity extends AppCompatActivity {

    private TextView text;

    private static final int REQUEST_CODE = 123;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        text = (TextView) findViewById(R.id.text);
    }

    public void runFindLocation(View view) {
        Log.v("Button", "pressed");

        OrganizedPermissions.init(PermissionGroup.LOCATION, PermissionGroup.STORAGE)
                .enableRationaleDialog(false)
                .enableBlockedDialog(true)
                .enableBackgroundDim(true)
                .setListener(new PermissionListener() {
                    private String TAG = "PERMISSIONS LISTENER";

                    @Override
                    public void onPermissionsGranted(int requestCode) {
                        Log.v(TAG, "granted");

                        findLocation();
                    }

                    @Override
                    public void onPermissionsDenied(int requestCode) {
                        Log.v(TAG, "denied");
                    }

                    @Override
                    public void onShowPermissionsRationale(int requestCode) {
                        Log.v(TAG, "show rationale");
                    }

                    @Override
                    public void onPermissionsBlocked(int requestCode) {
                            Log.v(TAG, "blocked");
                    }

                    @Override
                    public void onRationaleDialogDismissed(int requestCode) {
                        Log.v(TAG, "rationale dialog dismissed");
                    }

                    @Override
                    public void onBlockedDialogDismissed(int requestCode) {
                        Log.v(TAG, "blocked dialog dismissed");
                    }

                    @Override
                    public void onPermissionsError(int requestCode, String errorMessage) {
                        Log.v(TAG, errorMessage);
                    }
                }).check(REQUEST_CODE);

    }

    // Find a way to suppress warning without user of the lib placing @SuppressWarnings annotation
    // https://stackoverflow.com/questions/35124794/android-studio-remove-security-exception-warning
    //@SuppressWarnings("MissingPermission") // Sometimes it doesn't work?
    @SuppressWarnings("ResourceType")
    private void findLocation() {
        // this is an old method of finding last location (change it)
        LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        Criteria criteria = new Criteria();
        String provider = locationManager.getBestProvider(criteria, true);

        Location location = locationManager.getLastKnownLocation(provider);

        if (location != null){
            text.setText("Location: " + location.getLatitude() + ", " + location.getLongitude());
            // Store location info...
        } else {
            text.setText("Location cannot be found");
        }
    }

}
