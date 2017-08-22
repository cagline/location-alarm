package com.crowderia.helloandroid;

import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;

public final class PermissionUtil {

    private static final int PERMISSION_ACCESS_COARSE_LOCATION = 11;
    private static final int PERMISSION_ACCESS_FINE_LOCATION = 12;
    private static final int PERMISSION_INTERNET = 13;

    private static final int PERMISSION_WAKE_LOCK = 14;
    private static final int PERMISSION_VIBRATE = 15;

    public static boolean checkSelfPermission(Context context, String manifestPermision) {
        return ContextCompat.checkSelfPermission(context, manifestPermision) == PackageManager.PERMISSION_GRANTED;
    }

    public static void requestPermissions(Activity context, String... perms) {
        for(String perm : perms ) {
            switch (perm) {
                case "ACCESS_FINE_LOCATION":
                    ActivityCompat.requestPermissions(context, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION}, PERMISSION_ACCESS_FINE_LOCATION);
                case "ACCESS_COARSE_LOCATION":
                    ActivityCompat.requestPermissions(context, new String[]{android.Manifest.permission.ACCESS_COARSE_LOCATION}, PERMISSION_ACCESS_COARSE_LOCATION);
                case "INTERNET":
                    ActivityCompat.requestPermissions(context, new String[]{android.Manifest.permission.INTERNET}, PERMISSION_INTERNET);
                case "WAKE_LOCK":
                    ActivityCompat.requestPermissions(context, new String[]{android.Manifest.permission.WAKE_LOCK}, PERMISSION_WAKE_LOCK);
                case "VIBRATE":
                    ActivityCompat.requestPermissions(context, new String[]{android.Manifest.permission.VIBRATE}, PERMISSION_VIBRATE);
                default:
            }
        }
    }
}
