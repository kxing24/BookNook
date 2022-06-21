package com.codepath.kathyxing.booknook.activities;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import com.codepath.kathyxing.booknook.R;
import com.codepath.kathyxing.booknook.models.Book;
import com.codepath.kathyxing.booknook.parse_classes.Group;
import com.codepath.kathyxing.booknook.parse_classes.Post;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseQuery;
import com.parse.ParseUser;

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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group);

        // Extract book object from intent extras
        book = (Book) Parcels.unwrap(getIntent().getParcelableExtra(Book.class.getSimpleName()));

        // Set the group
        setGroup(book);

        // set up the toolbar
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle(book.getTitle() + " Group");

        // have the toolbar show a back button
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                goBookDetailActivity();
            }
        });

        rvPosts = findViewById(R.id.rvPosts);

        // initialize the array that will hold posts and create a PostsAdapter
        allPosts = new ArrayList<>();
        adapter = new PostsAdapter(this, allPosts);

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);

        // set the adapter on the recycler view
        rvPosts.setAdapter(adapter);
        // set the layout manager on the recycler view
        rvPosts.setLayoutManager(linearLayoutManager);

        // query posts in group
        queryPosts();
    }

    private void queryPosts() {
        // specify what type of data we want to query - Post.class
        ParseQuery<Post> query = ParseQuery.getQuery(Post.class);
        // include data referred by user key
        query.include(Post.KEY_USER);
        // get the posts in the group
        query.whereEqualTo("to", group);
        // limit query to latest 20 items
        query.setLimit(20);
        // order posts by creation date (newest first)
        query.addDescendingOrder("createdAt");
        // start an asynchronous call for posts
        query.findInBackground(new FindCallback<Post>() {
            @Override
            public void done(List<Post> posts, ParseException e) {
                // check for errors
                if (e != null) {
                    Log.e(TAG, "Issue with getting posts", e);
                    return;
                }

                // save received posts to list and notify adapter of new data
                allPosts.addAll(posts);
                adapter.notifyDataSetChanged();
            }
        });
    }

    private void setGroup(Book book) {
        // create a query to get the group from the book
        ParseQuery<Group> query = ParseQuery.getQuery(Group.class);
        // get results with the book id
        query.whereEqualTo("bookId", book.getId());
        query.findInBackground(new FindCallback<Group>() {
            @Override
            public void done(List<Group> objects, ParseException e) {
                // query yields an empty list
                // group does not exist
                if (objects.isEmpty()) {
                    Log.e(TAG, "Group does not exist!");
                }
                // query is nonempty
                // group exists: get the group and check if user is in group
                else {
                    group = objects.get(0);
                }
            }
        });
    }

    private void goBookDetailActivity() {
        Intent selectedItemIntent = new Intent(getBaseContext(), BookDetailActivity.class);
        selectedItemIntent.putExtra(Book.class.getSimpleName(), Parcels.wrap(book));
        startActivity(selectedItemIntent);
        finish();
    }
}