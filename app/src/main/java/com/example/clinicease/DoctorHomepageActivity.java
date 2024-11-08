package com.example.clinicease;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class DoctorHomepageActivity extends AppCompatActivity {

    private LinearLayout checkedInPatientsLayout;
    private Button logoutButton;
    private DatabaseReference databaseReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_doctor_homepage);

        // Initialize UI components
        checkedInPatientsLayout = findViewById(R.id.checkedInPatientsLayout);
        logoutButton = findViewById(R.id.logoutButton);

        // Initialize Firebase reference
        databaseReference = FirebaseDatabase.getInstance().getReference();

        loadCheckedInPatients();

        logoutButton.setOnClickListener(v -> {
            FirebaseAuth.getInstance().signOut();
            Intent intent = new Intent(DoctorHomepageActivity.this, LoginActivity.class);
            startActivity(intent);
            finish();
        });
    }

    private void loadCheckedInPatients() {
        DatabaseReference queueRef = databaseReference.child("clinic_queue").child("patients_in_queue");

        queueRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                checkedInPatientsLayout.removeAllViews();

                if (snapshot.exists()) {
                    for (DataSnapshot patientSnapshot : snapshot.getChildren()) {
                        String icNumber = patientSnapshot.getKey();
                        String patientName = patientSnapshot.child("name").getValue(String.class);

                        if (patientName != null) {
                            TextView patientTextView = new TextView(DoctorHomepageActivity.this);
                            patientTextView.setText(patientName);
                            patientTextView.setTextSize(18);
                            patientTextView.setPadding(16, 16, 16, 16);
                            patientTextView.setOnClickListener(v -> openPatientDetails(icNumber));
                            checkedInPatientsLayout.addView(patientTextView);
                        }
                    }
                } else {
                    Toast.makeText(DoctorHomepageActivity.this, "No checked-in patients available", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("DoctorHomepage", "Failed to load queue: " + error.getMessage());
            }
        });
    }

    private void openPatientDetails(String icNumber) {
        Intent intent = new Intent(DoctorHomepageActivity.this, PatientDetailsActivity.class);
        intent.putExtra("IC_NUMBER", icNumber);
        startActivity(intent);
    }
}
