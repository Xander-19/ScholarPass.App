package com.example.bulsuin_out;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.bumptech.glide.Glide;
import com.google.android.material.navigation.NavigationView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class violationResult_page extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    private DrawerLayout drawerlayout;
    private NavigationView naviView;
    private RequestQueue queue;
    private Spinner violationSpinner;
    private Button violationSubmitBtn;
    private TextView studName, studNumber, departmentTxt;
    private String studNo, studentID;
    private String violationName, violationSeverity;
    private ImageView avatarImageView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        Log.d("result_page", "Result page started.");
        setContentView(R.layout.activity_violation_result_page);

        // Set up window insets for edge-to-edge UI
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Para sa drawer menu
        Toolbar toolbar = findViewById(R.id.menuBar);
        setSupportActionBar(toolbar);

        drawerlayout = findViewById(R.id.drawer_layout);
        naviView = findViewById(R.id.navigationView);
        naviView.setNavigationItemSelectedListener(this);

        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawerlayout, toolbar,
                R.string.open_nav, R.string.close_nav);
        drawerlayout.addDrawerListener(toggle);
        toggle.syncState();

        // Initialize views
        violationSpinner = findViewById(R.id.violationDropdown);
        avatarImageView = findViewById(R.id.avatarImageView);
        studName = findViewById(R.id.studName);
        studNumber = findViewById(R.id.studNoResultTxt);
        departmentTxt = findViewById(R.id.departmentResultTxt);

        // Initialize the request queue
        queue = Volley.newRequestQueue(this);

        // Fetch data for Spinner
        fetchViolationsFromAPI();

        // Get QR result or Student Number
        String qrResultJson = getIntent().getStringExtra("QR_RESULT_JSON");
        if (qrResultJson != null) {
            // Handle QR code result if available
            handleQrResult(qrResultJson);
        } else {
            // Fetch data using Student Number if QR is not available
            String studentNumber = getIntent().getStringExtra("StudentNumber");
            if (studentNumber != null) {
                fetchStudentDataByNumber(studentNumber);
            } else {
                Toast.makeText(this, "No student identifier provided.", Toast.LENGTH_SHORT).show();
            }
        }
        // Submit button click
        violationSubmitBtn = findViewById(R.id.violationSubmitBtn);
        violationSubmitBtn.setOnClickListener(v -> {submitViolation();});
    }

    private void handleQrResult(String qrResultJson) {
        try {
            JSONObject qrResultObject = new JSONObject(qrResultJson);
            studentID = qrResultObject.optString("studentID");
            fetchDataFromApi(studentID);
        } catch (JSONException e) {
            Log.e("result_page", "Error parsing JSON: " + e.getMessage());
            Toast.makeText(this, "Error reading QR result.", Toast.LENGTH_SHORT).show();
        }
    }

    private void fetchStudentDataByNumber(String studentNumber) {
        String url = "https://scholarpassserver-production.up.railway.app/api/log/logging/student?studentNumber=" + studentNumber;

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, url, null,
                response -> {
                    try {
                        JSONObject studObject = response.getJSONObject("student");
                        String avatarUrl = studObject.getString("pfp");
                        String name = studObject.getString("name");
                        studNo = studObject.getString("studentNumber");
                        String department = studObject.getString("department");

                        Glide.with(violationResult_page.this).load(avatarUrl).into(avatarImageView);
                        studName.setText(name);
                        studNumber.setText(studNo);
                        departmentTxt.setText(department);
                    } catch (Exception e) {
                        Log.e("result_page", "Error parsing response: " + e.getMessage());
                    }
                },
                error -> {
                    Log.e("result_page", "API request error: " + error.getMessage());
                    Toast.makeText(this, "Failed to fetch student data by number.", Toast.LENGTH_SHORT).show();
                });

        queue.add(jsonObjectRequest);
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.menu_home) {
            Intent intent = new Intent(this, MainActivity.class);
            startActivity(intent);
            finish();
        } else if (id == R.id.menu_about) {
            Intent intent = new Intent(this, about_page.class);
            startActivity(intent);
            finish();
        } else if (id == R.id.menu_logout) {
            showLogoutDialog();
        }

        drawerlayout.closeDrawers();
        return true;
    }

    private void showLogoutDialog() {
        new AlertDialog.Builder(violationResult_page.this)
                .setTitle("Logout")
                .setMessage("Are you sure you want to log out?")
                .setPositiveButton("Yes", (dialog, which) -> logoutAndNavigateToLogin())
                .setNegativeButton("No", null)
                .show();
    }

    // Fetch violation function for Spinner
    private void fetchViolationsFromAPI() {
        String apiUrl = "https://scholarpassserver-production.up.railway.app/api/config";

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(
                Request.Method.GET,
                apiUrl,
                null,
                response -> {
                    try {
                        JSONArray violationTypesArray = response.getJSONArray("violationTypes");

                        // Create a list to hold the violations
                        ArrayList<String> violationList = new ArrayList<>();

                        // Loop through the array para makuha ang name and severity
                        for (int i = 0; i < violationTypesArray.length(); i++) {
                            JSONObject violationObject = violationTypesArray.getJSONObject(i);
                            violationName = violationObject.getString("name");
                            violationSeverity = violationObject.getString("severity");

                            // Combine name and severity with " - "
                            String violation = violationName + " - " + violationSeverity;
                            violationList.add(violation);
                        }
                        // Set up the ArrayAdapter with the violation list
                        ArrayAdapter<String> violationAdapter = new ArrayAdapter<>(
                                this,
                                android.R.layout.simple_spinner_item,
                                violationList
                        );
                        violationAdapter.setDropDownViewResource(R.layout.spinner_dropdown_item);

                        // Attach the adapter to the Spinner
                        violationSpinner.setAdapter(violationAdapter);

                    } catch (JSONException e) {
                        e.printStackTrace();
                        Toast.makeText(this, "Error parsing violation data", Toast.LENGTH_SHORT).show();
                    }
                },
                error -> {
                    // Handle error
                    Log.e("ViolationActivity", "Error fetching data: " + error.getMessage());
                    Toast.makeText(this, "Failed to fetch violation data", Toast.LENGTH_SHORT).show();
                }
        );

        // Add the request to the Volley queue
        queue.add(jsonObjectRequest);
    }

    // Fetch student data from API
    private void fetchDataFromApi(String studID) {
        String url = "https://scholarpassserver-production.up.railway.app/api/log/logging/student?studentID=" + studID; // URL or API

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, url, null,
                response -> {
                    try {
                        // Parse the student data from response
                        JSONObject studObject = response.getJSONObject("student");

                        // Extract Data
                        String avatarUrl = studObject.getString("pfp");
                        String name = studObject.getString("name");
                        studNo = studObject.getString("studentNumber");
                        String department = studObject.getString("department");

                        // Load student data into views
                        Glide.with(violationResult_page.this).load(avatarUrl).into(avatarImageView);
                        studName.setText(name);
                        studNumber.setText(studNo);
                        departmentTxt.setText(department);

                    } catch (Exception e) {
                        Log.e("result_page", "Error parsing response: " + e.getMessage());
                    }
                },
                error -> {
                    // Handle the error
                    Log.e("result_page", "API request error: " + error.getMessage());
                    Toast.makeText(violationResult_page.this, "Failed to fetch student data.", Toast.LENGTH_SHORT).show();
                });

        // Add the request to the RequestQueue
        queue.add(jsonObjectRequest);
    }

    private void submitViolation() {
        String selectedViolation = violationSpinner.getSelectedItem().toString();
        String[] violationParts = selectedViolation.split(" - ");
        String violationName = violationParts[0];
        String severity = violationParts[1];

        JSONObject postViolation = new JSONObject();
        try {
            postViolation.put("studentID", studentID != null ? studentID : JSONObject.NULL);
            postViolation.put("studentNumber", studentID == null ? studNo : JSONObject.NULL);
            postViolation.put("violation", violationName);
            postViolation.put("severity", severity);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        String apiViolation = "https://scholarpassserver-production.up.railway.app/api/violation";
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST, apiViolation, postViolation,
                response -> {
                    Toast.makeText(violationResult_page.this, "Violation submitted successfully!", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(violationResult_page.this, violation_page.class);
                    startActivity(intent);
                    finish();
                },
                error -> {
                    Toast.makeText(violationResult_page.this, "Error submitting violation: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                });

        queue.add(jsonObjectRequest);
    }

    private void logoutAndNavigateToLogin() {
        // Clear token and remember me preference
        SharedPreferences sharedPreferences = getSharedPreferences("GuardAppPrefs", MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.remove("authToken");
        editor.putBoolean("rememberMe", false);
        editor.apply();

        Intent intent = new Intent(violationResult_page.this, login_page.class);
        startActivity(intent);
        finish();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Intent intent = new Intent(violationResult_page.this, violation_page.class);
        startActivity(intent);
        finish();
    }
}
