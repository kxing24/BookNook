package com.codepath.kathyxing.booknook.fragments;

import android.app.Activity;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.codepath.kathyxing.booknook.R;
import com.codepath.kathyxing.booknook.adapters.PostsAdapter;
import com.codepath.kathyxing.booknook.parse_classes.Group;
import com.codepath.kathyxing.booknook.parse_classes.Member;
import com.codepath.kathyxing.booknook.parse_classes.Post;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import java.util.ArrayList;
import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 */
public class HomeFragment extends Fragment {

    // the fragment parameters
    public static final String TAG = "HomeFragment";
    protected PostsAdapter adapter;
    protected List<Post> allPosts;
    private RecyclerView rvPosts;
    private ProgressBar pbLoading;
    private TextView tvNoPosts;

    // Required empty public constructor
    public HomeFragment() {}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_home, container, false);
    }

    @Override
    public void onResume() {
        super.onResume();
        // Set the toolbar text
        Activity activity = getActivity();
        if (activity != null) {
            activity.setTitle(getString(R.string.home));
        }
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        // initialize views
        rvPosts = view.findViewById(R.id.rvPosts);
        pbLoading = view.findViewById(R.id.pbLoading);
        tvNoPosts = view.findViewById(R.id.tvNoPosts);
        // initialize the array that will hold posts and create a PostsAdapter
        allPosts = new ArrayList<>();
        adapter = new PostsAdapter(getContext(), allPosts);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext());
        // set the adapter on the recycler view
        rvPosts.setAdapter(adapter);
        // set the layout manager on the recycler view
        rvPosts.setLayoutManager(linearLayoutManager);
        // query posts in user groups
        queryPosts();
    }

    // get the posts from the user's groups and display them
    private void queryPosts() {
        // first query the groups from the user
        ParseQuery<Member> memberQuery = ParseQuery.getQuery(Member.class);
        memberQuery.whereEqualTo(Member.KEY_FROM, ParseUser.getCurrentUser());
        // query the groups from the posts
        ParseQuery<Post> postQuery = ParseQuery.getQuery(Post.class);
        postQuery.include(Post.KEY_CREATED_AT);
        postQuery.include(Post.KEY_USER);
        // find the results where the groups match across queries
        // this yield the posts of the groups that the user is in
        postQuery.whereMatchesKeyInQuery(Post.KEY_GROUP, Member.KEY_TO, memberQuery);
        // sort the posts in descending order by creation date
        postQuery.addDescendingOrder(Post.KEY_CREATED_AT);
        postQuery.findInBackground(new FindCallback<Post>() {
            @Override
            public void done(List<Post> objects, ParseException e) {
                if (e != null) {
                    Log.e(TAG, "Query posts error", e);
                    return;
                }
                if (objects.isEmpty()) {
                    tvNoPosts.setVisibility(View.VISIBLE);
                } else {
                    allPosts.addAll(objects);
                    adapter.notifyDataSetChanged();
                }
                pbLoading.setVisibility(View.GONE);
            }
        });
    }
}