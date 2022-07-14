package com.codepath.kathyxing.booknook.fragments;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.codepath.kathyxing.booknook.EndlessRecyclerViewScrollListener;
import com.codepath.kathyxing.booknook.ParseQueryUtilities;
import com.codepath.kathyxing.booknook.R;
import com.codepath.kathyxing.booknook.adapters.PostsAdapter;
import com.codepath.kathyxing.booknook.parse_classes.Post;
import com.parse.FindCallback;

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
    private SwipeRefreshLayout swipeContainer;
    private EndlessRecyclerViewScrollListener scrollListener;

    // Required empty public constructor
    public HomeFragment() {
    }

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
        swipeContainer = view.findViewById(R.id.swipeContainer);
        // initialize the array that will hold posts and create a PostsAdapter
        allPosts = new ArrayList<>();
        adapter = new PostsAdapter(getContext(), allPosts);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext());
        // set the adapter on the recycler view
        rvPosts.setAdapter(adapter);
        // set the layout manager on the recycler view
        rvPosts.setLayoutManager(linearLayoutManager);
        // Setup refresh listener which triggers new data loading
        swipeContainer.setOnRefreshListener(this::fetchFeed);
        // Configure the refreshing colors
        swipeContainer.setColorSchemeResources(R.color.accent);
        // add endless scroll
        // Retain an instance so that you can call `resetState()` for fresh searches
        scrollListener = new EndlessRecyclerViewScrollListener(linearLayoutManager) {
            @Override
            public void onLoadMore(int page, int totalItemsCount, RecyclerView view) {
                // Triggered only when new data needs to be appended to the list
                loadNextDataFromApi(page);
            }
        };
        // Add the scroll listener to RecyclerView
        rvPosts.addOnScrollListener(scrollListener);
        // query posts in user groups
        queryPosts(0);
    }

    // get the posts from the user's groups and display them
    private void queryPosts(int page) {
        FindCallback<Post> queryPostsCallback = (objects, e) -> {
            if (e != null) {
                Log.e(TAG, "Query posts error", e);
                return;
            }
            if (objects.isEmpty() && page == 0) {
                tvNoPosts.setVisibility(View.VISIBLE);
            } else {
                allPosts.addAll(objects);
                adapter.notifyDataSetChanged();
            }
            pbLoading.setVisibility(View.GONE);
            // Call setRefreshing(false) to signal refresh has finished
            swipeContainer.setRefreshing(false);
        };
        ParseQueryUtilities.queryHomeFeedPostsAsync(page, queryPostsCallback);
    }

    // Send the request to fetch the updated data
    private void fetchFeed() {
        // clear out old items before appending in the new ones
        adapter.clear();
        // Reset endless scroll listener when performing a new search
        scrollListener.resetState();
        // add new items to adapter
        queryPosts(0);
    }

    // Append the next page of data into the adapter
    // This method probably sends out a network request and appends new data items to your adapter.
    public void loadNextDataFromApi(int offset) {
        queryPosts(offset);
    }
}