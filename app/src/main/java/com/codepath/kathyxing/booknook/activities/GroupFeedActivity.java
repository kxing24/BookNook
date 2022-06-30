package com.codepath.kathyxing.booknook.activities;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.codepath.kathyxing.booknook.ParseQueryUtilities;
import com.codepath.kathyxing.booknook.R;
import com.codepath.kathyxing.booknook.adapters.PostsAdapter;
import com.codepath.kathyxing.booknook.models.Book;
import com.codepath.kathyxing.booknook.parse_classes.Group;
import com.codepath.kathyxing.booknook.parse_classes.Member;
import com.codepath.kathyxing.booknook.parse_classes.Post;
import com.codepath.kathyxing.booknook.parse_classes.User;
import com.parse.CountCallback;
import com.parse.DeleteCallback;
import com.parse.FindCallback;
import com.parse.GetCallback;
import com.parse.ParseUser;

import org.parceler.Parcels;

import java.util.ArrayList;
import java.util.List;

public class GroupFeedActivity extends AppCompatActivity {

    public static final String TAG = "GroupFeedActivity";
    private final int ADD_POST = 20;
    protected PostsAdapter adapter;
    protected List<Post> allPosts;
    private Book book;
    private Group group;
    private RecyclerView rvPosts;
    private ProgressBar pbLoading;
    private TextView tvNoGroupPosts;
    private TextView tvNumMembers;
    private TextView tvNumPosts;
    private int position;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_feed);
        // Extract data from intent extras
        book = Parcels.unwrap(getIntent().getParcelableExtra(Book.class.getSimpleName()));
        group = (Group) getIntent().getExtras().get("group");
        position = getIntent().getExtras().getInt("position");
        // set up the toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(group.getGroupName());
        }
        // have the toolbar show a back button
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        toolbar.setNavigationOnClickListener(v -> {
            Intent intent = new Intent();
            intent.putExtra("leftGroup", false);
            intent.putExtra("position", position);
            setResult(RESULT_OK, intent);
            finish();
        });
        // initialize the views
        rvPosts = findViewById(R.id.rvPosts);
        pbLoading = findViewById(R.id.pbLoading);
        tvNoGroupPosts = findViewById(R.id.tvNoGroupPosts);
        tvNumMembers = findViewById(R.id.tvNumMembers);
        tvNumPosts = findViewById(R.id.tvNumPosts);
        // initialize the array that will hold posts and create a PostsAdapter
        allPosts = new ArrayList<>();
        adapter = new PostsAdapter(this, allPosts);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        // set the adapter on the recycler view
        rvPosts.setAdapter(adapter);
        // set the layout manager on the recycler view
        rvPosts.setLayoutManager(linearLayoutManager);
        // get the number of members in the group
        getNumMembers();
        // get the number of posts in the group
        getNumPosts();
        // query posts in group
        queryPosts();
    }

    private void getNumMembers() {
        CountCallback getNumMembersInGroupCallback = (count, e) -> {
            if (e == null) {
                tvNumMembers.setText(count + " members");
            } else {
                Log.e(TAG, "error getting num members", e);
            }
        };
        ParseQueryUtilities.getNumMembersInGroupAsync(group, getNumMembersInGroupCallback);
    }

    private void getNumPosts() {
        CountCallback getNumPostsInGroupCallback = (count, e) -> {
            if (e == null) {
                tvNumPosts.setText(count + " posts");
            } else {
                Log.e(TAG, "error getting num posts", e);
            }
        };
        ParseQueryUtilities.getNumPostsInGroupAsync(group, getNumPostsInGroupCallback);
    }

    private void queryPosts() {
        FindCallback queryPostsCallback = (FindCallback<Post>) (posts, e) -> {
            // check for errors
            if (e != null) {
                Log.e(TAG, "Issue with getting posts", e);
                return;
            }
            // if there are no posts, set tvNoGroupPosts to visible
            if (posts.isEmpty()) {
                tvNoGroupPosts.setVisibility(View.VISIBLE);
            }
            // save received posts to list and notify adapter of new data
            allPosts.addAll(posts);
            adapter.notifyDataSetChanged();
            pbLoading.setVisibility(View.GONE);
        };
        ParseQueryUtilities.queryGroupPostsAsync(book.getId(), queryPostsCallback);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present
        getMenuInflater().inflate(R.menu.menu_group_feed, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.compose) {
            // Compose icon has been selected
            // Navigate to the compose activity
            Intent intent = new Intent(this, ComposeActivity.class);
            intent.putExtra("group", group);
            // serialize the book using parceler, use its short name as a key
            intent.putExtra(Book.class.getSimpleName(), Parcels.wrap(book));
            startActivityForResult(intent, ADD_POST);
            return true;
        }
        if (item.getItemId() == R.id.inviteUser) {
            // Navigate to the select users activity
            Intent intent = new Intent(this, SelectUsersActivity.class);
            intent.putExtra("group", group);
            startActivity(intent);
            return true;
        }
        if (item.getItemId() == R.id.leaveGroup) {
            leaveGroup();
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        // add the post to the top of the recyclerview
        if (data != null && requestCode == ADD_POST && resultCode == RESULT_OK) {
            // Get data from the intent (post)
            Post post = (Post) data.getExtras().get("post");
            // Update the RV with the tweet
            // Modify data source of tweets
            allPosts.add(0, post);
            // Update the adapter
            adapter.notifyItemInserted(0);
            rvPosts.smoothScrollToPosition(0);
            tvNoGroupPosts.setVisibility(View.GONE);
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    private void leaveGroup() {
        DeleteCallback leaveGroupCallback = e -> {
            if (e == null) {
                Toast.makeText(GroupFeedActivity.this, "Left group!", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent();
                intent.putExtra("leftGroup", true);
                setResult(RESULT_OK, intent);
                intent.putExtra("position", position);
                finish();
            } else {
                Log.e(TAG, "issue leaving group", e);
            }
        };
        GetCallback<Member> getMemberCallback = (member, e) -> {
            if (e == null) {
                ParseQueryUtilities.leaveGroupAsync(member, leaveGroupCallback);
                //TODO: pass back a boolean and adjust view visibility in previous ones
            } else {
                Log.e(TAG, "issue getting member", e);
            }
        };
        ParseQueryUtilities.getMemberAsync(group, (User) ParseUser.getCurrentUser(), getMemberCallback);
    }
}