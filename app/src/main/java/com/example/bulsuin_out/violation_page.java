package com.example.bulsuin_out;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageAnalysis;
import androidx.camera.core.ImageProxy;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.common.util.concurrent.ListenableFuture;
import com.google.mlkit.vision.barcode.BarcodeScanning;
import com.google.mlkit.vision.barcode.common.Barcode;
import com.google.mlkit.vision.common.InputImage;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.concurrent.ExecutionException;

public class violation_page extends AppCompatActivity {

    private PreviewView violationPv;
    private PreviewView violationPv1;
    private EditText studentNoInput;
    private Button submitStudentNo;
    private boolean isDetected = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_violation_page);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        studentNoInput = findViewById(R.id.studentNumberInput);
        submitStudentNo = findViewById(R.id.submitStudentNumber);

        violationPv = findViewById(R.id.violationScanner);
        FrameLayout.LayoutParams params = (FrameLayout.LayoutParams) violationPv.getLayoutParams();
        params.height = (int) (getResources().getDisplayMetrics().heightPixels * 0.33);
        violationPv.setLayoutParams(params);

        violationPv1 = findViewById(R.id.violationScanner1);
        FrameLayout.LayoutParams param = (FrameLayout.LayoutParams) violationPv1.getLayoutParams();
        param.height = (int) (getResources().getDisplayMetrics().heightPixels * 1.0);
        violationPv1.setLayoutParams(param);

        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.CAMERA}, 101);
        } else {
            startCamera();
        }

        // Set the onClickListener for the submit button
        submitStudentNo.setOnClickListener(v -> {
            String studentNumber = studentNoInput.getText().toString().trim();

            if (!studentNumber.isEmpty()) {
                Intent intent = new Intent(violation_page.this, violationResult_page.class);
                intent.putExtra("StudentNumber", studentNumber);
                startActivity(intent);
            } else {
                Toast.makeText(this, "Please enter a Student Number", Toast.LENGTH_SHORT).show();
            }
        });

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 101 && grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            startCamera();
        }
    }

    private void startCamera() {

        ListenableFuture<ProcessCameraProvider> cameraProviderFuture = ProcessCameraProvider.getInstance(this);

        cameraProviderFuture.addListener(() -> {
            try {
                ProcessCameraProvider cameraProvider = cameraProviderFuture.get();
                bindPreview(cameraProvider);
            } catch (ExecutionException | InterruptedException e) {
                e.printStackTrace();
            }
        }, ContextCompat.getMainExecutor(this));
    }

    private void bindPreview(ProcessCameraProvider cameraProvider) {

        Preview preview = new Preview.Builder().build();
        CameraSelector cameraSelector = new CameraSelector.Builder()
                .requireLensFacing(CameraSelector.LENS_FACING_BACK)
                .build();

        ImageAnalysis imageAnalysis = new ImageAnalysis.Builder()
                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                .build();

        imageAnalysis.setAnalyzer(ContextCompat.getMainExecutor(this), imageProxy -> {
            if (!isDetected) {
                scanBarcode(imageProxy);
            } else {
                imageProxy.close();
            }
        });

        cameraProvider.bindToLifecycle(this, cameraSelector, imageAnalysis, preview);
        preview.setSurfaceProvider(violationPv.getSurfaceProvider());
        preview.setSurfaceProvider(violationPv1.getSurfaceProvider());

    }

    private void scanBarcode(ImageProxy imageProxy) {

        @SuppressWarnings("UnsafeOptInUsageError")
        InputImage image = InputImage.fromMediaImage(imageProxy.getImage(), imageProxy.getImageInfo().getRotationDegrees());

        BarcodeScanning.getClient().process(image)
                .addOnSuccessListener(barcodes -> {
                    if (!barcodes.isEmpty()) {
                        Barcode barcode = barcodes.get(0);
                        handleBarcode(barcode);
                        isDetected = true;
                    }
                })
                .addOnFailureListener(e -> Log.e("scanner_page", "QR Code scan failed", e))
                .addOnCompleteListener(task -> imageProxy.close());
    }

    private void handleBarcode(Barcode barcode) {
        Intent intent = new Intent(violation_page.this, violationResult_page.class);
        JSONObject jsonObject = new JSONObject();

        try {
            if (barcode.getValueType() == Barcode.TYPE_TEXT) {
                // Handle any ng qr
                String rawData = barcode.getRawValue();

                String decryptedData = AESDecryptionHelper.decrypt(rawData);

                try {

                    decryptedData = decryptedData.replace("\\", "").replaceFirst("^\"|\"$", "");
                    Log.d("TAG", "handleBarcode: " + decryptedData);

                    JSONObject parsedObject = new JSONObject(decryptedData);
                    String studentID = parsedObject.optString("studentID");
                    String vehicleID = parsedObject.optString("vehicleID");

                    // Add the parsed values to the JSON object
                    jsonObject.put("studentID", studentID);
                    jsonObject.put("vehicleID", vehicleID != null && !vehicleID.equals("null") ? vehicleID : JSONObject.NULL);

                } catch (JSONException e) {
                    // If parsing fails, the rawData might not be a valid JSON string; handle old format
                    String[] lines = rawData.split("\n");
                    for (String line : lines) {
                        String[] keyValue = line.split(":");
                        if (keyValue.length == 2) {
                            String key = keyValue[0].trim();
                            String value = keyValue[1].trim();
                            jsonObject.put(key, value);
                        }
                    }
                }

            } else {
                // Handle other QR types if necessary
                jsonObject.put("type", "UNKNOWN");
                jsonObject.put("value", barcode.getRawValue());
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        // Pass ang JSON data sa result page
        intent.putExtra("QR_RESULT_JSON", jsonObject.toString());
        startActivity(intent);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();

        Intent intent = new Intent(violation_page.this, MainActivity.class);
        startActivity(intent);
        finish();

    }
}