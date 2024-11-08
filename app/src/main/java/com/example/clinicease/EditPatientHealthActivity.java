package com.example.clinicease;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class EditPatientHealthActivity extends AppCompatActivity {

    private EditText weightEditText, heightEditText, bmiEditText, bloodPressureEditText, sugarLevelEditText;
    private Button saveButton;
    private DatabaseReference databaseReference;
    private String patientId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_patient_health);

        // Initialize UI components
        weightEditText = findViewById(R.id.weightEditText);
        heightEditText = findViewById(R.id.heightEditText);
        bmiEditText = findViewById(R.id.bmiEditText);
        bloodPressureEditText = findViewById(R.id.bloodPressureEditText);
        sugarLevelEditText = findViewById(R.id.sugarLevelEditText);
        saveButton = findViewById(R.id.saveButton);

        // Initialize Firebase reference
        databaseReference = FirebaseDatabase.getInstance().getReference();

        // Get patient ID from intent extras
        patientId = getIntent().getStringExtra("PATIENT_ID");

        // Load patient health data
        loadPatientHealthData();

        // Save updated health data
        saveButton.setOnClickListener(v -> saveHealthData());
    }

    private void loadPatientHealthData() {
        databaseReference.child("users").child(patientId).child("health").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String weight = snapshot.child("weight").getValue(String.class);
                String height = snapshot.child("height").getValue(String.class);
                String bmi = snapshot.child("bmi").getValue(String.class);
                String bloodPressure = snapshot.child("blood_pressure").getValue(String.class);
                String sugarLevel = snapshot.child("sugar_level").getValue(String.class);

                // Set the retrieved values to the EditTexts
                weightEditText.setText(weight != null ? weight : "");
                heightEditText.setText(height != null ? height : "");
                bmiEditText.setText(bmi != null ? bmi : "");
                bloodPressureEditText.setText(bloodPressure != null ? bloodPressure : "");
                sugarLevelEditText.setText(sugarLevel != null ? sugarLevel : "");
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(EditPatientHealthActivity.this, "Failed to load data", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void saveHealthData() {
        String weight = weightEditText.getText().toString().trim();
        String height = heightEditText.getText().toString().trim();

        // Calculate BMI if weight and height are provided
        if (!weight.isEmpty() && !height.isEmpty()) {
            try {
                double weightValue = Double.parseDouble(weight);
                double heightValue = Double.parseDouble(height) / 100; // Convert height to meters
                double bmiValue = weightValue / (heightValue * heightValue); // BMI formula
                bmiEditText.setText(String.format("%.2f", bmiValue)); // Display BMI with 2 decimal places
            } catch (NumberFormatException e) {
                Toast.makeText(this, "Invalid weight or height input", Toast.LENGTH_SHORT).show();
                return;
            }
        }

        String bmi = bmiEditText.getText().toString().trim();
        String bloodPressure = bloodPressureEditText.getText().toString().trim();
        String sugarLevel = sugarLevelEditText.getText().toString().trim();

        // Save the updated health data to Firebase
        databaseReference.child("users").child(patientId).child("health").child("weight").setValue(weight);
        databaseReference.child("users").child(patientId).child("health").child("height").setValue(height);
        databaseReference.child("users").child(patientId).child("health").child("bmi").setValue(bmi);
        databaseReference.child("users").child(patientId).child("health").child("blood_pressure").setValue(bloodPressure);
        databaseReference.child("users").child(patientId).child("health").child("sugar_level").setValue(sugarLevel)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Toast.makeText(EditPatientHealthActivity.this, "Health data updated successfully", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(EditPatientHealthActivity.this, "Failed to update health data", Toast.LENGTH_SHORT).show();
                    }
                });
    }
}
