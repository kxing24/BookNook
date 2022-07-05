package com.codepath.kathyxing.booknook.activities;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.codepath.kathyxing.booknook.ParseQueryUtilities;
import com.codepath.kathyxing.booknook.R;
import com.codepath.kathyxing.booknook.parse_classes.Shelf;
import com.codepath.kathyxing.booknook.parse_classes.User;
import com.parse.ParseException;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import org.json.JSONArray;

public class AddShelfActivity extends AppCompatActivity {

    // activity parameters
    public static final String TAG = "AddShelfActivity";
    private EditText etShelfName;
    private Shelf shelf;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        supportRequestWindowFeature(Window.FEATURE_NO_TITLE);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_shelf);
        // Initialize views
        etShelfName = findViewById(R.id.etShelfName);
        Button btnAddShelf = findViewById(R.id.btnAddShelf);
        // Set click listener for join group button
        btnAddShelf.setOnClickListener(v -> addShelf(etShelfName.getText().toString()));
    }

    private void addShelf(String shelfName) {
        SaveCallback addShelfCallback = e -> {
            if (e == null) {
                Log.i(TAG, "added shelf");
                Toast.makeText(AddShelfActivity.this, "Added shelf!", Toast.LENGTH_SHORT).show();
                // put shelf metadata into the intent
                Intent intent = new Intent();
                intent.putExtra("shelf", shelf);
                // set result code and bundle data for response
                setResult(RESULT_OK, intent);
                finish();
            }
            else {
                Log.e(TAG, "issue adding shelf", e);
                Toast.makeText(AddShelfActivity.this, "Failed to add shelf!", Toast.LENGTH_SHORT).show();
            }
        };
        shelf = ParseQueryUtilities.addShelfAsync(shelfName, addShelfCallback);
    }
}