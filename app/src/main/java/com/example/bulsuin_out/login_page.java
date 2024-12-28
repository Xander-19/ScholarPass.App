package com.example.bulsuin_out;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class login_page extends AppCompatActivity {

    private TextView loginbtn;
    private TextView usernameEdit, passwordEdit;
    private CheckBox rememberMeCheckBox;
    private OkHttpClient client;
    private static final String LOGIN_URL = "https://scholarpassserver-production.up.railway.app/api/guard/login";
    private static final String PREFS_NAME = "GuardAppPrefs";
    private static final String TOKEN_KEY = "authToken";
    private static final String REMEMBER_ME_KEY = "rememberMe";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_login_page);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        client = new OkHttpClient();

        loginbtn = findViewById(R.id.loginBtn);
        usernameEdit = findViewById(R.id.usernameEdit);
        passwordEdit = findViewById(R.id.passwordEdit);

        // Check if "Remember Me" is enabled and skip login if true
        SharedPreferences sharedPreferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        boolean rememberMe = sharedPreferences.getBoolean(REMEMBER_ME_KEY, false);
        if (rememberMe && sharedPreferences.contains(TOKEN_KEY)) {
            navigateToMainActivity();
            return;
        }

        client = new OkHttpClient();

        loginbtn = findViewById(R.id.loginBtn);
        usernameEdit = findViewById(R.id.usernameEdit);
        passwordEdit = findViewById(R.id.passwordEdit);
        rememberMeCheckBox = findViewById(R.id.rememberMe);

        loginbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String username = usernameEdit.getText().toString().trim();
                String password = passwordEdit.getText().toString().trim();
                boolean rememberMe = rememberMeCheckBox.isChecked();

                if (!username.isEmpty() && !password.isEmpty()) {
                    attemptLogin(username, password, rememberMe);
                } else {
                    Toast.makeText(login_page.this, "Please enter both username and password", Toast.LENGTH_SHORT).show();
                }
            }
        });

    }

    private void attemptLogin(String username, String password, boolean rememberMe) {
        JSONObject loginJson = new JSONObject();
        try {
            loginJson.put("username", username);
            loginJson.put("password", password);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        RequestBody body = RequestBody.create(loginJson.toString(), MediaType.parse("application/json; charset=utf-8"));
        Request request = new Request.Builder()
                .url(LOGIN_URL)
                .post(body)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                runOnUiThread(() -> Toast.makeText(login_page.this, "Login failed: " + e.getMessage(), Toast.LENGTH_SHORT).show());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    try {
                        JSONObject responseJson = new JSONObject(response.body().string());
                        String message = responseJson.optString("message");
                        String token = responseJson.optString("token");

                        if ("Login successful".equals(message)) {
                            saveLoginData(token, rememberMe);

                            runOnUiThread(() -> {
                                Toast.makeText(login_page.this, "Login Successful", Toast.LENGTH_SHORT).show();
                                navigateToMainActivity();
                            });
                        } else {
                            runOnUiThread(() -> Toast.makeText(login_page.this, "Login failed", Toast.LENGTH_SHORT).show());
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                        runOnUiThread(() -> Toast.makeText(login_page.this, "Response parsing error", Toast.LENGTH_SHORT).show());
                    }
                } else {
                    runOnUiThread(() -> Toast.makeText(login_page.this, "Invalid credentials", Toast.LENGTH_SHORT).show());
                }
            }
        });
    }

    private void saveLoginData(String token, boolean rememberMe) {
        SharedPreferences sharedPreferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(TOKEN_KEY, token);
        editor.putBoolean(REMEMBER_ME_KEY, rememberMe);
        editor.apply();
    }

    private void navigateToMainActivity() {
        startActivity(new Intent(login_page.this, MainActivity.class));
        finish();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finishAffinity();
    }
}