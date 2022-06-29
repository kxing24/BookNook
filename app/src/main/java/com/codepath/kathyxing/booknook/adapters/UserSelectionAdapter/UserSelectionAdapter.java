package com.codepath.kathyxing.booknook.adapters.UserSelectionAdapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.selection.ItemDetailsLookup;
import androidx.recyclerview.selection.SelectionTracker;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.codepath.kathyxing.booknook.R;
import com.codepath.kathyxing.booknook.parse_classes.User;
import com.parse.ParseFile;

import java.util.ArrayList;

public class UserSelectionAdapter extends RecyclerView.Adapter<UserSelectionAdapter.UserViewHolder> {

    private ArrayList<User> users;
    private Context context;
    private SelectionTracker selectionTracker;

    public UserSelectionAdapter(Context context, ArrayList<User> users) {
        this.users = users;
        this.context = context;
    }

    public SelectionTracker getSelectionTracker() {
        return selectionTracker;
    }

    public void setSelectionTracker(SelectionTracker selectionTracker) {
        this.selectionTracker = selectionTracker;
    }

    @NonNull
    @Override
    public UserViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_user, parent, false);
        return new UserViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull UserViewHolder holder, int position) {
        User user = users.get(position);

        holder.bind(user, selectionTracker.isSelected(user));

    }

    @Override
    public int getItemCount() {
        return users.size();
    }

    public class UserViewHolder extends RecyclerView.ViewHolder implements ViewHolderWithDetails {
        private ImageView ivProfilePicture;
        private TextView tvUsername;

        public UserViewHolder(@NonNull View itemView) {
            super(itemView);
            // initialize views
            ivProfilePicture = itemView.findViewById(R.id.ivProfilePicture);
            tvUsername = itemView.findViewById(R.id.tvUsername);
        }

        public final void bind(User user, boolean isActive) {
            itemView.setActivated(isActive);
            // load in profile picture with glide
            ParseFile profilePicture = user.getProfilePicture();
            Glide.with(context).load(profilePicture.getUrl()).circleCrop().into(ivProfilePicture);
            // Populate data into the views
            tvUsername.setText(user.getUsername());
        }

        @Override
        public ItemDetailsLookup.ItemDetails getItemDetails() {
            return new UserSelectionDetail(getAdapterPosition(), users.get(getAdapterPosition()));
        }
    }

}
