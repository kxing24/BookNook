package com.codepath.kathyxing.booknook.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.view.ActionMode;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.selection.ItemDetailsLookup;
import androidx.recyclerview.selection.OnDragInitiatedListener;
import androidx.recyclerview.selection.OnItemActivatedListener;
import androidx.recyclerview.selection.SelectionTracker;
import androidx.recyclerview.selection.StorageStrategy;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.os.PersistableBundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.codepath.kathyxing.booknook.ActionModeController;
import com.codepath.kathyxing.booknook.ParseQueryUtilities;
import com.codepath.kathyxing.booknook.R;
import com.codepath.kathyxing.booknook.adapters.UserSelectionAdapter.UserSelectionAdapter;
import com.codepath.kathyxing.booknook.adapters.UserSelectionAdapter.UserSelectionKeyProvider;
import com.codepath.kathyxing.booknook.adapters.UserSelectionAdapter.UserSelectionLookup;
import com.codepath.kathyxing.booknook.models.Book;
import com.codepath.kathyxing.booknook.parse_classes.Group;
import com.codepath.kathyxing.booknook.parse_classes.User;
import com.parse.FindCallback;
import com.parse.FunctionCallback;
import com.parse.ParseCloud;
import com.parse.ParseException;
import com.parse.ParseUser;

