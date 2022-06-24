package com.codepath.kathyxing.booknook.fragments;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import com.codepath.kathyxing.booknook.R;

/**
 * A simple {@link Fragment} subclass.
 */
public class AdvancedSearchFragment extends Fragment {

    // activity parameters
    public static final String TAG = "AdvancedSearchFragment";
    private EditText etAnyField;
    private EditText etTitle;
    private EditText etAuthor;
    private EditText etPublisher;
    private EditText etSubject;
    private EditText etIsbn;
    private Button btnSearch;

    // Required empty public constructor
    public AdvancedSearchFragment() {}

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
                getActivity().getSupportFragmentManager().beginTransaction()
                        .replace(((ViewGroup)getView().getParent()).getId(), nextFragment)
                        .addToBackStack(null)
                        .commit();
            }
        });
    }
}