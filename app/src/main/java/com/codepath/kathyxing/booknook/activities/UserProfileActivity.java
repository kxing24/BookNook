package com.codepath.kathyxing.booknook.activities;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.codepath.kathyxing.booknook.ParseQueryUtilities;
import com.codepath.kathyxing.booknook.R;
import com.codepath.kathyxing.booknook.parse_classes.Friend;
import com.codepath.kathyxing.booknook.parse_classes.User;
import com.parse.FindCallback;
import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import java.util.List;

public class UserProfileActivity extends AppCompatActivity {

    // activity parameters
    public static final String TAG = "UserProfileActivity";
    private ImageView ivProfilePicture;
    private TextView tvUsername;
    private Button btnAddFriend;
    private Button btnAcceptFriend;
    private TextView tvFriendRequestSent;
    private TextView tvFriends;
    private User user;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_profile);
        // set up the toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("");
        // have the toolbar show a back button
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        // initialize views
        ivProfilePicture = findViewById(R.id.ivProfilePicture);
        tvUsername = findViewById(R.id.tvUsername);
        btnAddFriend = findViewById(R.id.btnAddFriend);
        btnAcceptFriend = findViewById(R.id.btnAcceptFriend);
        tvFriendRequestSent = findViewById(R.id.tvFriendRequestSent);
        tvFriends = findViewById(R.id.tvFriends);
        user = (User) getIntent().getExtras().get("user");
        // set the views
        tvUsername.setText(user.getUsername());
        // load in profile picture with glide
        ParseFile profilePicture = user.getProfilePicture();
        Glide.with(this).load(profilePicture.getUrl()).circleCrop().into(ivProfilePicture);
        // set view visibility
        if (!user.getObjectId().equals(ParseUser.getCurrentUser().getObjectId())) {
            setBtnAddFriendVisibility();
            setBtnAcceptFriendVisibility();
            setTvFriendRequestSentVisibility();
            setTvFriendsVisibility();
        }
        // set click handler for add friend button
        btnAddFriend.setOnClickListener(v -> requestFriend());
        // set click handler for accept friend button
        btnAcceptFriend.setOnClickListener(v -> acceptFriend());
    }

    private void requestFriend() {
        SaveCallback requestFriendCallback = new SaveCallback() {
            @Override
            public void done(ParseException e) {
                if (e == null) {
                    btnAddFriend.setVisibility(View.GONE);
                    tvFriendRequestSent.setVisibility(View.VISIBLE);
                } else {
                    Toast.makeText(UserProfileActivity.this,
                            "Error sending friend request!", Toast.LENGTH_SHORT).show();
                }
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