package com.codepath.kathyxing.booknook.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.codepath.kathyxing.booknook.R;
import com.codepath.kathyxing.booknook.parse_classes.Group;
import com.codepath.kathyxing.booknook.parse_classes.Post;
import com.parse.ParseException;
import com.parse.ParseFile;

import java.util.List;

public class PostsAdapter extends RecyclerView.Adapter<PostsAdapter.ViewHolder> {

    private Context context;
    private List<Post> posts;

    public PostsAdapter(Context context, List<Post> posts) {
        this.context = context;
        this.posts = posts;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_post, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Post post = posts.get(position);
        holder.bind(post);
    }

    @Override
    public int getItemCount() {
        return posts.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {

        private TextView tvUsername;
        private TextView tvGroupName;
        private TextView tvTimeAgo;
        private ImageView ivProfilePicture;
        private ImageView ivPostImage;
        private TextView tvPostDescription;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvUsername = itemView.findViewById(R.id.tvUsername);
            tvGroupName = itemView.findViewById(R.id.tvGroupName);
            tvTimeAgo = itemView.findViewById(R.id.tvTimeAgo);
            ivPostImage = itemView.findViewById(R.id.ivPostImage);
            tvPostDescription = itemView.findViewById(R.id.tvPostDescription);
            ivProfilePicture = itemView.findViewById(R.id.ivProfilePicture);
        }

        public void bind(Post post) {
            // Bind the post data to the view elements
            try {
                tvGroupName.setText(post.getGroup().fetchIfNeeded().getString(Group.KEY_GROUP_NAME));
            } catch (ParseException e) {
                e.printStackTrace();
            }
            tvPostDescription.setText(post.getDescription());
            tvTimeAgo.setText(Post.calculateTimeAgo(post.getCreatedAt()));
            tvUsername.setText(post.getUser().getUsername());

            // TODO: rotate the image to face the correct orientation
            // load in image with glide
            ParseFile image = post.getImage();
            if (image != null) {
                Glide.with(context).load(image.getUrl()).into(ivPostImage);
            }
            else {
                ivPostImage.setVisibility(View.GONE);
            }

            // load in profile picture with glide
            ParseFile profilePicture = post.getUser().getProfilePicture();
            Glide.with(context).load(profilePicture.getUrl()).circleCrop().into(ivProfilePicture);

        }
    }

    // Clean all elements of the recycler
    public void clear() {
        posts.clear();
        notifyDataSetChanged();
    }

    // Add a list of items -- change to type used
    public void addAll(List<Post> list) {
        posts.addAll(list);
        notifyDataSetChanged();
    }
}