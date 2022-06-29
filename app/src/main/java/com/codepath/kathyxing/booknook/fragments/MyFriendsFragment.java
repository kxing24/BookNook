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

import com.codepath.asynchttpclient.callback.JsonHttpResponseHandler;
import com.codepath.kathyxing.booknook.ParseQueryUtilities;
import com.codepath.kathyxing.booknook.R;
import com.codepath.kathyxing.booknook.activities.GroupFeedActivity;
import com.codepath.kathyxing.booknook.activities.UserProfileActivity;
import com.codepath.kathyxing.booknook.adapters.GroupAdapter;
import com.codepath.kathyxing.booknook.adapters.UserAdapter;
import com.codepath.kathyxing.booknook.models.Book;
import com.codepath.kathyxing.booknook.net.BookClient;
import com.codepath.kathyxing.booknook.parse_classes.Friend;
import com.codepath.kathyxing.booknook.parse_classes.Group;
import com.codepath.kathyxing.booknook.parse_classes.Member;
import com.codepath.kathyxing.booknook.parse_classes.User;
import com.parse.FindCallback;
import com.parse.ParseException;

import org.parceler.Parcels;

import java.util.ArrayList;
import java.util.List;

import okhttp3.Headers;

/**
 * A simple {@link Fragment} subclass.
 */
public class MyFriendsFragment extends Fragment {

    // fragment parameters
    public static final String TAG = "MyFriendsFragment";
    private RecyclerView rvMyFriends;
    private TextView tvNoFriends;
    private ProgressBar pbLoading;
    private UserAdapter userAdapter;
    private ArrayList<User> myFriends;

    // Required empty public constructor
    public MyFriendsFragment() {}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_my_friends, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        // initialize the fields
        rvMyFriends = view.findViewById(R.id.rvMyFriends);
        tvNoFriends = view.findViewById(R.id.tvNoFriends);
        pbLoading = view.findViewById(R.id.pbLoading);
        myFriends = new ArrayList<User>();
        userAdapter = new UserAdapter(getContext(), myFriends);
        // set up a click handler for bookAdapter
        userAdapter.setOnItemClickListener(new UserAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(View itemView, int position) {
                // get the user clicked
                User user = myFriends.get(position);
                Intent intent = new Intent(getContext(), UserProfileActivity.class);
                intent.putExtra("user", user);
                getContext().startActivity(intent);
            }
        });
        // Attach the adapter to the RecyclerView
        rvMyFriends.setAdapter(userAdapter);
        // Set layout manager to position the items
        rvMyFriends.setLayoutManager(new LinearLayoutManager(getContext()));
        queryFriends();
    }

    // get the friends and add them to the myFriends list
    private void queryFriends() {
        FindCallback queryFriendsCallback = new FindCallback<User>() {
            @Override
            public void done(List<User> users, ParseException e) {
                // check for errors
                if (e != null) {
                    Log.e(TAG, "Issue with getting friends", e);
                    return;
                }
                if (users.isEmpty()) {
                    tvNoFriends.setVisibility(View.VISIBLE);
                } else {
                    // save received users to list and notify adapter of new data
                    myFriends.addAll(users);
                    userAdapter.notifyDataSetChanged();
                }
                pbLoading.setVisibility(View.GONE);
            }
        };
        ParseQueryUtilities.queryFriendsAsync(queryFriendsCallback);
    }
}