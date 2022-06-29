package com.codepath.kathyxing.booknook.adapters;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.codepath.asynchttpclient.callback.JsonHttpResponseHandler;
import com.codepath.kathyxing.booknook.R;
import com.codepath.kathyxing.booknook.activities.UserProfileActivity;
import com.codepath.kathyxing.booknook.models.Book;
import com.codepath.kathyxing.booknook.net.BookClient;
import com.codepath.kathyxing.booknook.parse_classes.Group;
import com.codepath.kathyxing.booknook.parse_classes.Post;
import com.codepath.kathyxing.booknook.parse_classes.User;
import com.parse.ParseException;
import com.parse.ParseFile;

import java.util.ArrayList;
import java.util.List;

import okhttp3.Headers;

public class UserAdapter extends RecyclerView.Adapter<UserAdapter.ViewHolder> {
    private List<User> users;
    private Context context;

    // Define listener member variable
    private UserAdapter.OnItemClickListener listener;

    // Define the listener interface
    public interface OnItemClickListener {
        void onItemClick(View itemView, int position);
    }

    // Define the method that allows the parent activity or fragment to define the listener
    public void setOnItemClickListener(UserAdapter.OnItemClickListener listener) {
        this.listener = listener;
    }

    // View lookup cache
    public class ViewHolder extends RecyclerView.ViewHolder {
        public ImageView ivProfilePicture;
        public TextView tvUsername;
        public TextView tvEmail;

        public ViewHolder(final View itemView, final UserAdapter.OnItemClickListener clickListener) {
            // Stores the itemView in a public final member variable that can be used
            // to access the context from any ViewHolder instance.
            super(itemView);
            // initialize views
            ivProfilePicture = itemView.findViewById(R.id.ivProfilePicture);
            tvUsername = itemView.findViewById(R.id.tvUsername);
            tvEmail = itemView.findViewById(R.id.tvEmail);
            // set click handler for the itemview
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    clickListener.onItemClick(itemView, getAdapterPosition());
                }
            });
        }
    }

    public UserAdapter(Context context, ArrayList<User> users) {
        this.users = users;
        this.context = context;
    }

    // Inflating layout from XML and return the holder
    @NonNull
    @Override
    public UserAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        Context context = parent.getContext();
        LayoutInflater inflater = LayoutInflater.from(context);

        // Inflate the custom layout
        View groupView = inflater.inflate(R.layout.item_user, parent, false);

        // Return a new holder instance
        UserAdapter.ViewHolder viewHolder = new UserAdapter.ViewHolder(groupView, listener);
        return viewHolder;
    }

    // Involves populating data into the item through holder
    @Override
    public void onBindViewHolder(@NonNull UserAdapter.ViewHolder holder, int position) {
        // Get the data model based on position
        User user = users.get(position);
        // Populate data into the views
        holder.tvUsername.setText(user.getUsername());
        holder.tvEmail.setText(user.getEmailPublic());
        // load in profile picture with glide
        ParseFile profilePicture = user.getProfilePicture();
        Glide.with(context).load(profilePicture.getUrl()).circleCrop().into(holder.ivProfilePicture);
    }

    @Override
    public int getItemCount() {
        return users.size();
    }

    // Clean all elements of the recycler
    public void clear() {
        users.clear();
        notifyDataSetChanged();
    }

}