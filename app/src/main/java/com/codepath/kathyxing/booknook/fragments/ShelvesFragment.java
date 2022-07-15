package com.codepath.kathyxing.booknook.fragments;

import static android.app.Activity.RESULT_OK;

import static androidx.recyclerview.widget.RecyclerView.HORIZONTAL;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.drawable.DrawableCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.codepath.kathyxing.booknook.ParseQueryUtilities;
import com.codepath.kathyxing.booknook.R;
import com.codepath.kathyxing.booknook.activities.AddShelfActivity;
import com.codepath.kathyxing.booknook.adapters.ShelfAdapter;
import com.codepath.kathyxing.booknook.parse_classes.BookOnShelf;
import com.codepath.kathyxing.booknook.parse_classes.Shelf;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
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
    private Spinner spinnerSortShelves;

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
        FloatingActionButton btnAddShelf = view.findViewById(R.id.btnAddShelf);
        spinnerSortShelves = view.findViewById(R.id.spinnerSortShelves);
        // set up the spinner
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(getContext(),
                R.array.shelf_sorting_array, R.layout.spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerSortShelves.setAdapter(adapter);
        spinnerSortShelves.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String valueFromSpinner = parent.getItemAtPosition(position).toString();
                if (valueFromSpinner.equals(getString(R.string.date_added))) {
                    queryShelves(ParseQueryUtilities.SORT_BY_DATE_ADDED);
                }
                if (valueFromSpinner.equals(getString(R.string.shelf_name))) {
                    queryShelves(ParseQueryUtilities.SORT_BY_SHELF_NAME);
                }
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
        // set up a click handler for rlAddShelf
        btnAddShelf.setOnClickListener(v -> goAddShelfActivity());
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
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
        rvShelves.setLayoutManager(layoutManager);
        // add divider between items
        DividerItemDecoration itemDecor = new DividerItemDecoration(requireContext(), layoutManager.getOrientation());
        rvShelves.addItemDecoration(itemDecor);
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
            @Override
            public void onChildDraw(@NonNull Canvas c, @NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, float dX, float dY, int actionState, boolean isCurrentlyActive) {
                super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);
                // set background color
                final ColorDrawable background = new ColorDrawable(Color.RED);
                background.setBounds(0, viewHolder.itemView.getTop(),   viewHolder.itemView.getLeft() + Math.round(dX), viewHolder.itemView.getBottom());
                background.draw(c);
                // set icon
                Drawable icon = ContextCompat.getDrawable(requireContext(), R.drawable.icon_remove);
                assert icon != null;
                DrawableCompat.setTint(icon, Color.WHITE);
                int verticalMargin = (viewHolder.itemView.getHeight() - icon.getIntrinsicHeight()) / 2;
                int horizontalMargin;
                if (dX > icon.getIntrinsicWidth()) {
                    horizontalMargin = Math.round((dX - icon.getIntrinsicWidth()) / 2);
                } else {
                    horizontalMargin = icon.getIntrinsicWidth() * -1;
                }
                Log.i(TAG, "height: " + icon.getIntrinsicHeight() + ", width: " + icon.getIntrinsicWidth());
                icon.setBounds(horizontalMargin, viewHolder.itemView.getTop() + verticalMargin, horizontalMargin + icon.getIntrinsicWidth(), viewHolder.itemView.getBottom() - verticalMargin);
                icon.draw(c);
            }
        }).attachToRecyclerView(rvShelves);
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
                int position = -1;
                String valueFromSpinner = spinnerSortShelves.getSelectedItem().toString();
                if (valueFromSpinner.equals(getString(R.string.date_added))) {
                    position = 0;
                    shelves.add(position, shelf);
                }
                if (valueFromSpinner.equals(getString(R.string.shelf_name))) {
                    // find the shelf's alphabetical location
                    for (int i = 0; i < shelves.size(); i++) {
                        if (shelves.get(i).getShelfName().compareToIgnoreCase(shelf.getShelfName()) >= 0) {
                            position = i;
                            shelves.add(position, shelf);
                            break;
                        }
                    }
                }
                if (position < 0) {
                    position = shelves.size();
                    shelves.add(shelf);
                }
                // Update the adapter
                shelfAdapter.notifyItemInserted(position);
                rvShelves.smoothScrollToPosition(position);
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
    private void queryShelves(int sortBy) {
        shelves.clear();
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
                rvShelves.smoothScrollToPosition(0);
                shelfAdapter.notifyDataSetChanged();
            }
            pbLoading.setVisibility(View.GONE);
        };
        ParseQueryUtilities.queryShelvesAsync(sortBy, queryShelvesCallback);
    }

    private void goAddShelfActivity() {
        Intent i = new Intent(getContext(), AddShelfActivity.class);
        startActivityForResult(i, ADD_SHELF);
    }
}