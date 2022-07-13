package com.codepath.kathyxing.booknook.adapters;

import android.content.Context;
import android.net.Uri;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.codepath.asynchttpclient.callback.JsonHttpResponseHandler;
import com.codepath.kathyxing.booknook.ParseQueryUtilities;
import com.codepath.kathyxing.booknook.R;
import com.codepath.kathyxing.booknook.models.Book;
import com.codepath.kathyxing.booknook.BookQueryManager;
import com.codepath.kathyxing.booknook.parse_classes.Group;
import com.parse.CountCallback;
import com.parse.ParseException;

import java.util.ArrayList;
import java.util.List;

import okhttp3.Headers;

public class GroupAdapter extends RecyclerView.Adapter<GroupAdapter.ViewHolder> {

    // adapter parameters
    public static final String TAG = "GroupAdapter";
    private final List<Group> groups;
    private final Context context;
    private Book book;

    // Define listener member variable
    private GroupAdapter.OnItemClickListener listener;

    // Define the listener interface
    public interface OnItemClickListener {
        void onItemClick(View itemView, int position);
    }

    // Define the method that allows the parent activity or fragment to define the listener
    public void setOnItemClickListener(GroupAdapter.OnItemClickListener listener) {
        this.listener = listener;
    }

    // View lookup cache
    public static class ViewHolder extends RecyclerView.ViewHolder {
        public ImageView ivCover;
        public TextView tvGroupTitle;
        public TextView tvNumMembers;
        public TextView tvNumPosts;

        public ViewHolder(final View itemView, final GroupAdapter.OnItemClickListener clickListener) {
            // Stores the itemView in a public final member variable that can be used
            // to access the context from any ViewHolder instance.
            super(itemView);
            ivCover = itemView.findViewById(R.id.ivBookCover);
            tvGroupTitle = itemView.findViewById(R.id.tvGroupTitle);
            tvNumMembers = itemView.findViewById(R.id.tvNumMembers);
            tvNumPosts = itemView.findViewById(R.id.tvNumPosts);
            // set up click handler for the item
            itemView.setOnClickListener(v -> clickListener.onItemClick(itemView, getAdapterPosition()));
        }
    }

    public GroupAdapter(Context context, ArrayList<Group> groups) {
        this.groups = groups;
        this.context = context;
    }

    // Inflating layout from XML and return the holder
    @NonNull
    @Override
    public GroupAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        Context context = parent.getContext();
        LayoutInflater inflater = LayoutInflater.from(context);

        // Inflate the custom layout
        View groupView = inflater.inflate(R.layout.item_group, parent, false);

        // Return a new holder instance
        return new GroupAdapter.ViewHolder(groupView, listener);
    }

    // Involves populating data into the item through holder
    @Override
    public void onBindViewHolder(@NonNull GroupAdapter.ViewHolder holder, int position) {
        // Get the data model based on position
        Log.i(TAG, "position is " + position);
        Group group = groups.get(position);
        // Populate data into the views
        try {
            holder.tvGroupTitle.setText(group.fetchIfNeeded().getString(Group.KEY_GROUP_NAME));
        } catch (ParseException e) {
            e.printStackTrace();
        }
        // Query the group's number of members to set tvNumMembers
        CountCallback getNumMembersInGroupCallback = (count, e) -> {
            if(e == null) {
                holder.tvNumMembers.setText(count + " members");
            } else {
                Log.e(TAG, "error getting num members", e);
            }
        };
        ParseQueryUtilities.getNumMembersInGroupAsync(group, getNumMembersInGroupCallback);
        // Query the groups' number of posts to set tvNumPosts
        CountCallback getNumPostsInGroupCallback = (count, e) -> {
            if (e == null) {
                holder.tvNumPosts.setText(count + " posts");
            } else {
                Log.e(TAG, "error getting num posts", e);
            }
        };
        ParseQueryUtilities.getNumPostsInGroupAsync(group, getNumPostsInGroupCallback);
        // Get the group's book using an API call to set the book cover
        try {
            BookQueryManager.getInstance().getBook(group.fetchIfNeeded().getString(Group.KEY_BOOK_ID), new JsonHttpResponseHandler() {
                @Override
                public void onSuccess(int statusCode, Headers headers, JSON json) {
                    book = Book.fromJson(json.jsonObject);
                    Glide.with(getContext())
                            .load(Uri.parse(book.getCoverUrl()))
                            .apply(new RequestOptions()
                                    .placeholder(R.drawable.ic_nocover))
                            .into(holder.ivCover);
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

    @Override
    public int getItemCount() {
        return groups.size();
    }

    // Easy access to the context object in the recyclerview
    private Context getContext() {
        return context;
    }
}
