package com.codepath.kathyxing.booknook.fragments;

import static android.app.Activity.RESULT_OK;

import android.app.Activity;
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
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.codepath.asynchttpclient.callback.JsonHttpResponseHandler;
import com.codepath.kathyxing.booknook.ParseQueryUtilities;
import com.codepath.kathyxing.booknook.R;
import com.codepath.kathyxing.booknook.activities.GroupFeedActivity;
import com.codepath.kathyxing.booknook.activities.JoinGroupActivity;
import com.codepath.kathyxing.booknook.adapters.GroupAdapter;
import com.codepath.kathyxing.booknook.models.Book;
import com.codepath.kathyxing.booknook.BookQueryManager;
import com.codepath.kathyxing.booknook.parse_classes.Group;
import com.codepath.kathyxing.booknook.parse_classes.Member;
import com.google.android.material.button.MaterialButton;
import com.parse.FindCallback;
import com.parse.ParseException;

import org.parceler.Parcels;

import java.util.ArrayList;

import okhttp3.Headers;

/**
 * A simple {@link Fragment} subclass.
 */
public class MyGroupsFragment extends Fragment {

    // the fragment parameters
    public static final String TAG = "MyGroupsFragment";
    private static final int LEFT_GROUP = 10;
    private static final int ADD_GROUP = 30;
    private RecyclerView rvMyGroups;
    private GroupAdapter groupAdapter;
    private ArrayList<Group> myGroups;
    private ProgressBar pbLoading;
    private TextView tvNoGroups;

    // Required empty public constructor
    public MyGroupsFragment() {
    }

    @Override
    public void onResume() {
        super.onResume();
        // Set the toolbar text
        Activity activity = getActivity();
        if (activity != null) {
            activity.setTitle(getString(R.string.my_groups));
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_my_groups, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        // initialize fields
        rvMyGroups = view.findViewById(R.id.rvMyGroups);
        myGroups = new ArrayList<>();
        groupAdapter = new GroupAdapter(getContext(), myGroups);
        pbLoading = view.findViewById(R.id.pbLoading);
        tvNoGroups = view.findViewById(R.id.tvNoGroups);
        MaterialButton btnJoinGroup = view.findViewById(R.id.btnJoinGroup);
        // set up a click handler for rlJoinGroup
        btnJoinGroup.setOnClickListener(v -> goJoinGroupActivity());
        // set up a click handler for groupAdapter
        groupAdapter.setOnItemClickListener((itemView, position) -> {
            // get the group clicked
            Group group = myGroups.get(position);
            // Get the group's book using an API call
            // After getting the book, go to the group's feed
            try {
                BookQueryManager.getInstance().getBook(group.fetchIfNeeded().getString(Group.KEY_BOOK_ID), new JsonHttpResponseHandler() {
                    @Override
                    public void onSuccess(int statusCode, Headers headers, JSON json) {
                        Book book = Book.fromJson(json.jsonObject);
                        // go to the group's feed
                        Intent i = new Intent(getContext(), GroupFeedActivity.class);
                        i.putExtra(Book.class.getSimpleName(), Parcels.wrap(book));
                        i.putExtra("group", group);
                        i.putExtra("position", position);
                        startActivityForResult(i, LEFT_GROUP);
                    }
                    @Override
                    public void onFailure(int statusCode, Headers headers, String response, Throwable throwable) {
                        // Handle failed request here
                        Log.e(TAG, "Request failed with code " + statusCode + ". Response message: " + response);
                    }
                });
            } catch (ParseException e) {
                e.printStackTrace();
            }
        });
        // Attach the adapter to the RecyclerView
        rvMyGroups.setAdapter(groupAdapter);
        // Set layout manager to position the items
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
        rvMyGroups.setLayoutManager(layoutManager);
        // add divider between items
        DividerItemDecoration itemDecor = new DividerItemDecoration(requireContext(), layoutManager.getOrientation());
        rvMyGroups.addItemDecoration(itemDecor);
        queryGroups();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (resultCode == RESULT_OK && data != null) {
            if (requestCode == ADD_GROUP) {
                // Get data from the intent (group)
                Group group = (Group) data.getExtras().get("group");
                // Update the RV with the group
                // Modify data source of groups
                myGroups.add(0, group);
                // Update the adapter
                groupAdapter.notifyItemInserted(0);
                rvMyGroups.smoothScrollToPosition(0);
                tvNoGroups.setVisibility(View.GONE);
            }
            if (requestCode == LEFT_GROUP) {
                // Get the data from the intent
                boolean leftGroup = data.getExtras().getBoolean("leftGroup");
                if (leftGroup) {
                    // If the user left a group, remove it from the data
                    int position = data.getExtras().getInt("position");
                    myGroups.remove(position);
                    // update the adapter
                    groupAdapter.notifyItemRemoved(position);
                    // if the user is no longer in any group, set tvNoGroups to visible
                    if (myGroups.isEmpty()) {
                        tvNoGroups.setVisibility(View.VISIBLE);
                    }
                }
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    // get the groups and add them to the myGroups list
    private void queryGroups() {
        FindCallback<Member> queryGroupsCallback = (memberList, e) -> {
            // check for errors
            if (e != null) {
                Log.e(TAG, "Issue with getting groups", e);
                return;
            }
            if (memberList.isEmpty()) {
                tvNoGroups.setVisibility(View.VISIBLE);
            } else {
                // save received groups to list and notify adapter of new data
                for (Member member : memberList) {
                    myGroups.add(member.getTo());
                }
                groupAdapter.notifyDataSetChanged();
            }
            pbLoading.setVisibility(View.GONE);
        };
        ParseQueryUtilities.queryGroupsAsync(queryGroupsCallback);
    }

    private void goJoinGroupActivity() {
        Intent i = new Intent(getContext(), JoinGroupActivity.class);
        startActivityForResult(i, ADD_GROUP);
    }

}