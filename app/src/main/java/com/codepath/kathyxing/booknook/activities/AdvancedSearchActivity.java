package com.codepath.kathyxing.booknook.activities;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.codepath.kathyxing.booknook.R;

public class AdvancedSearchActivity extends AppCompatActivity {

    // activity parameters
    public static final String TAG = "AdvancedSearchActivity";
    private EditText etAnyField;
    private EditText etTitle;
    private EditText etAuthor;
    private EditText etPublisher;
    private EditText etSubject;
    private EditText etIsbn;
    private Button btnSearch;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_advanced_search);

        // initialize parameters
        etAnyField = findViewById(R.id.etAnyField);
        etTitle = findViewById(R.id.etTitle);
        etAuthor = findViewById(R.id.etAuthor);
        etPublisher = findViewById(R.id.etPublisher);
        etSubject = findViewById(R.id.etSubject);
        etIsbn = findViewById(R.id.etIsbn);
        btnSearch = findViewById(R.id.btnSearch);

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

                // launch search results activity
                Intent i = new Intent(AdvancedSearchActivity.this, SearchResultsActivity.class);
                i.putExtra("anyField", anyField);
                i.putExtra("title", title);
                i.putExtra("author", author);
                i.putExtra("publisher", publisher);
                i.putExtra("subject", subject);
                i.putExtra("isbn", isbn);
                startActivity(i);
            }
        });
    }
}