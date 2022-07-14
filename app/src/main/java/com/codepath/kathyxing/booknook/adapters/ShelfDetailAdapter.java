package com.codepath.kathyxing.booknook.adapters;

import android.content.Context;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.codepath.kathyxing.booknook.R;
import com.codepath.kathyxing.booknook.parse_classes.BookOnShelf;

import java.util.ArrayList;
import java.util.List;

public class ShelfDetailAdapter extends RecyclerView.Adapter<ShelfDetailAdapter.ViewHolder> {
    private final List<BookOnShelf> booksOnShelf;
    private final Context context;

    // Define listener member variable
    private ShelfDetailAdapter.OnItemClickListener listener;

    public ShelfDetailAdapter(Context context, ArrayList<BookOnShelf> booksOnShelf) {
        this.booksOnShelf = booksOnShelf;
        this.context = context;
    }

    // Define the method that allows the parent activity or fragment to define the listener
    public void setOnItemClickListener(ShelfDetailAdapter.OnItemClickListener listener) {
        this.listener = listener;
    }

    // Inflating layout from XML and return the holder
    @NonNull
    @Override
    public ShelfDetailAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        Context context = parent.getContext();
        LayoutInflater inflater = LayoutInflater.from(context);
        // Inflate the custom layout
        View bookView = inflater.inflate(R.layout.item_book, parent, false);
        // Return a new holder instance
        return new ShelfDetailAdapter.ViewHolder(bookView, listener);
    }

    // Involves populating data into the item through holder
    @Override
    public void onBindViewHolder(@NonNull ShelfDetailAdapter.ViewHolder holder, int position) {
        // Get the data model based on position
        BookOnShelf bookOnShelf = booksOnShelf.get(position);
        // Populate data into the template view using the data object
        holder.tvTitle.setText(bookOnShelf.getBookTitle());
        holder.tvAuthor.setText(bookOnShelf.getBookAuthor());
        // Load in the cover image with Glide
        if (bookOnShelf.getBookCoverUrl() != null) {
            Glide.with(getContext())
                    .load(Uri.parse(bookOnShelf.getBookCoverUrl()))
                    .apply(new RequestOptions()
                            .placeholder(R.drawable.ic_nocover))
                    .into(holder.ivCover);
        } else {
            Glide.with(getContext())
                    .load(R.drawable.ic_nocover)
                    .into(holder.ivCover);
        }
        // Return the completed view to render on screen
    }

    @Override
    public int getItemCount() {
        return booksOnShelf.size();
    }

    // Easy access to the context object in the recyclerview
    private Context getContext() {
        return context;
    }

    // Clean all elements of the recycler
    public void clear() {
        booksOnShelf.clear();
        notifyDataSetChanged();
    }

    // Define the listener interface
    public interface OnItemClickListener {
        void onItemClick(View itemView, int position);
    }

    // View lookup cache
    public static class ViewHolder extends RecyclerView.ViewHolder {
        public ImageView ivCover;
        public TextView tvTitle;
        public TextView tvAuthor;

        public ViewHolder(final View itemView, final ShelfDetailAdapter.OnItemClickListener clickListener) {
            // Stores the itemView in a public final member variable that can be used
            // to access the context from any ViewHolder instance.
            super(itemView);
            ivCover = itemView.findViewById(R.id.ivBookCover);
            tvTitle = itemView.findViewById(R.id.tvTitle);
            tvAuthor = itemView.findViewById(R.id.tvAuthor);
            itemView.setOnClickListener(v -> clickListener.onItemClick(itemView, getAdapterPosition()));
        }
    }
}
