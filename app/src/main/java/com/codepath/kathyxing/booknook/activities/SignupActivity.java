package com.codepath.kathyxing.booknook.activities;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.codepath.kathyxing.booknook.R;
import com.codepath.kathyxing.booknook.parse_classes.User;
import com.parse.ParseUser;

import java.util.Locale;

public class SignupActivity extends AppCompatActivity {

    public static final String TAG = "SignupActivity";
    private EditText etUsername;
    private EditText etEmail;
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
        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        etConfirmPassword = findViewById(R.id.etConfirmPassword);
        btnGoLogin = findViewById(R.id.btnGoLogin);
        btnSignup = findViewById(R.id.btnSignup);
        // click handler for go login button
        btnGoLogin.setOnClickListener(v -> {
            Log.i(TAG, "onClick go login button");
            goLoginActivity();
        });
        // click handler for signup button
        btnSignup.setOnClickListener(v -> {
            Log.i(TAG, "onClick signup button");
            // TODO: remove username trailing spaces
            String username = etUsername.getText().toString();
            String email = etEmail.getText().toString();
            String password = etPassword.getText().toString();
            String confirmPassword = etConfirmPassword.getText().toString();
            signupUser(username, email, password, confirmPassword);
        });
    }

    private void signupUser(String username, String email, String password, String confirmPassword) {
        if (username.equals("")) {
            Toast.makeText(SignupActivity.this, "You cannot have an empty username!", Toast.LENGTH_SHORT).show();
            return;
        } else if (email.equals("")) {
            Toast.makeText(SignupActivity.this, "You cannot have an empty email!", Toast.LENGTH_SHORT).show();
            return;
        } else if (password.equals("")) {
            Toast.makeText(SignupActivity.this, "You cannot have an empty password!", Toast.LENGTH_SHORT).show();
            return;
        } else if (confirmPassword.equals("")) {
            Toast.makeText(SignupActivity.this, "You must confirm your password!", Toast.LENGTH_SHORT).show();
            return;
        } else if (!password.equals(confirmPassword)) {
            Toast.makeText(SignupActivity.this, "Your passwords must match!", Toast.LENGTH_SHORT).show();
            etPassword.getText().clear();
            etConfirmPassword.getText().clear();
            return;
        }
        // Create the user
        User user = new User();
        // Set core properties
        user.setUsername(username);
        user.setUsernameLowercase(username.toLowerCase(Locale.ROOT));
        user.setPassword(password);
        user.setEmail(email.toLowerCase(Locale.ROOT));
        // Invoke signUpInBackground
        user.signUpInBackground(e -> {
            if (e == null) {
                // Hooray! Let them use the app now.
                ParseUser.logOut();
                showAlert("Account Created Successfully!", "Please verify your email before login", false);
            } else {
                // Sign up didn't succeed, look at the ParseException
                Log.e(TAG, "Issue with signup!", e);
                showAlert("Account Creation failed", "Account could not be created" + " : " + e.getMessage(), true);
                etUsername.getText().clear();
                etEmail.getText().clear();
                etPassword.getText().clear();
                etConfirmPassword.getText().clear();
            }
        });
    }

    private void showAlert(String title, String message, boolean error) {
        AlertDialog.Builder builder = new AlertDialog.Builder(SignupActivity.this)
                .setTitle(title)
                .setMessage(message)
                .setPositiveButton("OK", (dialog, which) -> {
                    dialog.cancel();
                    if (!error) {
                        Intent intent = new Intent(SignupActivity.this, LoginActivity.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(intent);
                    }
                });
        AlertDialog ok = builder.create();
        ok.show();
    }

    private void goLoginActivity() {
        Intent i = new Intent(this, LoginActivity.class);
        startActivity(i);
        finish();
    }

}