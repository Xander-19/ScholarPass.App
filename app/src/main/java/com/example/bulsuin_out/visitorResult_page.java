package com.example.bulsuin_out;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class visitorResult_page extends AppCompatActivity {

    private ImageView bckButton;
    private String cardNumber, id, status;
    private Button submitBtn;
    private TextInputLayout inputLay1, inputLay2, inputLay3, inputLay4, inputLay5, inputLay6;
    private ConstraintLayout conLay1, conLay2, conLay3, conLay4, conLay5, conLay6;
    private TextView fullNameTxt, agencyTxt, addressTxt, personToVisitTxt, numberTxt, purposeTxt;
    private TextInputEditText fullNameEditText, agencyEditText, addressEditText, personToVisitEditText, numberEditText, purposeEditText;
    private String authToken;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_visitor_result_page);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Initialize the InputText views
        fullNameEditText = findViewById(R.id.fullName);
        agencyEditText = findViewById(R.id.agency);
        addressEditText = findViewById(R.id.address);
        personToVisitEditText = findViewById(R.id.personToVisit);
        numberEditText = findViewById(R.id.number);
        purposeEditText = findViewById(R.id.purpose);
        submitBtn = findViewById(R.id.visiSubmitBtn);

        // Initialize the TextInputLayout views
        inputLay1 = findViewById(R.id.textInputLayout1);
        inputLay2 = findViewById(R.id.textInputLayout2);
        inputLay3 = findViewById(R.id.textInputLayout3);
        inputLay4 = findViewById(R.id.textInputLayout4);
        inputLay5 = findViewById(R.id.textInputLayout5);
        inputLay6 = findViewById(R.id.textInputLayout6);

        // Initialize the Text views
        fullNameTxt = findViewById(R.id.txtViewFn);
        agencyTxt = findViewById(R.id.txtViewAg);
        addressTxt = findViewById(R.id.txtViewAd);
        personToVisitTxt = findViewById(R.id.txtViewPv);
        numberTxt = findViewById(R.id.txtViewNo);
        purposeTxt = findViewById(R.id.txtViewPu);

        // Initialize the Constraints views
        conLay1 = findViewById(R.id.txtContraintsView1);
        conLay2 = findViewById(R.id.txtContraintsView2);
        conLay3 = findViewById(R.id.txtContraintsView3);
        conLay4 = findViewById(R.id.txtContraintsView4);
        conLay5 = findViewById(R.id.txtContraintsView5);
        conLay6 = findViewById(R.id.txtContraintsView6);

        SharedPreferences sharedPreferences = getSharedPreferences("GuardAppPrefs", MODE_PRIVATE);
        authToken = sharedPreferences.getString("authToken", "");

        // Fetch the QR result JSON
        String qrResultJson = getIntent().getStringExtra("QR_RESULT_JSON");

        if (qrResultJson != null) {
            try {
                // Parsing the QR JSON result
                JSONObject qrResultObject = new JSONObject(qrResultJson);
                cardNumber = qrResultObject.optString("cardNumber");
                id = qrResultObject.optString("_id");

                // Check status IN or OUT
                checkVisitorStatus(id);

            } catch (JSONException e) {
                Log.e("visitorResult_page", "Error parsing JSON: " + e.getMessage());
            }
        }

        submitBtn.setOnClickListener(v -> submitVisitor());

        // Back button logic
        bckButton = findViewById(R.id.bckBTN);
        bckButton.setOnClickListener(v -> {
            startActivity(new Intent(getApplicationContext(), visitor_page.class));
            finish();
        });
    }

    // Function to check visitor status
    private void checkVisitorStatus(String id) {
        String apiUrl = "https://scholarpassserver-production.up.railway.app/api/visitor/qr/" + id;

        // Make GET request para macheck yung visitor status
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(
                Request.Method.GET,
                apiUrl,
                null,
                response -> {
                    // Get status IN or OUT
                    status = response.optString("status");

                    if ("IN".equals(status)) {
                        // If IN, allow the user to fill the form
                        visibleFormFields(View.VISIBLE);
                        visibleConstraintsView(View.GONE);
                    } else if ("OUT".equals(status)) {
                        // Populate fields with visitor data if OUT
                        JSONObject visitorLog = response.optJSONObject("visitorLog");
                        if (visitorLog != null) {
                            fullNameTxt.setText(visitorLog.optString("name"));
                            agencyTxt.setText(visitorLog.optString("agency"));
                            addressTxt.setText(visitorLog.optString("address"));
                            personToVisitTxt.setText(visitorLog.optString("personToVisit"));
                            numberTxt.setText(visitorLog.optString("number"));
                            purposeTxt.setText(visitorLog.optString("purpose"));
                        }
                        visibleFormFields(View.GONE);
                        visibleConstraintsView(View.VISIBLE);
                    }
                },
                error -> {
                    Toast.makeText(this, "Error fetching visitor status: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                }
        ) {
            @Override
            public Map<String, String> getHeaders() {
                Map<String, String> headers = new HashMap<>();
                headers.put("Authorization", "Bearer YOUR_TOKEN_HERE"); // Replace with your actual token
                headers.put("Content-Type", "application/json");
                return headers;
            }
        };

        RequestQueue queue = Volley.newRequestQueue(this);
        queue.add(jsonObjectRequest);
    }

    private void visibleConstraintsView(int visibility) {
        conLay1.setVisibility(visibility);
        conLay2.setVisibility(visibility);
        conLay3.setVisibility(visibility);
        conLay4.setVisibility(visibility);
        conLay5.setVisibility(visibility);
        conLay6.setVisibility(visibility);
    }

    private void visibleFormFields(int visibility) {
        inputLay1.setVisibility(visibility);
        inputLay2.setVisibility(visibility);
        inputLay3.setVisibility(visibility);
        inputLay4.setVisibility(visibility);
        inputLay5.setVisibility(visibility);
        inputLay6.setVisibility(visibility);
    }

    // Function to submit visitor data
    private void submitVisitor() {
        if ("OUT".equals(status)) {
            // If the status is OUT, babalik sa visitor_page
            Toast.makeText(this, "Visitor already logged out.", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(visitorResult_page.this, visitor_page.class);
            startActivity(intent);
            finish();
            return;
        }

        String fullName = fullNameEditText.getText().toString();
        String agency = agencyEditText.getText().toString();
        String address = addressEditText.getText().toString();
        String personToVisit = personToVisitEditText.getText().toString();
        String number = numberEditText.getText().toString();
        String purpose = purposeEditText.getText().toString();

        JSONObject postVisitor = new JSONObject();
        try {
            JSONObject visitorData = new JSONObject();
            visitorData.put("name", fullName);
            visitorData.put("agency", agency);
            visitorData.put("address", address);
            visitorData.put("personToVisit", personToVisit);
            visitorData.put("number", number);
            visitorData.put("purpose", purpose);

            postVisitor.put("cardID", id);
            postVisitor.put("visitor", visitorData);

        } catch (JSONException e) {
            e.printStackTrace();
        }

        String apiUrl = "https://scholarpassserver-production.up.railway.app/api/visitor/log";

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(
                Request.Method.POST,
                apiUrl,
                postVisitor,
                response -> {
                    Toast.makeText(this, "Visitor submitted successfully!", Toast.LENGTH_SHORT).show();
                    updateVisitorStatus();
                    Intent intent = new Intent(visitorResult_page.this, visitor_page.class);
                    startActivity(intent);
                    finish();
                },
                error -> {
                    Toast.makeText(this, "Error submitting visitor data: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                }
        ) {
            @Override
            public Map<String, String> getHeaders() {
                Map<String, String> headers = new HashMap<>();
                headers.put("Authorization", authToken);
                headers.put("Content-Type", "application/json");
                return headers;
            }
        };

        RequestQueue queue = Volley.newRequestQueue(this);
        queue.add(jsonObjectRequest);
    }

    // Function to update visitor status
    private void updateVisitorStatus() {
        // Update the status based on current state
        status = "IN".equals(status) ? "OUT" : "IN";
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Intent intent = new Intent(visitorResult_page.this, visitor_page.class);
        startActivity(intent);
        finish();
    }
}
