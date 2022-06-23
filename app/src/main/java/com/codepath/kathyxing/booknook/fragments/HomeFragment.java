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

        // initialize the recyclerview
        rvPosts = view.findViewById(R.id.rvPosts);

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

    private void queryPosts() {
        // TODO: post displays group

        // first query the groups that the user is in
        ParseQuery<Member> memberQuery = ParseQuery.getQuery(Member.class);
        // get data where the "from" (user) parameter matches the current user
        memberQuery.whereEqualTo(Member.KEY_FROM, ParseUser.getCurrentUser());
        // start an asynchronous call for groups
        memberQuery.findInBackground(new FindCallback<Member>() {
            @Override
            public void done(List<Member> memberList, ParseException e) {
                // check for errors
                if (e != null) {
                    Log.e(TAG, "Issue with getting groups", e);
                    return;
                }

                // query the posts from the groups
                // query the posts from each group then add them into the main query
                List<ParseQuery<Post>> queries = new ArrayList<ParseQuery<Post>>();
                for(Member member : memberList) {
                    Group group = member.getTo();
                    ParseQuery<Post> postQuery = ParseQuery.getQuery(Post.class);
                    postQuery.whereEqualTo(Post.KEY_GROUP, group);
                    queries.add(postQuery);
                }
                ParseQuery<Post> mainQuery = ParseQuery.or(queries);
                // show posts
                mainQuery.addDescendingOrder(Post.KEY_CREATED_AT);
                mainQuery.setLimit(20);
                mainQuery.include(Post.KEY_USER);
                mainQuery.include(Post.KEY_CREATED_AT);
                mainQuery.findInBackground(new FindCallback<Post>() {
                    @Override
                    public void done(List<Post> objects, ParseException e) {
                        // save received posts to list and notify adapter of new data
                        allPosts.addAll(objects);
                        adapter.notifyDataSetChanged();
                    }
                });
            }
        });
    }
}