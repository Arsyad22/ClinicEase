// EditPatientDetailsActivity.java
package com.example.clinicease;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class EditPatientDetailsActivity extends AppCompatActivity {

    private TextView nameTextView, icNumberTextView, bmiTextView;
    private EditText weightEditText, heightEditText, bloodPressureEditText, sugarLevelEditText;
    private Button saveButton;

    private DatabaseReference databaseReference;
    private String patientId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_patient_details);

        // Get the patient ID from the intent
        patientId = getIntent().getStringExtra("PATIENT_ID");

        // Initialize Firebase reference
        databaseReference = FirebaseDatabase.getInstance().getReference("users").child(patientId);

        // Initialize UI components
        nameTextView = findViewById(R.id.nameTextView);
        icNumberTextView = findViewById(R.id.icNumberTextView);
        bmiTextView = findViewById(R.id.bmiTextView);
        weightEditText = findViewById(R.id.weightEditText);
        heightEditText = findViewById(R.id.heightEditText);
        bloodPressureEditText = findViewById(R.id.bloodPressureEditText);
        sugarLevelEditText = findViewById(R.id.sugarLevelEditText);
        saveButton = findViewById(R.id.saveButton);

        // Load patient data
        loadPatientDetails();

        // Save button action
        saveButton.setOnClickListener(v -> savePatientDetails());
    }

    private void loadPatientDetails() {
        databaseReference.get().addOnSuccessListener(dataSnapshot -> {
            String name = dataSnapshot.child("name").getValue(String.class);
            String icNumber = dataSnapshot.child("ic_number").getValue(String.class);
            String weight = dataSnapshot.child("health").child("weight").getValue(String.class);
            String height = dataSnapshot.child("health").child("height").getValue(String.class);
            String bloodPressure = dataSnapshot.child("health").child("blood_pressure").getValue(String.class);
            String sugarLevel = dataSnapshot.child("health").child("sugar_level").getValue(String.class);

            // Set patient details in UI (registration details as read-only)
            nameTextView.setText(name);
            icNumberTextView.setText(icNumber);
            weightEditText.setText(weight);
            heightEditText.setText(height);
            bloodPressureEditText.setText(bloodPressure);
            sugarLevelEditText.setText(sugarLevel);

            // Calculate and display BMI
            if (weight != null && height != null) {
                calculateAndSetBMI(weight, height);
            }
        });
    }

    private void savePatientDetails() {
        String updatedWeight = weightEditText.getText().toString().trim();
        String updatedHeight = heightEditText.getText().toString().trim();
        String updatedBloodPressure = bloodPressureEditText.getText().toString().trim();
        String updatedSugarLevel = sugarLevelEditText.getText().toString().trim();

        // Update Firebase with the new health details
        databaseReference.child("health").child("weight").setValue(updatedWeight);
        databaseReference.child("health").child("height").setValue(updatedHeight);
        databaseReference.child("health").child("blood_pressure").setValue(updatedBloodPressure);
        databaseReference.child("health").child("sugar_level").setValue(updatedSugarLevel);

        // Recalculate BMI based on new weight and height
        if (!updatedWeight.isEmpty() && !updatedHeight.isEmpty()) {
            calculateAndSetBMI(updatedWeight, updatedHeight);
        }

        Toast.makeText(EditPatientDetailsActivity.this, "Patient health details updated", Toast.LENGTH_SHORT).show();
        finish();  // Close activity after saving
    }

    private void calculateAndSetBMI(String weight, String height) {
        try {
            double weightValue = Double.parseDouble(weight);
            double heightValue = Double.parseDouble(height) / 100;  // Convert cm to meters

            if (heightValue > 0) {
                double bmi = weightValue / (heightValue * heightValue);
                bmiTextView.setText(String.format("BMI: %.2f", bmi));
                databaseReference.child("health").child("bmi").setValue(String.valueOf(bmi));  // Save BMI to Firebase
            }
        } catch (NumberFormatException e) {
            bmiTextView.setText("BMI: N/A");
        }
    }
}
