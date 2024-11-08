package com.example.clinicease;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class PatientDetailsActivity extends AppCompatActivity {

    private EditText notesEditText, prescriptionEditText;
    private Button submitButton;
    private FirebaseFirestore firestore;
    private DatabaseReference databaseReference;
    private String icNumber;
    private String userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_patient_details);

        // Initialize UI components
        notesEditText = findViewById(R.id.notesEditText);
        prescriptionEditText = findViewById(R.id.prescriptionEditText);
        submitButton = findViewById(R.id.submitButton);

        // Initialize Firebase references
        firestore = FirebaseFirestore.getInstance();
        databaseReference = FirebaseDatabase.getInstance().getReference();

        // Get the IC number from intent
        icNumber = getIntent().getStringExtra("IC_NUMBER");
        if (icNumber == null) {
            Toast.makeText(this, "Error: IC Number not found", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        Log.d("PatientDetails", "IC Number received: " + icNumber);

        // Find the userId from Firestore based on the IC number
        findUserIdByICNumber();

        // Submit notes and update queue
        submitButton.setOnClickListener(v -> {
            savePrescriptionAndNotes();
        });
    }

    private void findUserIdByICNumber() {
        firestore.collection("users")
                .whereEqualTo("ic_number", icNumber)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    if (!querySnapshot.isEmpty()) {
                        userId = querySnapshot.getDocuments().get(0).getId();
                        Log.d("PatientDetails", "User ID found: " + userId);
                    } else {
                        Toast.makeText(this, "No user found with this IC number", Toast.LENGTH_SHORT).show();
                        finish();
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e("PatientDetails", "Error finding user: " + e.getMessage());
                    Toast.makeText(this, "Failed to find user", Toast.LENGTH_SHORT).show();
                    finish();
                });
    }

    private void savePrescriptionAndNotes() {
        String notes = notesEditText.getText().toString().trim();
        String prescription = prescriptionEditText.getText().toString().trim();
        String currentDate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(new Date());

        if (notes.isEmpty() || prescription.isEmpty()) {
            Toast.makeText(this, "Please fill in both notes and prescription", Toast.LENGTH_SHORT).show();
            return;
        }

        Map<String, Object> details = new HashMap<>();
        details.put("notes", notes);
        details.put("prescription", prescription);
        details.put("timestamp", currentDate);

        // Save details to Firestore under the user's checkup_notes collection
        firestore.collection("users")
                .document(userId)
                .collection("checkup_notes")
                .add(details)
                .addOnSuccessListener(documentReference -> {
                    Toast.makeText(this, "Details saved successfully", Toast.LENGTH_SHORT).show();
                    updateQueueAndRemovePatient();
                })
                .addOnFailureListener(e -> {
                    Log.e("Firestore", "Failed to save details: " + e.getMessage());
                    Toast.makeText(this, "Failed to save details", Toast.LENGTH_SHORT).show();
                });
    }

    private void updateQueueAndRemovePatient() {
        DatabaseReference queueRef = databaseReference.child("clinic_queue");

        // Decrease queue count
        queueRef.child("current_count").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Long currentCount = snapshot.getValue(Long.class);
                if (currentCount != null && currentCount > 0) {
                    queueRef.child("current_count").setValue(currentCount - 1);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("QueueUpdate", "Failed to update queue count: " + error.getMessage());
            }
        });

        // Remove patient from queue
        queueRef.child("patients_in_queue").child(icNumber).removeValue()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Toast.makeText(this, "Patient removed from queue", Toast.LENGTH_SHORT).show();
                        finish(); // Close the activity after successful queue update
                    } else {
                        Log.e("QueueUpdate", "Failed to remove patient from queue");
                    }
                });
    }
}
