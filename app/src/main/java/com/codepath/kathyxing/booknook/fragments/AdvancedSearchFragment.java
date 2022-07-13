package com.codepath.kathyxing.booknook.fragments;

import android.app.Activity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.codepath.kathyxing.booknook.R;
import com.codepath.kathyxing.booknook.activities.MainActivity;

/**
 * A simple {@link Fragment} subclass.
 */
public class AdvancedSearchFragment extends Fragment {

    // activity parameters
    public static final String TAG = "AdvancedSearchFragment";
    private RelativeLayout rlAdvancedSearch;
    private EditText etAnyField;
    private EditText etTitle;
    private EditText etAuthor;
    private EditText etPublisher;
    private EditText etSubject;
    private EditText etIsbn;
    private Button btnSearch;

    // Required empty public constructor
    public AdvancedSearchFragment() {
    }

    @Override
    public void onResume() {
        super.onResume();
        // Set the toolbar text
        Activity activity = getActivity();
        if (activity != null) {
            activity.setTitle("Advanced Search");
        }
        ((MainActivity) getActivity()).showBackButton();
        clearEditTexts();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_advanced_search, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        // initialize parameters
        rlAdvancedSearch = view.findViewById(R.id.rlAdvancedSearch);
        etAnyField = view.findViewById(R.id.etAnyField);
        etTitle = view.findViewById(R.id.etTitle);
        etAuthor = view.findViewById(R.id.etAuthor);
        etPublisher = view.findViewById(R.id.etPublisher);
        etSubject = view.findViewById(R.id.etSubject);
        etIsbn = view.findViewById(R.id.etIsbn);
        btnSearch = view.findViewById(R.id.btnSearch);
        // set click handler for search button
        btnSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // get the values from the EditText's
                String anyField = etAnyField.getText().toString();
                String title = etTitle.getText().toString();
                String author = etAuthor.getText().toString();
                String publisher = etPublisher.getText().toString();
                String subject = etSubject.getText().toString();
                String isbn = etIsbn.getText().toString();
                // if every EditText is empty, make a Toast message
                if (anyField.equals("") && title.equals("") && author.equals("") &&
                        publisher.equals("") && subject.equals("") && isbn.equals("")) {
                    Toast.makeText(getContext(), "Make a query!", Toast.LENGTH_SHORT).show();
                } else {
                    // swap in the search results fragment
                    SearchResultsFragment nextFragment = new SearchResultsFragment();
                    Bundle bundle = new Bundle();
                    bundle.putString("anyField", anyField);
                    bundle.putString("title", title);
                    bundle.putString("author", author);
                    bundle.putString("publisher", publisher);
                    bundle.putString("subject", subject);
                    bundle.putString("isbn", isbn);
                    nextFragment.setArguments(bundle);
                    getActivity().getSupportFragmentManager()
                            .beginTransaction()
                            .replace(((ViewGroup) getView().getParent()).getId(), nextFragment)
                            .addToBackStack(null)
                            .commit();
                }
            }
        });
        // hide keyboard when the relative layout is touched
        rlAdvancedSearch.setOnTouchListener((v, event) -> {
            InputMethodManager imm = (InputMethodManager) getContext().getSystemService(getActivity().INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(getActivity().getCurrentFocus().getWindowToken(), 0);
            return true;
        });
    }

    private void clearEditTexts() {
        etAnyField.getText().clear();
        etTitle.getText().clear();
        etAuthor.getText().clear();
        etPublisher.getText().clear();
        etSubject.getText().clear();
        etIsbn.getText().clear();
    }
}