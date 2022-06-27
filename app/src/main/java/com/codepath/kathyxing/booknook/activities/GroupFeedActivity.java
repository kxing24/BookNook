package com.codepath.kathyxing.booknook.activities;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.codepath.kathyxing.booknook.ParseQueryUtilities;
import com.codepath.kathyxing.booknook.adapters.PostsAdapter;
import com.codepath.kathyxing.booknook.R;
import com.codepath.kathyxing.booknook.models.Book;
import com.codepath.kathyxing.booknook.parse_classes.Group;
import com.codepath.kathyxing.booknook.parse_classes.Member;
import com.codepath.kathyxing.booknook.parse_classes.Post;
import com.parse.CountCallback;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseQuery;

import org.parceler.Parcels;

import java.util.ArrayList;
import java.util.List;

public class GroupFeedActivity extends AppCompatActivity {

    public static final String TAG = "GroupFeedActivity";
    protected PostsAdapter adapter;
    protected List<Post> allPosts;
    private Book book;
    private Group group;
    private RecyclerView rvPosts;
    private ProgressBar pbLoading;
    private TextView tvNoGroupPosts;
    private TextView tvNumMembers;
    private final int ADD_POST = 20;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_feed);
        // Extract book and group objects from intent extras
        book = (Book) Parcels.unwrap(getIntent().getParcelableExtra(Book.class.getSimpleName()));
        group = (Group) getIntent().getExtras().get("group");
        // set up the toolbar
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle(group.getGroupName());
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
        // initialize the views
        rvPosts = findViewById(R.id.rvPosts);
        pbLoading = findViewById(R.id.pbLoading);
        tvNoGroupPosts = findViewById(R.id.tvNoGroupPosts);
        tvNumMembers = findViewById(R.id.tvNumMembers);
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
        // query posts in group
        queryPosts();
    }

    private void getNumMembers() {
        ParseQuery<Member> queryMembers = ParseQuery.getQuery(Member.class);
        queryMembers.whereEqualTo(Member.KEY_BOOK_ID, book.getId());
        queryMembers.countInBackground(new CountCallback() {
            @Override
            public void done(int count, ParseException e) {
                tvNumMembers.setText(Integer.toString(count) + " members");
            }
        });
    }

    private void queryPosts() {
        FindCallback queryPostsCallback = new FindCallback<Post>() {
            @Override
            public void done(List<Post> posts, ParseException e) {
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
            }
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
            // send an email to invite a user to the group
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        // add the post to the top of the recyclerview
        if (requestCode == ADD_POST && resultCode == RESULT_OK) {
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
}