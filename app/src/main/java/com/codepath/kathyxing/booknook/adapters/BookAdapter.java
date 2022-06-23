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
import com.codepath.kathyxing.booknook.models.Book;

import java.util.ArrayList;
import java.util.List;

public class BookAdapter extends RecyclerView.Adapter<BookAdapter.ViewHolder> {

    private List<Book> books;
    private Context context;

    // Define listener member variable
    private OnItemClickListener listener;

    // Define the listener interface
    public interface OnItemClickListener {
        void onItemClick(View itemView, int position);
    }

    // Define the method that allows the parent activity or fragment to define the listener
    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }

    // View lookup cache
    public class ViewHolder extends RecyclerView.ViewHolder {
        public ImageView ivCover;
        public TextView tvTitle;
        public TextView tvAuthor;

        public ViewHolder(final View itemView, final OnItemClickListener clickListener) {
            // Stores the itemView in a public final member variable that can be used
            // to access the context from any ViewHolder instance.
            super(itemView);

            ivCover = (ImageView)itemView.findViewById(R.id.ivBookCover);
            tvTitle = (TextView)itemView.findViewById(R.id.tvTitle);
            tvAuthor = (TextView)itemView.findViewById(R.id.tvAuthor);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    clickListener.onItemClick(itemView, getAdapterPosition());
                }
            });
        }
    }

    public BookAdapter(Context context, ArrayList<Book> books) {
        this.books = books;
        this.context = context;
    }

    // Inflating layout from XML and return the holder
    @Override
    public BookAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        Context context = parent.getContext();
        LayoutInflater inflater = LayoutInflater.from(context);

        // Inflate the custom layout
        View bookView = inflater.inflate(R.layout.item_book, parent, false);

        // Return a new holder instance
        BookAdapter.ViewHolder viewHolder = new BookAdapter.ViewHolder(bookView, listener);
        return viewHolder;
    }

    // Involves populating data into the item through holder
    @Override
    public void onBindViewHolder(@NonNull BookAdapter.ViewHolder holder, int position) {
        // Get the data model based on position
        Book book = books.get(position);

        // Populate data into the template view using the data object
        holder.tvTitle.setText(book.getTitle());
        holder.tvAuthor.setText(book.getAuthor());

        Glide.with(getContext())
                .load(Uri.parse(book.getCoverUrl()))
                .apply(new RequestOptions()
                        .placeholder(R.drawable.ic_nocover))
                .into(holder.ivCover);
        // Return the completed view to render on screen
    }

    @Override
    public int getItemCount() {
        return books.size();
    }

    // Easy access to the context object in the recyclerview
    private Context getContext() {
        return context;
    }
}
