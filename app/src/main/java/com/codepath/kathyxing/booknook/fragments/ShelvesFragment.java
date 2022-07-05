package com.codepath.kathyxing.booknook.fragments;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.codepath.kathyxing.booknook.ParseQueryUtilities;
import com.codepath.kathyxing.booknook.R;
import com.codepath.kathyxing.booknook.activities.AddShelfActivity;
import com.codepath.kathyxing.booknook.activities.JoinGroupActivity;
import com.codepath.kathyxing.booknook.activities.ShelfDetailActivity;
import com.codepath.kathyxing.booknook.adapters.ShelfAdapter;
import com.codepath.kathyxing.booknook.parse_classes.Member;
import com.codepath.kathyxing.booknook.parse_classes.Shelf;
import com.parse.FindCallback;
import com.parse.ParseException;

import java.util.ArrayList;
import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 */
public class ShelvesFragment extends Fragment {

    // fragment parameters
    public static final String TAG = "ShelvesFragment";
    private static final int REMOVE_SHELF = 20;
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
    public void onCreate(@Nullable Bundle savedInstanceState) {
        // Set the toolbar text
        Activity activity = getActivity();
        if (activity != null) {
            activity.setTitle(getString(R.string.shelves));
        }
        super.onCreate(savedInstanceState);
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
            // Go to shelf detail
            Intent i = new Intent(getContext(), ShelfDetailActivity.class);
            i.putExtra("shelf", shelf);
            i.putExtra("position", position);
            startActivityForResult(i, REMOVE_SHELF);
        });
        // Attach the adapter to the RecyclerView
        rvShelves.setAdapter(shelfAdapter);
        // Set layout manager to position the items
        rvShelves.setLayoutManager(new LinearLayoutManager(getContext()));
        queryShelves();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if(resultCode == getActivity().RESULT_OK && data != null) {
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