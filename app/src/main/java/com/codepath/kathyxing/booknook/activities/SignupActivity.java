package com.codepath.kathyxing.booknook.activities;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.codepath.kathyxing.booknook.R;
import com.codepath.kathyxing.booknook.parse_classes.User;
import com.parse.ParseException;
import com.parse.SignUpCallback;

public class SignupActivity extends AppCompatActivity {

    public static final String TAG = "SignupActivity";
    private EditText etUsername;
    private EditText etPassword;
    private EditText etConfirmPassword;
    private Button btnSignup;
    private Button btnGoLogin;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        // initialize views
        etUsername = findViewById(R.id.etUsername);
        etPassword = findViewById(R.id.etPassword);
        etConfirmPassword = findViewById(R.id.etConfirmPassword);
        btnGoLogin = findViewById(R.id.btnGoLogin);
        btnSignup = findViewById(R.id.btnSignup);

        // click handler for go login button
        btnGoLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.i(TAG, "onClick go login button");
                goLoginActivity();
            }
        });

        // click handler for signup button
        btnSignup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.i(TAG, "onClick signup button");
                String username = etUsername.getText().toString();
                String password = etPassword.getText().toString();
                String confirmPassword = etConfirmPassword.getText().toString();
                signupUser(username, password, confirmPassword);
            }
        });
    }

    private void signupUser(String username, String password, String confirmPassword) {
        if (username.equals("")) {
            Toast.makeText(SignupActivity.this, "You cannot have an empty username!", Toast.LENGTH_SHORT).show();
            return;
        }
        else if (password.equals("")) {
            Toast.makeText(SignupActivity.this, "You cannot have an empty password!", Toast.LENGTH_SHORT).show();
            return;
        }
        else if (confirmPassword.equals("")) {
            Toast.makeText(SignupActivity.this, "You must confirm your password!", Toast.LENGTH_SHORT).show();
            return;
        }
        else if (!password.equals(confirmPassword)) {
            Toast.makeText(SignupActivity.this, "Your passwords must match!", Toast.LENGTH_SHORT).show();
            etPassword.getText().clear();
            etConfirmPassword.getText().clear();
            return;
        }

        // Create the user
        User user = new User();
        // Set core properties
        user.setUsername(username);
        user.setPassword(password);
        // Invoke signUpInBackground
        user.signUpInBackground(new SignUpCallback() {
            public void done(ParseException e) {
                if (e == null) {
                    // Hooray! Let them use the app now.
                    Toast.makeText(SignupActivity.this, "You have signed up for an account!", Toast.LENGTH_SHORT).show();
                    goBookSearchActivity();
                } else {
                    // Sign up didn't succeed. Look at the ParseException
                    // to figure out what went wrong
                    Log.e(TAG, "Issue with signup!", e);
                }
            }
        });
    }

    private void goLoginActivity() {
        Intent i = new Intent(this, LoginActivity.class);
        startActivity(i);
        finish();
    }

    private void goBookSearchActivity() {
        Intent i = new Intent(this, BookSearchActivity.class);
        startActivity(i);
        finish();
    }
}