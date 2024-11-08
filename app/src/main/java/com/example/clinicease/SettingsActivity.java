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

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class SettingsActivity extends AppCompatActivity {

    private EditText editTextName, editTextIcNumber, editTextEmail, editTextAge, editTextAddress, editTextContact, editTextOldPassword, editTextNewPassword;
    private Button buttonSaveDetails, buttonChangePassword, buttonLogout;
    private FirebaseAuth auth;
    private FirebaseFirestore firestore;
    private FirebaseUser currentUser;
    private DocumentReference userDocRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        // Initialize Firebase instances
        auth = FirebaseAuth.getInstance();
        firestore = FirebaseFirestore.getInstance();
        currentUser = auth.getCurrentUser();

        // Initialize UI components
        editTextName = findViewById(R.id.editTextName);
        editTextIcNumber = findViewById(R.id.editTextIcNumber);
        editTextEmail = findViewById(R.id.editTextEmail);
        editTextAge = findViewById(R.id.editTextAge);
        editTextAddress = findViewById(R.id.editTextAddress);
        editTextContact = findViewById(R.id.editTextContact);
        editTextOldPassword = findViewById(R.id.editTextOldPassword);
        editTextNewPassword = findViewById(R.id.editTextNewPassword);
        buttonSaveDetails = findViewById(R.id.buttonSaveDetails);
        buttonChangePassword = findViewById(R.id.buttonChangePassword);
        buttonLogout = findViewById(R.id.buttonLogout);

        // Load patient data
        if (currentUser != null) {
            userDocRef = firestore.collection("users").document(currentUser.getUid());
            loadUserData();
        }

        // Save Details
        buttonSaveDetails.setOnClickListener(v -> saveUserDetails());

        // Change Password
        buttonChangePassword.setOnClickListener(v -> changePassword());

        // Logout
        buttonLogout.setOnClickListener(v -> logout());
    }

    // Load the user data from Firestore
    private void loadUserData() {
        userDocRef.get().addOnSuccessListener(documentSnapshot -> {
            if (documentSnapshot.exists()) {
                String name = documentSnapshot.getString("name");
                String icNumber = documentSnapshot.getString("ic_number");
                String email = documentSnapshot.getString("email");
                String age = documentSnapshot.getString("age");
                String address = documentSnapshot.getString("address");
                String contact = documentSnapshot.getString("contact");

                editTextName.setText(name);
                editTextIcNumber.setText(icNumber);
                editTextEmail.setText(email);
                editTextAge.setText(age);
                editTextAddress.setText(address);
                editTextContact.setText(contact);

                // Disable editing for view-only fields
                editTextName.setEnabled(false);
                editTextIcNumber.setEnabled(false);
                editTextEmail.setEnabled(false);
                editTextAge.setEnabled(false);
            } else {
                Toast.makeText(SettingsActivity.this, "Failed to load user data.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    // Save the updated user details to Firestore
    private void saveUserDetails() {
        String address = editTextAddress.getText().toString().trim();
        String contact = editTextContact.getText().toString().trim();

        if (TextUtils.isEmpty(address) || TextUtils.isEmpty(contact)) {
            Toast.makeText(this, "Address and Contact fields are required.", Toast.LENGTH_SHORT).show();
            return;
        }

        Map<String, Object> updates = new HashMap<>();
        updates.put("address", address);
        updates.put("contact", contact);

        userDocRef.update(updates)
                .addOnSuccessListener(aVoid -> Toast.makeText(SettingsActivity.this, "Details updated successfully.", Toast.LENGTH_SHORT).show())
                .addOnFailureListener(e -> Toast.makeText(SettingsActivity.this, "Failed to update details.", Toast.LENGTH_SHORT).show());
    }

    // Change the password
    private void changePassword() {
        String oldPassword = editTextOldPassword.getText().toString().trim();
        String newPassword = editTextNewPassword.getText().toString().trim();

        if (TextUtils.isEmpty(oldPassword) || TextUtils.isEmpty(newPassword)) {
            Toast.makeText(this, "Both old and new passwords are required.", Toast.LENGTH_SHORT).show();
            return;
        }

        AuthCredential credential = EmailAuthProvider.getCredential(currentUser.getEmail(), oldPassword);
        currentUser.reauthenticate(credential).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                currentUser.updatePassword(newPassword).addOnCompleteListener(task1 -> {
                    if (task1.isSuccessful()) {
                        Toast.makeText(SettingsActivity.this, "Password changed successfully.", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(SettingsActivity.this, "Failed to change password.", Toast.LENGTH_SHORT).show();
                    }
                });
            } else {
                Toast.makeText(SettingsActivity.this, "Authentication failed.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    // Logout the user
    private void logout() {
        auth.signOut();
        startActivity(new Intent(SettingsActivity.this, LoginActivity.class));
        finish();
    }
}
