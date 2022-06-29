package com.codepath.kathyxing.booknook.fragments;

import android.content.Intent;
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
import android.widget.Toast;

import com.codepath.kathyxing.booknook.ParseQueryUtilities;
import com.codepath.kathyxing.booknook.R;
import com.codepath.kathyxing.booknook.activities.UserProfileActivity;
import com.codepath.kathyxing.booknook.adapters.UserAdapter;
import com.codepath.kathyxing.booknook.parse_classes.User;
import com.parse.FindCallback;
import com.parse.ParseException;

import java.util.ArrayList;
import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 */
public class IncomingFriendRequestsFragment extends Fragment {

    // fragment parameter
    public static final String TAG = "IncomingRequestFragment";
    private RecyclerView rvFriendRequests;
    private TextView tvNoFriendRequests;
    private ProgressBar pbLoading;
    private UserAdapter userAdapter;
    private ArrayList<User> requestingFriends;

    // Required empty public constructor
    public IncomingFriendRequestsFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_incoming_friend_requests, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        // initialize the fields
        rvFriendRequests = view.findViewById(R.id.rvFriendRequests);
        tvNoFriendRequests = view.findViewById(R.id.tvNoFriendRequests);
        pbLoading = view.findViewById(R.id.pbLoading);
        requestingFriends = new ArrayList<User>();
        userAdapter = new UserAdapter(getContext(), requestingFriends);
        // set up a click handler for the adapter
        userAdapter.setOnItemClickListener(new UserAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(View itemView, int position) {
                // get the group clicked
                User user = requestingFriends.get(position);
                Intent intent = new Intent(getContext(), UserProfileActivity.class);
                intent.putExtra("user", user);
                getContext().startActivity(intent);
            }
        });
        // Attach the adapter to the RecyclerView
        rvFriendRequests.setAdapter(userAdapter);
        // Set layout manager to position the items
        rvFriendRequests.setLayoutManager(new LinearLayoutManager(getContext()));
        queryRequestingFriends();
    }

    // get the requesting friends and add them to the requestingFriends list
    private void queryRequestingFriends() {
        FindCallback<User> getRequestingFriendsCallback = (objects, e) -> {
            // check for errors
            if (e != null) {
                Log.e(TAG, "Issue with getting friends", e);
                return;
            }
            if (objects.isEmpty()) {
                tvNoFriendRequests.setVisibility(View.VISIBLE);
            } else {
                // save received users to list and notify adapter of new data
                requestingFriends.addAll(objects);
                userAdapter.notifyDataSetChanged();
            }
            pbLoading.setVisibility(View.GONE);
        };
        ParseQueryUtilities.getRequestingFriendsAsync(getRequestingFriendsCallback);
    }
}