import org.parceler.Parcels;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class SelectUsersActivity extends AppCompatActivity {

    public static final String TAG = "SelectUsersActivity";
    protected UserSelectionAdapter adapter;
    protected ArrayList<User> users;
    private RecyclerView rvSelectUsers;
    private ProgressBar pbLoading;
    private TextView tvNoUsers;
    private ActionMode actionMode;
    private Group group;
    SelectionTracker selectionTracker;
    MenuItem selectedItemCount;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_users);
        // extract the group from intent extras
        group = (Group) getIntent().getExtras().get("group");
        // set up the toolbar
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if(getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Invite users");
        }
        // have the toolbar show a back button
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        toolbar.setNavigationOnClickListener(v -> finish());
        // initialize the views
        rvSelectUsers = findViewById(R.id.rvSelectUsers);
        pbLoading = findViewById(R.id.pbLoading);
        tvNoUsers = findViewById(R.id.tvNoUsers);
        // initialize the array that will hold users and create a UserSelectionAdapter
        users = new ArrayList<>();
        adapter = new UserSelectionAdapter(this, users);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        // set the adapter on the recycler view
        rvSelectUsers.setAdapter(adapter);
        // set the layout manager on the recycler view
        rvSelectUsers.setLayoutManager(linearLayoutManager);
        // initialize the selection tracker
        selectionTracker = new SelectionTracker.Builder<>("my-selection-id", rvSelectUsers,
                new UserSelectionKeyProvider(1, users), new UserSelectionLookup(rvSelectUsers),
                StorageStrategy.createLongStorage())
                .withOnItemActivatedListener(new OnItemActivatedListener<Long>() {
                    @Override
                    public boolean onItemActivated(@NonNull ItemDetailsLookup.ItemDetails<Long> item, @NonNull MotionEvent e) {
                        Log.d(TAG, "Selected ItemId: " + item);
                        return true;
                    }
                })
                .withOnDragInitiatedListener(new OnDragInitiatedListener() {
                    @Override
                    public boolean onDragInitiated(@NonNull MotionEvent e) {
                        Log.d(TAG, "onDragInitiated");
                        return true;
                    }
                })
                .build();
        // set the selection tracker to the adapter
        adapter.setSelectionTracker(selectionTracker);
        selectionTracker.addObserver(new SelectionTracker.SelectionObserver() {
            @Override
            public void onItemStateChanged(@NonNull Object key, boolean selected) {
                super.onItemStateChanged(key, selected);
            }
            @Override
            public void onSelectionRefresh() {
                super.onSelectionRefresh();
            }
            @Override
            public void onSelectionChanged() {
                super.onSelectionChanged();
                if (selectionTracker.hasSelection() && actionMode == null) {
                    actionMode = startSupportActionMode(new ActionModeController(SelectUsersActivity.this, selectionTracker));
                    setMenuItemTitle(selectionTracker.getSelection().size());
                } else if (!selectionTracker.hasSelection() && actionMode != null) {
                    actionMode.finish();
                    actionMode = null;
                } else {
                    setMenuItemTitle(selectionTracker.getSelection().size());
                }
                Iterator<User> itemIterable = selectionTracker.getSelection().iterator();
                while (itemIterable.hasNext()) {
                    Log.i(TAG, itemIterable.next().getUsername());
                }
            }
            @Override
            public void onSelectionRestored() {
                super.onSelectionRestored();
            }
        });

        if (savedInstanceState != null) {
            selectionTracker.onRestoreInstanceState(savedInstanceState);
        }
        getFriendsNotInGroup();
    }

    @Override
    public void onSaveInstanceState(Bundle outState, PersistableBundle outPersistentState) {
        super.onSaveInstanceState(outState, outPersistentState);
        selectionTracker.onSaveInstanceState(outState);
    }

    public void setMenuItemTitle(int selectedItemSize) {
        selectedItemCount.setTitle("" + selectedItemSize);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_select_users, menu);
        selectedItemCount = menu.findItem(R.id.action_item_count);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_invite:
                if (selectionTracker.getSelection().isEmpty()) {
                    Toast.makeText(SelectUsersActivity.this,
                            "Choose at least one friend to invite!", Toast.LENGTH_SHORT).show();
                }
                else {
                    sendEmail();
                }
                break;
            case R.id.action_clear:
                selectionTracker.clearSelection();
                break;
        }
        return true;
    }

    private void getFriendsNotInGroup() {
        FindCallback<User> getFriendsNotInGroupCallback = (objects, e) -> {
            // check for errors
            if (e != null) {
                Log.e(TAG, "Issue with getting users", e);
                return;
            }
            // if there are no posts, set tvNoGroupPosts to visible
            if (objects.isEmpty()) {
                tvNoUsers.setVisibility(View.VISIBLE);
            }
            // save received posts to list and notify adapter of new data
            users.addAll(objects);
            adapter.notifyDataSetChanged();
            pbLoading.setVisibility(View.GONE);
        };
        ParseQueryUtilities.getFriendsNotInGroupAsync(group.getBookId(), getFriendsNotInGroupCallback);
    }

    private void sendEmail() {
        pbLoading.setVisibility(View.VISIBLE);
        Map<String, Object> params = new HashMap<>();
        // Create the fields "recipients", "emailSubject" and "emailBody"
        ArrayList<String> recipientIds = new ArrayList<>();
        for(int i = 0; i < users.size(); i++) {
            User user = users.get(i);
            if (selectionTracker.isSelected(user)) {
                Log.i(TAG, "adding user " + user.getUsername());
                recipientIds.add(user.getObjectId());
            }
        }
        adapter.clear();
        String emailSubject = "Invitation to " + group.getGroupName();
        String emailBody = "Hi,\n \n" + "You have been invited to join " + group.getGroupName() + " by " + ParseUser.getCurrentUser().getUsername() + ".\n" + "\n To join, use the following group ID:\n" + "\t" + group.getObjectId();
        // Add the fields to the requests
        params.put("recipientIds", recipientIds);
        params.put("subject", emailSubject);
        params.put("body", emailBody);
        ParseCloud.callFunctionInBackground("sendgridEmail", params, new FunctionCallback<Object>() {
            @Override
            public void done(Object response, ParseException e) {
                if(e == null) {
                    // The function executed, but still has to check the response
                    Toast.makeText(SelectUsersActivity.this, "Invitation sent!",
                            Toast.LENGTH_SHORT).show();
                    finish();
                }
                else {
                    // Something went wrong
                    Log.e(TAG, "issue with sending email", e);
                    Toast.makeText(SelectUsersActivity.this, "Issue sending invitation", Toast.LENGTH_SHORT).show();
                    getFriendsNotInGroup();
                }
                pbLoading.setVisibility(View.GONE);
            }
        });
    }
}