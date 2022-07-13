package com.codepath.kathyxing.booknook.activities;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;

import com.codepath.kathyxing.booknook.AlertUtilities;
import com.codepath.kathyxing.booknook.R;
import com.codepath.kathyxing.booknook.parse_classes.User;
import com.google.android.material.textfield.TextInputEditText;
import com.parse.ParseUser;

import java.util.Calendar;
import java.util.Objects;

public class LoginActivity extends BaseActivity {
    public static final String TAG = "LoginActivity";
    private TextInputEditText etUsername;
    private TextInputEditText etPassword;
    private Button btnLogin;
    private Button btnGoSignup;
    private Button btnForgotPassword;
    private ProgressBar pbLoading;
    private RelativeLayout rlLogin;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        // If user is already logged in, don't show login screen
        if (ParseUser.getCurrentUser() != null) {
            goMainActivity(false);
        }
        // Initialize views
        etUsername = findViewById(R.id.etUsername);
        etPassword = findViewById(R.id.etPassword);
        btnLogin = findViewById(R.id.btnLogin);
        btnGoSignup = findViewById(R.id.btnGoSignup);
        btnForgotPassword = findViewById(R.id.btnForgotPassword);
        pbLoading = findViewById(R.id.pbLoading);
        rlLogin = findViewById(R.id.rlLogin);
        // click handler for go sign up button
        btnGoSignup.setOnClickListener(v -> {
            Log.i(TAG, "onClick go signup button");
            goSignupActivity();
        });
        // click handler for login button
        btnLogin.setOnClickListener(v -> {
            Log.i(TAG, "onClick login button");
            String username = etUsername.getText().toString().trim();
            String password = etPassword.getText().toString();
            loginUser(username, password);
        });
        // click handler for forgot password button
        btnForgotPassword.setOnClickListener(v -> goForgotPasswordActivity());
        // hide keyboard when the relative layout is touched
        setupUI(rlLogin);
    }

    private void loginUser(String username, String password) {
        Log.i(TAG, "Attempting to login user " + username);
        btnLogin.setVisibility(View.INVISIBLE);
        pbLoading.setVisibility(View.VISIBLE);
        ParseUser.logInInBackground(username, password, (user, e) -> {
            if (e != null) {
                Log.e(TAG, "Issues with login", e);
                // show an alert
                AlertUtilities.GoActivity goActivity = () -> {
                    Intent intent = new Intent(LoginActivity.this, LoginActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);
                };
                AlertUtilities.showAlert(LoginActivity.this, "Login Fail", e.getMessage() + " Please try again.", true, goActivity);
                // clear the edit texts
                Objects.requireNonNull(etUsername.getText()).clear();
                Objects.requireNonNull(etPassword.getText()).clear();
            } else {
                goMainActivity(((User) user).getLastLogin() == null);
                ((User) user).setLastLogin(Calendar.getInstance().getTime());
                user.saveInBackground();
                Log.i(TAG, "Successfully logged in");
            }
            btnLogin.setVisibility(View.VISIBLE);
            pbLoading.setVisibility(View.GONE);
        });
    }

    private void goMainActivity(boolean firstLogin) {
        Intent i = new Intent(this, MainActivity.class);
        i.putExtra("firstLogin", firstLogin);
        startActivity(i);
        finish();
    }

    private void goSignupActivity() {
        Intent i = new Intent(this, SignupActivity.class);
        startActivity(i);
        finish();
    }

    private void goForgotPasswordActivity() {
        Intent i = new Intent(this, ForgotPasswordActivity.class);
        startActivity(i);
    }
}