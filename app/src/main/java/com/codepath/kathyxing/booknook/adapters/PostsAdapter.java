package com.codepath.kathyxing.booknook.adapters;

import android.content.Context;
import android.content.Intent;
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
import com.codepath.kathyxing.booknook.ParseQueryUtilities;
import com.codepath.kathyxing.booknook.R;
import com.codepath.kathyxing.booknook.activities.UserProfileActivity;
import com.codepath.kathyxing.booknook.parse_classes.Group;
import com.codepath.kathyxing.booknook.parse_classes.Like;
import com.codepath.kathyxing.booknook.parse_classes.Post;
import com.like.LikeButton;
import com.like.OnLikeListener;
import com.parse.CountCallback;
import com.parse.DeleteCallback;
import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import java.util.List;

public class PostsAdapter extends RecyclerView.Adapter<PostsAdapter.ViewHolder> {

    public static final String TAG = "PostsAdapter";
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

    public class ViewHolder extends RecyclerView.ViewHolder implements android.view.View.OnClickListener {

        private TextView tvUsername;
        private TextView tvGroupName;
        private TextView tvTimeAgo;
        private ImageView ivProfilePicture;
        private ImageView ivPostImage;
        private TextView tvPostDescription;
        private LikeButton likeButton;
        private TextView tvLikeCount;
        private RelativeLayout rlUserProfile;
        private Post post;
        private int likeCount = 0;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvUsername = itemView.findViewById(R.id.tvUsername);
            tvGroupName = itemView.findViewById(R.id.tvGroupName);
            tvTimeAgo = itemView.findViewById(R.id.tvTimeAgo);
            ivPostImage = itemView.findViewById(R.id.ivPostImage);
            tvPostDescription = itemView.findViewById(R.id.tvPostDescription);
            ivProfilePicture = itemView.findViewById(R.id.ivProfilePicture);
            likeButton = itemView.findViewById(R.id.likeButton);
            tvLikeCount = itemView.findViewById(R.id.tvLikeCount);
            rlUserProfile = itemView.findViewById(R.id.rlUserProfile);
            // set click handler for rlUserProfile
            rlUserProfile.setOnClickListener(this);
            // set click handler for like button
            likeButton.setOnLikeListener(new OnLikeListener() {
                @Override
                public void liked(LikeButton likeButton) {
                    likePost();
                }
                @Override
                public void unLiked(LikeButton likeButton) {
                    unlikePost();
                }
            });
        }

        public void bind(@NonNull Post post) {
            // Bind the post data to the view elements
            this.post = post;
            try {
                tvGroupName.setText(post.getGroup().fetchIfNeeded().getString(Group.KEY_GROUP_NAME));
            } catch (ParseException e) {
                e.printStackTrace();
            }
            tvPostDescription.setText(post.getDescription());
            tvTimeAgo.setText(Post.calculateTimeAgo(post.getCreatedAt()));
            tvUsername.setText(post.getUser().getUsername());
            setLikeCount();
            setLike();
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

        @Override
        public void onClick(View v) {
            if(v == rlUserProfile) {
                if(!post.getUser().getObjectId().equals(ParseUser.getCurrentUser().getObjectId())) {
                    Intent intent = new Intent(context, UserProfileActivity.class);
                    intent.putExtra("user", post.getUser());
                    context.startActivity(intent);
                }
            }
        }

        private void likePost() {
            likeButton.setEnabled(false);
            SaveCallback likePostCallback = e -> {
                if (e == null) {
                    likeButton.setEnabled(true);
                    tvLikeCount.setText(Integer.toString(++likeCount));
                } else {
                    Log.e(TAG, "Issue liking post", e);
                }
            };
            ParseQueryUtilities.likePostAsync(post, likePostCallback);
        }

        private void unlikePost() {
            likeButton.setEnabled(false);
            DeleteCallback unlikePostCallback = e -> {
                if(e == null) {
                    likeButton.setEnabled(true);
                    tvLikeCount.setText(Integer.toString(--likeCount));
                } else {
                    Log.e(TAG, "Issue unliking post", e);
                }
            };
            GetCallback<Like> getLikePostCallback = (like, e) -> {
                if(e == null) {
                    ParseQueryUtilities.unlikePostAsync(like, unlikePostCallback);
                } else {
                    Log.e(TAG, "Issue getting friend relation", e);
                }
            };
            ParseQueryUtilities.getLikePostAsync(post, getLikePostCallback);
        }

        private void setLikeCount() {
            CountCallback getPostLikeCallback = (count, e) -> {
                if (e == null) {
                    tvLikeCount.setText(Integer.toString(count));
                    likeCount = count;
                } else {
                    Log.e(TAG, "issue getting post like count", e);
                }
            };
            ParseQueryUtilities.getPostLikeCountAsync(post, getPostLikeCallback);
        }

        private void setLike() {
            GetCallback<Like> getUserLikePostStatusCallback = (object, e) -> {
                if (e == null) {
                    // user liked the post
                    likeButton.setLiked(true);
                } else {
                    if(e.getCode() == ParseException.OBJECT_NOT_FOUND) {
                        // user has not liked the post
                        likeButton.setLiked(false);
                    } else {
                        // unknown error, debug
                        Log.e(TAG, "issue getting user like post status", e);
                    }
                }
            };
            ParseQueryUtilities.getUserLikePostStatusAsync(post, getUserLikePostStatusCallback);
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
