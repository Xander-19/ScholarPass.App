package com.example.bulsuin_out;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
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
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.bumptech.glide.Glide;
import com.google.android.material.navigation.NavigationView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class result_page extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    private DrawerLayout drawerlayout;
    private NavigationView naviView;
    private RequestQueue queue;
    private Button inBtn, outBtn;
    private TextView studName, studNumber, plateNumber, departmentTxt, scheduleResult;
    private ImageView avatarImageView, avatarVehicleImage;
    private String studentID, vehicleID, authToken;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        Log.d("result_page", "Result page started.");
        setContentView(R.layout.activity_result_page);
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

        // Initialize the request queue
        queue = Volley.newRequestQueue(this);

        inBtn = findViewById(R.id.inBtn);
        outBtn = findViewById(R.id.outBtn);
        avatarImageView = findViewById(R.id.avatarImageView);
        avatarVehicleImage = findViewById(R.id.vehicleImageView);
        studName = findViewById(R.id.studName);
        studNumber = findViewById(R.id.studNumResultTxt);
        plateNumber = findViewById(R.id.plateResultTxt);
        departmentTxt = findViewById(R.id.departmentResultTxt);
        scheduleResult = findViewById(R.id.schedResultTxt);

        SharedPreferences sharedPreferences = getSharedPreferences("GuardAppPrefs", MODE_PRIVATE);
        authToken = sharedPreferences.getString("authToken", "");

        String qrResultJson = getIntent().getStringExtra("QR_RESULT_JSON");

        if (qrResultJson != null) {
            try {
                // Parse the JSON string
                JSONObject qrResultObject = new JSONObject(qrResultJson);

                // Extract the studentID and vehicleID
                studentID = qrResultObject.optString("studentID");
                vehicleID = qrResultObject.optString("vehicleID");

                // Call the API with the studentID
                fetchDataFromApi(studentID, vehicleID);

            } catch (JSONException e) {
                Log.e("result_page", "Error parsing JSON: " + e.getMessage());
                Toast.makeText(this, "Error reading QR result.", Toast.LENGTH_SHORT).show();
            }
        } else {
            Log.d("result_page", "No QR code result received.");
            Toast.makeText(this, "No QR code result received.", Toast.LENGTH_SHORT).show();
        }

        inBtn.setOnClickListener(v -> {

            sendStatusUpdate(studentID, vehicleID, "IN");
            
            Intent intent = new Intent(result_page.this, scanner_page.class);
            startActivity(intent);
            finish();
        });

        outBtn.setOnClickListener(v -> {

            sendStatusUpdate(studentID, vehicleID, "OUT");

            Intent intent = new Intent(result_page.this, scanner_page.class);
            startActivity(intent);
            finish();
        });
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
        new AlertDialog.Builder(result_page.this)
                .setTitle("Logout")
                .setMessage("Are you sure you want to log out?")
                .setPositiveButton("Yes", (dialog, which) -> logoutAndNavigateToLogin())
                .setNegativeButton("No", null)
                .show();
    }

    private void sendStatusUpdate(String studentID, String vehicleID, String status) {

        String url = "https://scholarpassserver-production.up.railway.app/api/log/student";

        JSONObject postParams = new JSONObject();
        try {

            postParams.put("studentID", studentID);

            if (vehicleID != null && !vehicleID.equals("null")) {
                postParams.put("vehicleID", vehicleID);
            } else {
                postParams.put("vehicleID", JSONObject.NULL);
            }

            postParams.put("status", status); // "IN" or "OUT"

        } catch (JSONException e) {

            Log.e("result_page", "Error creating JSON: " + e.getMessage());
            Toast.makeText(this, "Failed to create request.", Toast.LENGTH_SHORT).show();
            return;

        }

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(
                Request.Method.POST, url, postParams,
                response -> {
                    // Handle the response from the server
                    Toast.makeText(result_page.this, "Status updated: " + status, Toast.LENGTH_SHORT).show();
                },
                error -> {
                    // Handle errors
                    Log.e("result_page", "Error in request: " + error.getMessage());
                    Toast.makeText(result_page.this, "Failed to update status.", Toast.LENGTH_SHORT).show();
                }) {
            @Override
            public Map<String, String> getHeaders() {
                Map<String, String> headers = new HashMap<>();
                headers.put("Authorization", " " + authToken);
                return headers;

            }
        };

        // Add the request to the RequestQueue
        queue.add(jsonObjectRequest);

    }

    private void fetchDataFromApi(String studentID, String vehicleID) {
        String url = "https://scholarpassserver-production.up.railway.app/api/log/logging/student?studentID=" + studentID;

        if (vehicleID != null && !vehicleID.equals("null")) {
            url += "&vehicleID=" + vehicleID;
        }

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, url, null,
                response -> {
                    try {
                        JSONObject studObject = response.getJSONObject("student");
                        String avatarUrl = studObject.getString("pfp");
                        String name = studObject.getString("name");
                        String studNo = studObject.getString("studentNumber");
                        String department = studObject.getString("department");
                        String scheduleFormatted = parseSchedule(studObject.getJSONArray("schedule"));

                        Glide.with(result_page.this).load(avatarUrl).into(avatarImageView);
                        studName.setText(name);
                        studNumber.setText(studNo);
                        departmentTxt.setText(department);
                        scheduleResult.setText(scheduleFormatted);

                        if (vehicleID != null && !vehicleID.equals("null")) {
                            JSONObject vehicleObject = response.getJSONObject("vehicle");
                            String imageUrl = vehicleObject.getString("image");
                            String plateNum = vehicleObject.getString("plateNumber");

                            Glide.with(result_page.this).load(imageUrl).into(avatarVehicleImage);
                            plateNumber.setText(plateNum);
                        }
                    } catch (JSONException e) {
                        Log.e("result_page", "Error parsing response: " + e.getMessage());
                    }
                },
                error -> {
                    Log.e("result_page", "API request error: " + error.getMessage());
                    Toast.makeText(result_page.this, "Failed to fetch data.", Toast.LENGTH_SHORT).show();
                }) {

            @Override
            public Map<String, String> getHeaders() {
                Map<String, String> headers = new HashMap<>();
                headers.put("Authorization", " " + authToken);
                return headers;

            }
        };

        queue.add(jsonObjectRequest);
    }

    private String parseSchedule(JSONArray scheduleArray) {
        StringBuilder formattedSchedule = new StringBuilder();

        try {
            for (int i = 0; i < scheduleArray.length(); i++) {
                JSONArray classDetails = scheduleArray.getJSONArray(i);

                // Check kung may additional time ng sabject
                if (classDetails.length() > 5 && !classDetails.getString(5).isEmpty()) {
                    formattedSchedule.append(classDetails.getString(0)).append(" and ").append(classDetails.getString(5)).append("\n");
                } else {
                    formattedSchedule.append(classDetails.getString(0)).append("\n");
                }

                // Concatenate then subject name and code
                formattedSchedule.append(classDetails.getString(1)).append(" ").append(classDetails.getString(2)).append("\n");

                // Check kung may teacher
                if (classDetails.length() > 3 && !classDetails.getString(3).isEmpty()) {
                    formattedSchedule.append(classDetails.getString(3)).append("\n");
                } else {
                    formattedSchedule.append("No assigned teacher\n");
                }

                if (classDetails.length() > 4) {
                    formattedSchedule.append(classDetails.getString(4)).append("\n");
                }

                formattedSchedule.append("\n").append("\n");
            }
        } catch (Exception e) {
            Log.e("SCHED", "Error parsing schedule: " + e.getMessage());
            return "Error parsing schedule.";
        }

        return formattedSchedule.toString();
    }

    private void logoutAndNavigateToLogin() {
        // Clear token and remember me preference
        SharedPreferences sharedPreferences = getSharedPreferences("GuardAppPrefs", MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.remove("authToken");
        editor.putBoolean("rememberMe", false);
        editor.apply();

        Intent intent = new Intent(result_page.this, login_page.class);
        startActivity(intent);
        finish();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();

        Intent intent = new Intent(result_page.this, scanner_page.class);
        startActivity(intent);
        finish();

    }
}
