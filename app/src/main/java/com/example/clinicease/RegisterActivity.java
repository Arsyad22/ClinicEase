package com.example.clinicease;

import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class RegisterActivity extends AppCompatActivity {

    // Declare your variables
    private EditText name, email, password, icNumber, address, contact, age;
    private Button registerButton;
    private FirebaseAuth auth;
    private FirebaseFirestore firestore;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        // Initialize Firebase and UI components
        auth = FirebaseAuth.getInstance();
        firestore = FirebaseFirestore.getInstance();
        name = findViewById(R.id.name);
        email = findViewById(R.id.email);
        password = findViewById(R.id.password);
        icNumber = findViewById(R.id.ic_number);
        address = findViewById(R.id.address);
        contact = findViewById(R.id.contact);
        age = findViewById(R.id.age);
        registerButton = findViewById(R.id.registerButton);

        // Handle registration button click
        registerButton.setOnClickListener(v -> registerUser());
    }

    private void registerUser() {
        final String userName = name.getText().toString().trim();
        final String userEmail = email.getText().toString().trim();
        final String userPassword = password.getText().toString().trim();
        final String userIcNumber = icNumber.getText().toString().trim();
        final String userAddress = address.getText().toString().trim();
        final String userContact = contact.getText().toString().trim();
        final String userAge = age.getText().toString().trim();

        // Validate inputs
        if (TextUtils.isEmpty(userName)) {
            name.setError("Name is required");
            name.requestFocus();
            return;
        }

        if (TextUtils.isEmpty(userEmail) || !Patterns.EMAIL_ADDRESS.matcher(userEmail).matches()) {
            email.setError("Please enter a valid email address");
            email.requestFocus();
            return;
        }

        if (TextUtils.isEmpty(userPassword) || userPassword.length() < 6) {
            password.setError("Password must be at least 6 characters");
            password.requestFocus();
            return;
        }

        if (TextUtils.isEmpty(userIcNumber) || userIcNumber.length() != 12) {
            icNumber.setError("IC Number must be 12 digits");
            icNumber.requestFocus();
            return;
        }

        if (TextUtils.isEmpty(userAddress)) {
            address.setError("Address is required");
            address.requestFocus();
            return;
        }

        if (TextUtils.isEmpty(userContact)) {
            contact.setError("Contact number is required");
            contact.requestFocus();
            return;
        }

        if (TextUtils.isEmpty(userAge)) {
            age.setError("Age is required");
            age.requestFocus();
            return;
        }

        // Check if the IC number already exists in Firestore
        firestore.collection("users").whereEqualTo("ic_number", userIcNumber)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && !task.getResult().isEmpty()) {
                        // IC Number already exists
                        icNumber.setError("This IC Number is already registered");
                        icNumber.requestFocus();
                    } else {
                        // IC Number does not exist, proceed with registration
                        auth.createUserWithEmailAndPassword(userEmail, userPassword)
                                .addOnCompleteListener(authTask -> {
                                    if (authTask.isSuccessful()) {
                                        FirebaseUser user = auth.getCurrentUser();
                                        if (user != null) {
                                            String userId = user.getUid();

                                            // Create a map of user data to store in Firestore
                                            Map<String, Object> userData = new HashMap<>();
                                            userData.put("name", userName);
                                            userData.put("email", userEmail);
                                            userData.put("ic_number", userIcNumber);
                                            userData.put("address", userAddress);
                                            userData.put("contact", userContact);
                                            userData.put("age", userAge);
                                            userData.put("role", "Patient");

                                            // Save user details to Firestore
                                            firestore.collection("users").document(userId)
                                                    .set(userData)
                                                    .addOnSuccessListener(aVoid -> {
                                                        Toast.makeText(RegisterActivity.this, "Registration successful!", Toast.LENGTH_SHORT).show();
                                                        // Redirect to patient homepage or login activity
                                                        finish();
                                                    })
                                                    .addOnFailureListener(e -> {
                                                        Toast.makeText(RegisterActivity.this, "Failed to save user data: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                                    });
                                        }
                                    } else {
                                        Toast.makeText(RegisterActivity.this, "Registration failed: " + authTask.getException().getMessage(), Toast.LENGTH_SHORT).show();
                                    }
                                });
                    }
                });
    }
}
