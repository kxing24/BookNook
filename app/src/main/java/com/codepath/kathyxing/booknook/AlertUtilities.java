package com.codepath.kathyxing.booknook;

import android.content.Context;

import androidx.appcompat.app.AlertDialog;

public final class AlertUtilities {

    // implement this interface to switch activities after the alert is shown
    public interface GoActivity {
        void goActivity();
    }

    // show the alert
    public static void showAlert(Context context, String title, String message, boolean error, GoActivity activity) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context)
                .setTitle(title)
                .setMessage(message)
                .setPositiveButton("OK", (dialog, which) -> {
                    dialog.cancel();
                    if (!error) {
                        activity.goActivity();
                    }
                });
        AlertDialog ok = builder.create();
        ok.show();
    }
}
