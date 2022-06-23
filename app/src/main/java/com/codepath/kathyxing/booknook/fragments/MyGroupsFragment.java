package com.codepath.kathyxing.booknook.fragments;

import android.content.Intent;
import android.net.Uri;
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
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.codepath.asynchttpclient.callback.JsonHttpResponseHandler;
import com.codepath.kathyxing.booknook.BookAdapter;
import com.codepath.kathyxing.booknook.GroupAdapter;
import com.codepath.kathyxing.booknook.R;
import com.codepath.kathyxing.booknook.activities.BookDetailActivity;
import com.codepath.kathyxing.booknook.activities.GroupFeedActivity;
import com.codepath.kathyxing.booknook.models.Book;
import com.codepath.kathyxing.booknook.net.BookClient;
import com.codepath.kathyxing.booknook.parse_classes.Group;
import com.codepath.kathyxing.booknook.parse_classes.Member;
import com.codepath.kathyxing.booknook.parse_classes.Post;
import com.codepath.kathyxing.booknook.parse_classes.User;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import org.parceler.Parcels;

import java.util.ArrayList;
import java.util.List;

import okhttp3.Headers;

/**
 * A simple {@link Fragment} subclass.
 */
public class MyGroupsFragment extends Fragment {

    // the fragment parameters
    public static final String TAG = "MyGroupsFragment";
    private RecyclerView rvMyGroups;
    private GroupAdapter groupAdapter;
    private ArrayList<Group> myGroups;

    // Required empty public constructor
    public MyGroupsFragment() {}

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

        // set up a click handler for bookAdapter
        groupAdapter.setOnItemClickListener(new GroupAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(View itemView, int position) {
                Toast.makeText(getContext(), "Going to group!", Toast.LENGTH_SHORT).show();

                // get the group clicked
                Group group = myGroups.get(position);

                // Get the group's book using an API call
                // After getting the book, go to the group's feed
                BookClient client = new BookClient();
                try {
                    client.getBook(group.fetchIfNeeded().getString(Group.KEY_BOOK_ID), new JsonHttpResponseHandler() {
                        @Override
                        public void onSuccess(int statusCode, Headers headers, JSON json) {
                            Book book = Book.fromJson(json.jsonObject);

                            // go to the group's feed
                            Intent i = new Intent(getContext(), GroupFeedActivity.class);
                            i.putExtra(Book.class.getSimpleName(), Parcels.wrap(book));
                            i.putExtra("group", group);
                            startActivity(i);
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
            }
        });

        // Attach the adapter to the RecyclerView
        rvMyGroups.setAdapter(groupAdapter);

        // Set layout manager to position the items
        rvMyGroups.setLayoutManager(new LinearLayoutManager(getContext()));

        queryGroups();

    }

    // get the groups and add them to the myGroups list
    private void queryGroups() {
        // specify what type of data we want to query - Groups.class
        ParseQuery<Member> query = ParseQuery.getQuery(Member.class);
        // get data where the "from" (user) parameter matches the current user
        query.whereEqualTo("from", ParseUser.getCurrentUser());
        // limit query to latest 20 items
        query.setLimit(20);
        // start an asynchronous call for groups
        query.findInBackground(new FindCallback<Member>() {
            @Override
            public void done(List<Member> memberList, ParseException e) {
                // check for errors
                if (e != null) {
                    Log.e(TAG, "Issue with getting groups", e);
                    return;
                }

                // save received groups to list and notify adapter of new data
                for(Member member : memberList) {
                    myGroups.add(member.getTo());
                }
                groupAdapter.notifyDataSetChanged();
            }
        });
    }

}