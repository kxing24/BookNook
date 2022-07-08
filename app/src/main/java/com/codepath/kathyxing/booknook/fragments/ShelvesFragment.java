package com.codepath.kathyxing.booknook.fragments;

import static android.app.Activity.RESULT_OK;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.codepath.kathyxing.booknook.ParseQueryUtilities;
import com.codepath.kathyxing.booknook.R;
import com.codepath.kathyxing.booknook.activities.AddShelfActivity;
import com.codepath.kathyxing.booknook.adapters.ShelfAdapter;
import com.codepath.kathyxing.booknook.parse_classes.BookOnShelf;
import com.codepath.kathyxing.booknook.parse_classes.Shelf;
import com.google.android.material.snackbar.Snackbar;
import com.parse.FindCallback;

import java.util.ArrayList;
import java.util.Objects;

/**
 * A simple {@link Fragment} subclass.
 */
public class ShelvesFragment extends Fragment {

    // fragment parameters
    public static final String TAG = "ShelvesFragment";
    private static final int ADD_SHELF = 40;
    private RecyclerView rvShelves;
    private ShelfAdapter shelfAdapter;
    private ArrayList<Shelf> shelves;
    private ProgressBar pbLoading;
    private TextView tvNoShelves;
    private RelativeLayout rlAddShelf;

    public ShelvesFragment() {
        // Required empty public constructor
    }

    @Override
    public void onResume() {
        super.onResume();
        // Set the toolbar text
        Activity activity = getActivity();
        if (activity != null) {
            activity.setTitle(getString(R.string.shelves));
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_shelves, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        // initialize fields
        rvShelves = view.findViewById(R.id.rvShelves);
        shelves = new ArrayList<>();
        shelfAdapter = new ShelfAdapter(getContext(), shelves);
        pbLoading = view.findViewById(R.id.pbLoading);
        tvNoShelves = view.findViewById(R.id.tvNoShelves);
        rlAddShelf = view.findViewById(R.id.rlAddShelf);
        // set up a click handler for rlAddShelf
        rlAddShelf.setOnClickListener(v -> goAddShelfActivity());
        // set up a click handler for bookAdapter
        shelfAdapter.setOnItemClickListener((itemView, position) -> {
            // get the shelf clicked
            Shelf shelf = shelves.get(position);
            // swap in the shelf detail fragment
            ShelfDetailFragment nextFragment = new ShelfDetailFragment();
            Bundle bundle = new Bundle();
            bundle.putParcelable("shelf", shelf);
            nextFragment.setArguments(bundle);
            if (getActivity() != null && getView() != null) {
                getActivity().getSupportFragmentManager().beginTransaction()
                        .replace(((ViewGroup) getView().getParent()).getId(), nextFragment)
                        .addToBackStack(null)
                        .commit();
            }
        });
        // Attach the adapter to the RecyclerView
        rvShelves.setAdapter(shelfAdapter);
        // Set layout manager to position the items
        rvShelves.setLayoutManager(new LinearLayoutManager(getContext()));
        // enable swipe to delete
        new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.RIGHT) {
            @Override
            public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
                return false;
            }
            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
                // get the shelf swiped and its position
                Shelf shelf = shelves.get(viewHolder.getAdapterPosition());
                // prompt for confirmation of item removal
                showRemoveShelfConfirmation(shelf, viewHolder.getAdapterPosition());
            }
        }).attachToRecyclerView(rvShelves);
        queryShelves();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (resultCode == RESULT_OK && data != null) {
            if (requestCode == ADD_SHELF) {
                Log.i(TAG, "shelf added!");
                // Get data from the intent
                Shelf shelf = (Shelf) data.getExtras().get("shelf");
                // Update the RV with the shelf
                // Modify data source of shelves
                shelves.add(0, shelf);
                // Update the adapter
                shelfAdapter.notifyItemInserted(0);
                rvShelves.smoothScrollToPosition(0);
                tvNoShelves.setVisibility(View.GONE);
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    private void showRemoveShelfConfirmation(Shelf shelf, int position) {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext())
                .setTitle("Remove Shelf")
                .setMessage("Are you sure you want to delete this shelf?")
                .setCancelable(false)
                .setPositiveButton("Yes", (dialog, which) -> removeShelf(shelf, position))
                .setNegativeButton("Never mind", (dialog, which) -> {
                    dialog.dismiss();
                    shelfAdapter.notifyItemChanged(position);
                });
        builder.show();
    }

    private void removeShelf(@NonNull Shelf shelf, int position) {
        // delete the shelf
        shelf.deleteInBackground();
        // delete all of the books on the shelf
        FindCallback<BookOnShelf> getBooksOnShelfCallback = (booksOnShelf, e) -> {
            if (e == null) {
                for (BookOnShelf bookOnShelf : booksOnShelf) {
                    bookOnShelf.deleteInBackground();
                }
            } else {
                Log.e(TAG, "issue deleting book on shelf", e);
            }
        };
        ParseQueryUtilities.getBooksOnShelfAsync(shelf, getBooksOnShelfCallback);
        // remove shelf from the adapter
        shelves.remove(shelf);
        shelfAdapter.notifyItemRemoved(position);
    }

    // get the shelves and add them to the shelves list
    private void queryShelves() {
        FindCallback<Shelf> queryShelvesCallback = (objects, e) -> {
            // check for errors
            if (e != null) {
                Log.e(TAG, "Issue with getting shelves", e);
                return;
            }
            if (objects.isEmpty()) {
                tvNoShelves.setVisibility(View.VISIBLE);
            } else {
                // save received shelves to list and notify adapter of new data
                shelves.addAll(objects);
                shelfAdapter.notifyDataSetChanged();
            }
            pbLoading.setVisibility(View.GONE);
        };
        ParseQueryUtilities.queryShelvesAsync(queryShelvesCallback);
    }

    private void goAddShelfActivity() {
        Intent i = new Intent(getContext(), AddShelfActivity.class);
        startActivityForResult(i, ADD_SHELF);
    }
}