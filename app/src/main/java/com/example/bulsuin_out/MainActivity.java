package com.example.bulsuin_out;

import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.Toolbar;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.material.navigation.NavigationView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    private DrawerLayout drawerlayout;
    private NavigationView naviView;
    private TextView currVehicle;
    private ImageView refreshIcon;
    private RecyclerView recyclerView;
    private Button visitorpage, violationpage, scannerpage;
    private EntryAdapter recentEntry;
    private List<Entry> entryList = new ArrayList<>();

    @SuppressLint("WrongViewCast")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
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

        //Para sa Current Vehicle
        currVehicle = findViewById(R.id.currentVehicle);
        fetchCurrentVehicle();
        //Pang refresh sa current vehicle
        refreshIcon = findViewById(R.id.refresh);
        refreshIcon.setOnClickListener(v -> {
            Toast.makeText(MainActivity.this, "Refreshing Current Vehicle", Toast.LENGTH_SHORT).show();
            fetchCurrentVehicle();
        });

        visitorpage = findViewById(R.id.btn_record_visitor);
        violationpage = findViewById(R.id.btn_record_violation);
        scannerpage = findViewById(R.id.btn_qr_code);

        // Recycleview setup
        recyclerView = findViewById(R.id.recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recentEntry = new EntryAdapter(entryList);
        recyclerView.setAdapter(recentEntry);
        // Fetch data from API
        fetchData();

        visitorpage.setOnClickListener(v -> startActivity(new Intent(getApplicationContext(), visitor_page.class)));
        violationpage.setOnClickListener(v -> startActivity(new Intent(getApplicationContext(), violation_page.class)));
        scannerpage.setOnClickListener(v -> startActivity(new Intent(getApplicationContext(), scanner_page.class)));

    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.menu_home) {

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
        new AlertDialog.Builder(MainActivity.this)
                .setTitle("Logout")
                .setMessage("Are you sure you want to log out?")
                .setPositiveButton("Yes", (dialog, which) -> logoutAndNavigateToLogin())
                .setNegativeButton("No", null)
                .show();
    }

    private void fetchCurrentVehicle() {
        String API_URL = "https://scholarpassserver-production.up.railway.app/api/log/vehicle-number";
        RequestQueue queue = Volley.newRequestQueue(this);

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(
                Request.Method.GET,
                API_URL,
                null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            // Get the "count" field from the JSON
                            int vehicleCount = response.getInt("count");

                            currVehicle.setText(String.valueOf(vehicleCount));

                        } catch (JSONException e) {
                            e.printStackTrace();
                            Toast.makeText(MainActivity.this, "Error parsing vehicle count", Toast.LENGTH_SHORT).show();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.e("API_ERROR", "Error fetching vehicle count: " + error.getMessage());
                        Toast.makeText(MainActivity.this, "Error fetching vehicle count", Toast.LENGTH_SHORT).show();
                    }
                }
        );

        // Add the request to the RequestQueue
        queue.add(jsonObjectRequest);
    }


    private void fetchData() {

        String API_URL = "https://scholarpassserver-production.up.railway.app/api/log/logs?limit=25";

        RequestQueue queue = Volley.newRequestQueue(this);

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(
                Request.Method.GET,
                API_URL,
                null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            // Get "docs" array from response
                            JSONArray docs = response.getJSONArray("docs");
                            for (int i = 0; i < docs.length(); i++) {
                                JSONObject log = docs.getJSONObject(i);
                                JSONObject student = log.optJSONObject("studentID");
                                JSONObject visitor = log.optJSONObject("visitorID");

                                String name;
                                String role;
                                String departmentOrPurpose;
                                String vehicleModel;

                                // Determine role and related info
                                if (student != null) {
                                    name = student.getString("name");
                                    role = "Student";

                                    // Abbreviate department name
                                    String department = student.getString("department");
                                    departmentOrPurpose = abbreviateDepartment(department);

                                } else if (visitor != null) {
                                    name = visitor.getString("name");
                                    role = "Visitor";
                                    departmentOrPurpose = visitor.getString("purpose");
                                } else {
                                    continue; // Skip invalid entries
                                }

                                // Check vehicle or set as "Walk-in"
                                JSONObject vehicle = log.optJSONObject("vehicle");
                                if (vehicle != null) {
                                    vehicleModel = vehicle.getString("model");
                                } else {
                                    vehicleModel = "Walk-in";
                                }

                                // Add the log entry to the list
                                entryList.add(new Entry(name, role, departmentOrPurpose, vehicleModel));
                            }

                            // Notify the adapter that the data has changed
                            recentEntry.notifyDataSetChanged();

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.e("API_ERROR", "Error fetching data: " + error.getMessage());
                    }
                }
        );

        // Add the request to the RequestQueue
        queue.add(jsonObjectRequest);

    }

    private String abbreviateDepartment(String department) {
        // List of words to exclude from abbreviation
        String[] excludeWords = {"of", "and", "the", "in"};
        List<String> excludeList = Arrays.asList(excludeWords);

        StringBuilder abbreviation = new StringBuilder();
        String[] words = department.split(" ");

        for (String word : words) {
            // Skip if the word is in the exclusion list
            if (!excludeList.contains(word.toLowerCase()) && !word.isEmpty()) {
                abbreviation.append(word.charAt(0));
            }
        }

        return abbreviation.toString().toUpperCase();

    }

    @SuppressLint("MissingSuperCall")
    @Override
    public void onBackPressed() {
        // Create an alert dialog for logout confirmation
        new AlertDialog.Builder(MainActivity.this)
                .setTitle("Logout")
                .setMessage("Are you sure you want to log out?")
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        logoutAndNavigateToLogin();
                    }
                })
                .setNegativeButton("No", null)
                .show();
    }

    // Logout function to clear preferences and navigate to login screen
    private void logoutAndNavigateToLogin() {
        // Clear token and remember me preference
        SharedPreferences sharedPreferences = getSharedPreferences("GuardAppPrefs", MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.remove("authToken");
        editor.putBoolean("rememberMe", false);
        editor.apply();

        Intent intent = new Intent(MainActivity.this, login_page.class);
        startActivity(intent);
        finish();
    }
}