package com.example.bulsuin_out;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.MenuItem;
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

import com.google.android.material.navigation.NavigationView;

public class about_page extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    private DrawerLayout drawerlayout;
    private NavigationView naviView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_about_page);
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

    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.menu_home) {
            Intent intent = new Intent(this, MainActivity.class);
            startActivity(intent);
            finish();
        } else if (id == R.id.menu_about) {

        } else if (id == R.id.menu_logout) {
            showLogoutDialog();
        }

        drawerlayout.closeDrawers();
        return true;
    }

    private void showLogoutDialog() {
        new AlertDialog.Builder(about_page.this)
                .setTitle("Logout")
                .setMessage("Are you sure you want to log out?")
                .setPositiveButton("Yes", (dialog, which) -> logoutAndNavigateToLogin())
                .setNegativeButton("No", null)
                .show();
    }

    private void logoutAndNavigateToLogin() {
        // Clear token and remember me preference
        SharedPreferences sharedPreferences = getSharedPreferences("GuardAppPrefs", MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.remove("authToken");
        editor.putBoolean("rememberMe", false);
        editor.apply();

        Intent intent = new Intent(about_page.this, login_page.class);
        startActivity(intent);
        finish();
    }

    public void onBackPressed() {
        super.onBackPressed();

        Intent intent = new Intent(about_page.this, MainActivity.class);
        startActivity(intent);
        finish();

    }

}