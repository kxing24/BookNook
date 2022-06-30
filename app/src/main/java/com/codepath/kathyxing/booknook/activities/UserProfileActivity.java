package com.codepath.kathyxing.booknook.activities;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.bumptech.glide.Glide;
import com.codepath.kathyxing.booknook.ParseQueryUtilities;
import com.codepath.kathyxing.booknook.R;
import com.codepath.kathyxing.booknook.parse_classes.Friend;
import com.codepath.kathyxing.booknook.parse_classes.User;
import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseUser;
import com.parse.SaveCallback;

public class UserProfileActivity extends AppCompatActivity {

    // activity parameters
    public static final String TAG = "UserProfileActivity";
    private ImageView ivProfilePicture;
    private TextView tvUsername;
    private TextView tvProfileDescription;
    private Button btnAddFriend;
    private Button btnAcceptFriend;
    private TextView tvFriendRequestSent;
    private TextView tvFriends;
    private Button btnEditProfile;
    private EditText etProfileDescription;
    private Button btnSave;
    private Button btnCancel;
    private User user;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_profile);
        // set up the toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Profile");
        }
        // have the toolbar show a back button
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        toolbar.setNavigationOnClickListener(v -> finish());
        // initialize views
        ivProfilePicture = findViewById(R.id.ivProfilePicture);
        tvUsername = findViewById(R.id.tvUsername);
        tvProfileDescription = findViewById(R.id.tvProfileDescription);
        btnAddFriend = findViewById(R.id.btnAddFriend);
        btnAcceptFriend = findViewById(R.id.btnAcceptFriend);
        tvFriendRequestSent = findViewById(R.id.tvFriendRequestSent);
        tvFriends = findViewById(R.id.tvFriends);
        btnEditProfile = findViewById(R.id.btnEditProfile);
        etProfileDescription = findViewById(R.id.etProfileDescription);
        btnSave = findViewById(R.id.btnSave);
        btnCancel = findViewById(R.id.btnCancel);
        user = (User) getIntent().getExtras().get("user");
        // set the views
        tvUsername.setText(user.getUsername());
        if (user.getProfileDescription() == null) {
            tvProfileDescription.setText("This user has no description");
            etProfileDescription.setText("");
        } else {
            tvProfileDescription.setText(user.getProfileDescription());
            etProfileDescription.setText(user.getProfileDescription());
        }
        // load in profile picture with glide
        ParseFile profilePicture = user.getProfilePicture();
        Glide.with(this).load(profilePicture.getUrl()).circleCrop().into(ivProfilePicture);
        // set view visibility
        if (!user.getObjectId().equals(ParseUser.getCurrentUser().getObjectId())) {
            setBtnAddFriendVisibility();
            setBtnAcceptFriendVisibility();
            setTvFriendRequestSentVisibility();
            setTvFriendsVisibility();
        } else {
            btnEditProfile.setVisibility(View.VISIBLE);
        }
        // set click handler for add friend button
        btnAddFriend.setOnClickListener(v -> requestFriend());
        // set click handler for accept friend button
        btnAcceptFriend.setOnClickListener(v -> acceptFriend());
        // set click handler for edit profile button
        btnEditProfile.setOnClickListener(v -> {
            btnEditProfile.setVisibility(View.GONE);
            tvProfileDescription.setVisibility(View.GONE);
            etProfileDescription.setVisibility(View.VISIBLE);
            btnCancel.setVisibility(View.VISIBLE);
            btnSave.setVisibility(View.VISIBLE);
        });
        // set click handler for save button
        btnSave.setOnClickListener(v -> {
            String newDescription = etProfileDescription.getText().toString();
            doneEditingProfile(newDescription);
            saveUserDescription(newDescription);
            Toast.makeText(UserProfileActivity.this, "Saved profile changes!", Toast.LENGTH_SHORT).show();
        });
        // set click handler for cancel button
        btnCancel.setOnClickListener(v -> {
            String oldDescription = tvProfileDescription.getText().toString();
            doneEditingProfile(oldDescription);
        });
    }

    private void saveUserDescription(String description) {
        SaveCallback saveUserDescriptionCallback = e -> {
            if (e != null) {
                Log.e(TAG, "Error while saving", e);
            }
            else {
                Log.i(TAG, "Profile description save was successful");
            }
        };
        ParseQueryUtilities.saveUserDescriptionAsync(user, description, saveUserDescriptionCallback);
    }

    private void doneEditingProfile(String description) {
        tvProfileDescription.setText(description);
        tvProfileDescription.setVisibility(View.VISIBLE);
        etProfileDescription.setVisibility(View.GONE);
        btnSave.setVisibility(View.GONE);
        btnCancel.setVisibility(View.GONE);
        btnEditProfile.setVisibility(View.VISIBLE);
    }

    private void requestFriend() {
        SaveCallback requestFriendCallback = e -> {
            if (e == null) {
                btnAddFriend.setVisibility(View.GONE);
                tvFriendRequestSent.setVisibility(View.VISIBLE);
            } else {
                Toast.makeText(UserProfileActivity.this,
                        "Error sending friend request!", Toast.LENGTH_SHORT).show();
            }
        };
        ParseQueryUtilities.requestFriendAsync(user, requestFriendCallback);
    }

    private void acceptFriend() {
        SaveCallback acceptFriendCallback = e -> {
            if (e == null) {
                Log.i(TAG, "Successfully accepted friend");
                btnAcceptFriend.setVisibility(View.GONE);
                tvFriends.setVisibility(View.VISIBLE);
            } else {
                Toast.makeText(UserProfileActivity.this, "Error accepting friend!",
                        Toast.LENGTH_SHORT).show();
            }
        };
        GetCallback<Friend> getFriendReceivingCallback = (friend, e) -> {
            if (e == null) {
                ParseQueryUtilities.acceptFriendAsync(friend, acceptFriendCallback);
            }
        };
        ParseQueryUtilities.getFriendReceivingAsync(user, getFriendReceivingCallback);
    }

    private void setBtnAddFriendVisibility() {
        GetCallback<Friend> getFriendStatusCallback = (object, e) -> {
            if (e != null) {
                if (e.getCode() == ParseException.OBJECT_NOT_FOUND) {
                    // friend doesn't exist
                    btnAddFriend.setVisibility(View.VISIBLE);
                } else {
                    // error is something other than the friend not existing
                    Log.e(TAG, "Error setting add friend button visibility", e);
                }
            }
        };
        ParseQueryUtilities.getFriendStatusAsync(user, getFriendStatusCallback);
    }

    private void setBtnAcceptFriendVisibility() {
        GetCallback<Friend> getReceivingFriendStatusCallback = (object, e) -> {
            if (e == null) {
                // no error, friend exists
                if (!object.getAccepted()) {
                    // current user receiving friend
                    btnAcceptFriend.setVisibility(View.VISIBLE);
                }
            } else {
                if (e.getCode() != ParseException.OBJECT_NOT_FOUND) {
                    // error is something other than the friend not existing, debug
                    Log.e(TAG, "Error setting accept friend button visibility", e);
                }
            }
        };
        ParseQueryUtilities.getReceivingFriendStatusAsync(user, getReceivingFriendStatusCallback);
    }

    private void setTvFriendRequestSentVisibility() {
        GetCallback<Friend> getRequestingFriendStatusCallback = (object, e) -> {
            if (e == null) {
                // no error, friend exists
                if (!object.getAccepted()) {
                    // current user requesting friend
                    tvFriendRequestSent.setVisibility(View.VISIBLE);
                }
            } else {
                // error is something other than the friend not existing
                if (e.getCode() != ParseException.OBJECT_NOT_FOUND) {
                    // debug
                    Log.e(TAG, "Error setting accept friend button visibility", e);
                }
            }
        };
        ParseQueryUtilities.getRequestingFriendStatusAsync(user, getRequestingFriendStatusCallback);
    }

    private void setTvFriendsVisibility() {
        GetCallback<Friend> getFriendStatusCallback = (object, e) -> {
            if (e == null) {
                // no error, friend exists
                if (object.getAccepted()) {
                    // current user and other user are friends
                    tvFriends.setVisibility(View.VISIBLE);
                }
            } else {
                // error is something other than the friend not existing
                if (e.getCode() != ParseException.OBJECT_NOT_FOUND) {
                    // debug
                    Log.e(TAG, "Error setting accept friend button visibility", e);
                }
            }
        };
        ParseQueryUtilities.getFriendStatusAsync(user, getFriendStatusCallback);
    }
}