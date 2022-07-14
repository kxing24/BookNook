package com.codepath.kathyxing.booknook;

import android.content.Context;
import android.content.DialogInterface;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;

import com.codepath.kathyxing.booknook.parse_classes.User;
import com.parse.ParseUser;

import java.util.ArrayList;

public final class AlertUtilities {

    // implement this interface to switch activities after the alert is shown
    public interface GoActivity {
        void goActivity();
    }

    // show the standard alert
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

    // show the list of genres
    public static void showGenresAlert(@NonNull Context context, int numGenres, @NonNull String[] genres) {
        ArrayList<String> selectedGenres = new ArrayList<>();
        boolean[] selected = new boolean[genres.length];
        // Initialize alert dialog
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        // set title
        builder.setTitle("Select your top " + numGenres + " genres");
        // set dialog non cancelable
        builder.setCancelable(false);
        builder.setMultiChoiceItems(genres, selected, new DialogInterface.OnMultiChoiceClickListener() {
            int count = 0;
            @Override
            public void onClick(DialogInterface dialogInterface, int position, boolean isChecked) {
                // kep track of the number of items selected
                count += isChecked ? 1 : -1;
                // make sure the count does not exceed the number of genres to select
                if (count > numGenres) {
                    Toast.makeText(context, "You can only select " + numGenres + " genres", Toast.LENGTH_SHORT).show();
                    selected[position] = false;
                    count--;
                    ((AlertDialog) dialogInterface).getListView().setItemChecked(position, false);
                } else {
                    if (isChecked) {
                        // when checkbox selected, add position to selectedShelvesPositions
                        selectedGenres.add(genres[position]);
                    } else {
                        // when checkbox unselected, remove position from selectedShelvesPositions
                        selectedGenres.remove(genres[position]);
                    }
                }
                // if the user has selected the correct number of genres, enable the button
                ((AlertDialog) dialogInterface).getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(count == numGenres);
            }
        });
        builder.setPositiveButton("Done", (dialogInterface, i) -> {
            // Add the genres to the user
            ParseQueryUtilities.saveGenresAsync((User) ParseUser.getCurrentUser(), selectedGenres);
        });
        AlertDialog dialog = builder.create();
        dialog.show();
        // Initially disable the done button
        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(false);
    }

}
