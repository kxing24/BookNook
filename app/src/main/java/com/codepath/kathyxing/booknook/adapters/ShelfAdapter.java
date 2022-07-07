package com.codepath.kathyxing.booknook.adapters;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.codepath.kathyxing.booknook.ParseQueryUtilities;
import com.codepath.kathyxing.booknook.R;
import com.codepath.kathyxing.booknook.parse_classes.Shelf;
import com.parse.CountCallback;
import com.parse.ParseException;

import java.util.ArrayList;
import java.util.List;

public class ShelfAdapter extends RecyclerView.Adapter<ShelfAdapter.ViewHolder> {

    // adapter parameters
    public static final String TAG = "ShelfAdapter";
    private final List<Shelf> shelves;
    private final Context context;

    // Define listener member variable
    private ShelfAdapter.OnItemClickListener listener;

    // Define the listener interface
    public interface OnItemClickListener {
        void onItemClick(View itemView, int position);
    }

    // Define the method that allows the parent activity or fragment to define the listener
    public void setOnItemClickListener(ShelfAdapter.OnItemClickListener listener) {
        this.listener = listener;
    }

    // View lookup cache
    public static class ViewHolder extends RecyclerView.ViewHolder {
        public TextView tvShelfName;
        public TextView tvBookCount;

        public ViewHolder(final View itemView, final ShelfAdapter.OnItemClickListener clickListener) {
            // Stores the itemView in a public final member variable that can be used
            // to access the context from any ViewHolder instance.
            super(itemView);
            tvShelfName = itemView.findViewById(R.id.tvShelfName);
            tvBookCount = itemView.findViewById(R.id.tvBookCount);
            // set up click handler for the item
            itemView.setOnClickListener(v -> clickListener.onItemClick(itemView, getAdapterPosition()));
        }
    }

    public ShelfAdapter(Context context, ArrayList<Shelf> shelves) {
        this.shelves = shelves;
        this.context = context;
    }

    // Inflating layout from XML and return the holder
    @NonNull
    @Override
    public ShelfAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        Context context = parent.getContext();
        LayoutInflater inflater = LayoutInflater.from(context);

        // Inflate the custom layout
        View groupView = inflater.inflate(R.layout.item_shelf, parent, false);

        // Return a new holder instance
        return new ShelfAdapter.ViewHolder(groupView, listener);
    }

    // Involves populating data into the item through holder
    @Override
    public void onBindViewHolder(@NonNull ShelfAdapter.ViewHolder holder, int position) {
        // Get the data model based on position
        Log.i(TAG, "position is " + position);
        Shelf shelf = shelves.get(position);
        holder.tvShelfName.setText(shelf.getShelfName());
        setBookCount(shelf, holder);
    }

    @Override
    public int getItemCount() {
        return shelves.size();
    }

    // Easy access to the context object in the recyclerview
    private Context getContext() {
        return context;
    }

    private void setBookCount(@NonNull Shelf shelf, @NonNull ShelfAdapter.ViewHolder holder) {
        CountCallback getShelfBookCountCallback = (count, e) -> holder.tvBookCount.setText(count + " books");
        ParseQueryUtilities.getShelfBookCountAsync(shelf, getShelfBookCountCallback);
    }
}
