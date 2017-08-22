package com.crowderia.helloandroid.common;

import android.content.Context;
import android.content.DialogInterface;

/**
 * Created by Chanuka on 2/27/2017.
 */

public class AlertDialogUtil {

    public static void show(Context context, String title, String message) {

        android.app.AlertDialog.Builder builder1 = new android.app.AlertDialog.Builder(context);
        builder1.setTitle(title);
        builder1.setMessage(message);
        builder1.setCancelable(true);

        builder1.setPositiveButton(
                "Yes",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                    }
                });

        builder1.setNegativeButton(
                "No",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                    }
                });

        android.app.AlertDialog alert11 = builder1.create();
        alert11.show();
    }

}
