package com.codepath.kathyxing.booknook.activities;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.codepath.kathyxing.booknook.ParseQueryUtilities;
import com.codepath.kathyxing.booknook.R;
import com.codepath.kathyxing.booknook.parse_classes.Group;
import com.codepath.kathyxing.booknook.parse_classes.Member;
import com.codepath.kathyxing.booknook.parse_classes.User;
import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseUser;
import com.parse.SaveCallback;

public class JoinGroupActivity extends AppCompatActivity {

    // activity parameters
    public static final String TAG = "JoinGroupActivity";
    private EditText etGroupId;
    private Group group;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        supportRequestWindowFeature(Window.FEATURE_NO_TITLE);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_join_group);
        // Initialize views
        etGroupId = findViewById(R.id.etGroupId);
        Button btnJoinGroup = findViewById(R.id.btnJoinGroup);
        // Set click listener for join group button
        btnJoinGroup.setOnClickListener(v -> joinGroup(etGroupId.getText().toString()));
    }

    private void joinGroup(String groupId) {
        GetCallback<Group> getGroupCallback = (object, e) -> {
            if (e == null) {
                group = object;
                // group gotten successfully, check if user is in group
                userInGroup(object);
            } else {
                // an error occurred
                if (e.getCode() == ParseException.OBJECT_NOT_FOUND) {
                    Toast.makeText(JoinGroupActivity.this,
                            "No group with this ID exists!", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(JoinGroupActivity.this,
                            "Issue joining group", Toast.LENGTH_SHORT).show();
                    Log.e(TAG, "Issue joining group", e);
                }
            }
        };
        ParseQueryUtilities.getGroupAsync(groupId, getGroupCallback);
    }

    // checks if current user is in the group
    // if user is not in the group, add them to the group
    private void userInGroup(Group newGroup) {
        GetCallback<Member> userInGroupCallback = (object, e) -> {
            if (e == null) {
                // user is already in the group
                Toast.makeText(JoinGroupActivity.this,
                        "You are already in this group!", Toast.LENGTH_SHORT).show();
                finish();
            } else {
                if (e.getCode() == ParseException.OBJECT_NOT_FOUND) {
                    // user is not in the group, add user to group
                    addMember(newGroup);
                } else {
                    Log.e(TAG, "issue checking if user is in group", e);
                }
            }
        };
        ParseQueryUtilities.userInGroupAsync(newGroup, (User) ParseUser.getCurrentUser(), userInGroupCallback);
    }

    // adds the current user to the group
    private void addMember(Group newGroup) {
        // create a callback for addMemberWithGroup
        SaveCallback addMemberWithGroupCallback = e -> {
            if (e != null) {
                // Error while creating member
                Log.e(TAG, "Error while creating member", e);
                Toast.makeText(JoinGroupActivity.this, "Failed to join group!", Toast.LENGTH_SHORT).show();
            } else {
                // Successfully created member
                Toast.makeText(JoinGroupActivity.this, "Joined group!", Toast.LENGTH_SHORT).show();
                // put group metadata into the intent
                Intent intent = new Intent();
                intent.putExtra("group", group);
                // set result code and bundle data for response
                setResult(RESULT_OK, intent);
                finish();
            }
        };
        ParseQueryUtilities.addMemberWithGroupAsync(newGroup, (User) ParseUser.getCurrentUser(),
                newGroup.getBookId(), addMemberWithGroupCallback);
    }

}