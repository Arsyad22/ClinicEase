package com.example.clinicease;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class NurseSettingsActivity extends AppCompatActivity {

    private EditText icNumberEditText, emailEditText, ageEditText, addressEditText, contactNumberEditText;
    private EditText oldPasswordEditText, newPasswordEditText;
    private Button saveDetailsButton, changePasswordButton, logoutButton;

    private FirebaseAuth auth;
    private FirebaseFirestore firestore;
    private FirebaseUser currentUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_nurse_settings);

        // Initialize UI components
        icNumberEditText = findViewById(R.id.icNumberEditText);
        emailEditText = findViewById(R.id.emailEditText);
        ageEditText = findViewById(R.id.ageEditText);
        addressEditText = findViewById(R.id.addressEditText);
        contactNumberEditText = findViewById(R.id.contactNumberEditText);
        oldPasswordEditText = findViewById(R.id.oldPasswordEditText);
        newPasswordEditText = findViewById(R.id.newPasswordEditText);
        saveDetailsButton = findViewById(R.id.saveDetailsButton);
        changePasswordButton = findViewById(R.id.changePasswordButton);
        logoutButton = findViewById(R.id.logoutButton);

        // Initialize Firebase
        auth = FirebaseAuth.getInstance();
        firestore = FirebaseFirestore.getInstance();
        currentUser = auth.getCurrentUser();

        if (currentUser != null) {
            loadUserDetails();
        } else {
            Toast.makeText(this, "User not authenticated", Toast.LENGTH_SHORT).show();
            finish();
        }

        saveDetailsButton.setOnClickListener(v -> updateDetails());
        changePasswordButton.setOnClickListener(v -> changePassword());
        logoutButton.setOnClickListener(v -> logout());
    }

    private void loadUserDetails() {
        String userId = currentUser.getUid();
        DocumentReference userRef = firestore.collection("users").document(userId);

        userRef.get().addOnSuccessListener(documentSnapshot -> {
            if (documentSnapshot.exists()) {
                icNumberEditText.setText(documentSnapshot.getString("ic_number"));
                emailEditText.setText(documentSnapshot.getString("email"));
                ageEditText.setText(documentSnapshot.getString("age"));
                addressEditText.setText(documentSnapshot.getString("address"));
                contactNumberEditText.setText(documentSnapshot.getString("contact_number"));

                // Disable editing for certain fields
                icNumberEditText.setEnabled(false);
                emailEditText.setEnabled(false);
                ageEditText.setEnabled(false);
            } else {
                Toast.makeText(NurseSettingsActivity.this, "User details not found", Toast.LENGTH_SHORT).show();
            }
        }).addOnFailureListener(e -> {
            Log.e("NurseSettings", "Error loading user details: " + e.getMessage());
            Toast.makeText(NurseSettingsActivity.this, "Error loading user details", Toast.LENGTH_SHORT).show();
        });
    }

    private void updateDetails() {
        String contactNumber = contactNumberEditText.getText().toString().trim();
        String address = addressEditText.getText().toString().trim();

        if (TextUtils.isEmpty(contactNumber) || TextUtils.isEmpty(address)) {
            Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        String userId = currentUser.getUid();
        Map<String, Object> userUpdates = new HashMap<>();
        userUpdates.put("contact_number", contactNumber);
        userUpdates.put("address", address);

        firestore.collection("users").document(userId)
                .update(userUpdates)
                .addOnSuccessListener(aVoid -> Toast.makeText(NurseSettingsActivity.this, "Details updated successfully", Toast.LENGTH_SHORT).show())
                .addOnFailureListener(e -> {
                    Log.e("NurseSettings", "Error updating details: " + e.getMessage());
                    Toast.makeText(NurseSettingsActivity.this, "Failed to update details", Toast.LENGTH_SHORT).show();
                });
    }

    private void changePassword() {
        String oldPassword = oldPasswordEditText.getText().toString().trim();
        String newPassword = newPasswordEditText.getText().toString().trim();

        if (TextUtils.isEmpty(oldPassword) || TextUtils.isEmpty(newPassword)) {
            Toast.makeText(this, "Please enter old and new passwords", Toast.LENGTH_SHORT).show();
            return;
        }

        // Re-authenticate the user
        auth.signInWithEmailAndPassword(currentUser.getEmail(), oldPassword)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        // Update password
                        currentUser.updatePassword(newPassword)
                                .addOnCompleteListener(passwordUpdateTask -> {
                                    if (passwordUpdateTask.isSuccessful()) {
                                        Toast.makeText(NurseSettingsActivity.this, "Password changed successfully", Toast.LENGTH_SHORT).show();
                                    } else {
                                        Toast.makeText(NurseSettingsActivity.this, "Failed to change password", Toast.LENGTH_SHORT).show();
                                    }
                                });
                    } else {
                        Toast.makeText(NurseSettingsActivity.this, "Old password is incorrect", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void logout() {
        auth.signOut();
        Toast.makeText(this, "Logged out successfully", Toast.LENGTH_SHORT).show();

        // Redirect to LoginActivity after logout
        Intent intent = new Intent(NurseSettingsActivity.this, LoginActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish(); // Close the current activity
    }
}
