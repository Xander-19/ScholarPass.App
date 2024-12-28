package com.example.bulsuin_out;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.VideoView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class loading_page extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_loading_page);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        VideoView videoView = findViewById(R.id.LoadingVideo);
        Uri video = Uri.parse("android.resource://" + getPackageName() + "/" + R.raw.bulsu);

        videoView.setVideoURI(video);
        videoView.setOnCompletionListener(mp -> {
            // After video completes, go to the next activity (main activity or loading screen)
            Intent intent = new Intent(loading_page.this, login_page.class);
            startActivity(intent);
            finish();
        });

        videoView.start();

    }
}