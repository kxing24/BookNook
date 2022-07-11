package com.codepath.kathyxing.booknook.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.SearchView;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.codepath.kathyxing.booknook.ParseQueryUtilities;
import com.codepath.kathyxing.booknook.R;
import com.codepath.kathyxing.booknook.activities.UserProfileActivity;
import com.codepath.kathyxing.booknook.adapters.UserAdapter;
import com.codepath.kathyxing.booknook.parse_classes.User;
import com.parse.FindCallback;

import java.util.ArrayList;

/**
 * A simple {@link Fragment} subclass.
 */
public class FindUserFragment extends Fragment {

    // fragment parameters
    public static final String TAG = "FindUserFragment";
    private RecyclerView rvUsers;
    private ProgressBar pbLoading;
    private TextView tvNoResults;
    private SearchView svSearch;
    private ArrayList<User> users;
    private UserAdapter userAdapter;

    public FindUserFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_find_user, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        // initialize views
        rvUsers = view.findViewById(R.id.rvUsers);
        pbLoading = view.findViewById(R.id.pbLoading);
        tvNoResults = view.findViewById(R.id.tvNoResults);
        svSearch = view.findViewById(R.id.svSearch);
        users = new ArrayList<>();
        userAdapter = new UserAdapter(getContext(), users);
        // Attach the adapter to the RecyclerView
        rvUsers.setAdapter(userAdapter);
        // Set layout manager to position the items
        rvUsers.setLayoutManager(new LinearLayoutManager(getContext()));
        // set up a click handler for bookAdapter
        userAdapter.setOnItemClickListener((itemView, position) -> {
            // get the user clicked
            User user = users.get(position);
            Intent intent = new Intent(getContext(), UserProfileActivity.class);
            intent.putExtra("user", user);
            if (getContext() != null) {
                getContext().startActivity(intent);
            }
        });
        // Set the textlistener for the searchview
        svSearch.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                // perform query here
                queryUsers(query);
                // workaround to avoid issues with some emulators and keyboard devices firing twice if a keyboard enter is used
                svSearch.clearFocus();
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                tvNoResults.setVisibility(View.GONE);
                return false;
            }
        });
    }

    private void queryUsers(String query) {
        pbLoading.setVisibility(View.VISIBLE);
        // smooth scroll to top
        rvUsers.smoothScrollToPosition(0);
        // Remove users from the adapter
        userAdapter.clear();
        FindCallback<User> queryUsersCallback = (objects, e) -> {
            // check for errors
            if (e != null) {
                Log.e(TAG, "Issue with getting users", e);
                return;
            }
            if (objects.isEmpty()) {
                tvNoResults.setVisibility(View.VISIBLE);
            } else {
                // save received users to list and notify adapter of new data
                users.addAll(objects);
                userAdapter.notifyDataSetChanged();
            }
            pbLoading.setVisibility(View.GONE);
        };
        ParseQueryUtilities.queryUsersAsync(query.trim(), queryUsersCallback);
    }
}