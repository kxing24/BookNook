package com.codepath.kathyxing.booknook.activities;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.codepath.kathyxing.booknook.ParseQueryUtilities;
import com.codepath.kathyxing.booknook.R;
import com.codepath.kathyxing.booknook.parse_classes.Group;
import com.codepath.kathyxing.booknook.parse_classes.User;
import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.SaveCallback;

public class JoinGroupActivity extends AppCompatActivity {

    // activity parameters
    public static final String TAG = "JoinGroupActivity";
    private EditText etGroupId;
    private Button btnJoinGroup;
    private Group group;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        supportRequestWindowFeature(Window.FEATURE_NO_TITLE);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_join_group);
        // Initialize views
        etGroupId = findViewById(R.id.etGroupId);
        btnJoinGroup = findViewById(R.id.btnJoinGroup);
        // Set click listener for join group button
        btnJoinGroup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                joinGroupAsync(etGroupId.getText().toString());
            }
        });
    }

    private void joinGroupAsync(String groupId) {
        // create a callback for addMemberWithGroup
        SaveCallback addMemberWithGroupCallback = new SaveCallback() {
            @Override
            public void done(ParseException e) {
                // Error while creating member
                if (e != null) {
                    Log.e(TAG, "Error while creating member", e);
                    Toast.makeText(JoinGroupActivity.this, "Failed to join group!", Toast.LENGTH_SHORT).show();
                }
                // Successfully created member
                else {
                    Toast.makeText(JoinGroupActivity.this, "Joined group!", Toast.LENGTH_SHORT).show();
                    // put group metadata into the intent
                    Intent intent = new Intent();
                    intent.putExtra("group", group);
                    // set result code and bundle data for response
                    setResult(RESULT_OK, intent);
                    finish();
                }
            }
        };
        ParseQuery<Group> query = ParseQuery.getQuery(Group.class);
        query.include(Group.KEY_BOOK_ID);
        query.whereEqualTo(Group.KEY_OBJECT_ID, groupId);
        query.getFirstInBackground(new GetCallback<Group>() {
            @Override
            public void done(Group object, ParseException e) {
                group = object;
                //TODO: check if user is already in group
                if (e == null) {
                    ParseQueryUtilities.addMemberWithGroupAsync(object, (User) ParseUser.getCurrentUser(),
                            object.getBookId(), addMemberWithGroupCallback);
                }
                else {
                    Toast.makeText(JoinGroupActivity.this, "Issue joining group!", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
}