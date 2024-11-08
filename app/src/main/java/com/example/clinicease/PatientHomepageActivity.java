package com.example.clinicease;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class PatientHomepageActivity extends AppCompatActivity {

    private TextView welcomeTextView, dateTimeTextView, queuePositionTextView, weightTextView,
            heightTextView, bmiTextView, bloodPressureTextView, sugarLevelTextView, announcementTextView;
    private ImageView announcementImageView;

    private DatabaseReference databaseReference;
    private FirebaseFirestore firestore;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_patient_homepage);

        // Initialize UI components
        welcomeTextView = findViewById(R.id.welcomeTextView);
        dateTimeTextView = findViewById(R.id.dateTimeTextView);
        queuePositionTextView = findViewById(R.id.queuePositionTextView);
        weightTextView = findViewById(R.id.weightTextView);
        heightTextView = findViewById(R.id.heightTextView);
        bmiTextView = findViewById(R.id.bmiTextView);
        bloodPressureTextView = findViewById(R.id.bloodPressureTextView);
        sugarLevelTextView = findViewById(R.id.sugarLevelTextView);
        announcementTextView = findViewById(R.id.announcementTextView);
        announcementImageView = findViewById(R.id.announcementImageView);

        databaseReference = FirebaseDatabase.getInstance().getReference();
        firestore = FirebaseFirestore.getInstance();

        // Load data
        loadUserName();
        loadCurrentDateTime();
        loadQueueNumber();
        loadPatientHealthData();
        loadAnnouncement();

        // Bottom Navigation
        setupBottomNavigation();
    }

    // Handle the device back button press
    @Override
    public void onBackPressed() {
        // Minimize the app instead of logging out or going back to login screen
        moveTaskToBack(true);
    }

    private void loadUserName() {
        String userId = FirebaseAuth.getInstance().getUid();
        if (userId != null) {
            firestore.collection("users").document(userId).get().addOnSuccessListener(documentSnapshot -> {
                if (documentSnapshot.exists()) {
                    String name = documentSnapshot.getString("name");
                    welcomeTextView.setText(String.format("Welcome, %s!", name != null ? name : "User"));
                }
            }).addOnFailureListener(e -> {
                Log.e("PatientHomepageActivity", "Failed to load user name: " + e.getMessage());
                Toast.makeText(this, "Failed to load user name", Toast.LENGTH_SHORT).show();
            });
        }
    }

    private void loadCurrentDateTime() {
        SimpleDateFormat sdf = new SimpleDateFormat("EEEE, MMMM dd, yyyy - HH:mm", Locale.getDefault());
        String currentDateAndTime = sdf.format(new Date());
        dateTimeTextView.setText(currentDateAndTime);
    }

    private void loadQueueNumber() {
        DatabaseReference queueRef = databaseReference.child("clinic_queue").child("current_count");

        queueRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Integer queueNumber = snapshot.getValue(Integer.class);
                if (queueNumber != null) {
                    queuePositionTextView.setText(String.format("Queue: %d", queueNumber));
                } else {
                    queuePositionTextView.setText("Queue number not available");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("PatientHomepageActivity", "Failed to load queue number: " + error.getMessage());
                Toast.makeText(PatientHomepageActivity.this, "Queue number is shown", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadPatientHealthData() {
        String userId = FirebaseAuth.getInstance().getUid();
        if (userId == null) {
            Toast.makeText(this, "User not authenticated", Toast.LENGTH_SHORT).show();
            return;
        }

        DatabaseReference healthRef = databaseReference.child("users").child(userId).child("health");

        healthRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String weight = snapshot.child("weight").getValue(String.class);
                String height = snapshot.child("height").getValue(String.class);
                String bmi = snapshot.child("bmi").getValue(String.class);
                String bloodPressure = snapshot.child("blood_pressure").getValue(String.class);
                String sugarLevel = snapshot.child("sugar_level").getValue(String.class);

                weightTextView.setText("Weight: " + (weight != null ? weight + " kg" : "N/A"));
                heightTextView.setText("Height: " + (height != null ? height + " cm" : "N/A"));
                bmiTextView.setText("BMI: " + (bmi != null ? bmi : "N/A"));
                bloodPressureTextView.setText("Blood Pressure: " + (bloodPressure != null ? bloodPressure : "N/A"));
                sugarLevelTextView.setText("Sugar Level: " + (sugarLevel != null ? sugarLevel : "N/A"));
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("PatientHomepageActivity", "Failed to load health data: " + error.getMessage());
                Toast.makeText(PatientHomepageActivity.this, "Failed to load health data", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadAnnouncement() {
        firestore.collection("announcements")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (!queryDocumentSnapshots.isEmpty()) {
                        for (DocumentSnapshot announcementDoc : queryDocumentSnapshots) {
                            String announcementText = announcementDoc.getString("text");
                            String announcementImageUrl = announcementDoc.getString("imageUri");

                            Log.d("PatientHomepageActivity", "Announcement Text: " + announcementText);
                            Log.d("PatientHomepageActivity", "Announcement Image URL: " + announcementImageUrl);

                            announcementTextView.setText(announcementText != null ? announcementText : "No announcements available");

                            if (announcementImageUrl != null) {
                                Glide.with(PatientHomepageActivity.this)
                                        .load(announcementImageUrl)
                                        .into(announcementImageView);
                            } else {
                                announcementImageView.setImageDrawable(null);
                            }
                        }
                    } else {
                        Log.d("PatientHomepageActivity", "No announcements found");
                        announcementTextView.setText("No announcements available");
                        announcementImageView.setImageDrawable(null);
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e("PatientHomepageActivity", "Failed to load announcement: " + e.getMessage());
                    announcementTextView.setText("No announcements available");
                    announcementImageView.setImageDrawable(null);
                    Toast.makeText(PatientHomepageActivity.this, "Failed to load announcement", Toast.LENGTH_SHORT).show();
                });
    }

    private void setupBottomNavigation() {
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottomNavigationView);
        bottomNavigationView.setOnNavigationItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_home) {
                Toast.makeText(PatientHomepageActivity.this, "Home clicked", Toast.LENGTH_SHORT).show();
                return true;
            } else if (id == R.id.nav_appointment) {
                startActivity(new Intent(PatientHomepageActivity.this, BookAppointmentActivity.class));
                return true;
            } else if (id == R.id.nav_history) {
                startActivity(new Intent(PatientHomepageActivity.this, HistoryActivity.class));
                return true;
            } else if (id == R.id.nav_settings) {
                startActivity(new Intent(PatientHomepageActivity.this, SettingsActivity.class));
                return true;
            }
            return false;
        });
    }
}
