package com.example.clinicease;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

public class LoginActivity extends AppCompatActivity {

    private EditText email, password;
    private Button loginButton;
    private FirebaseAuth auth;
    private FirebaseFirestore firestore;
    // Hardcoded admin credentials
    private final String ADMIN_EMAIL = "admin@gmail.com";
    private final String ADMIN_PASSWORD = "cliniceaseadmin";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // Firebase instances
        auth = FirebaseAuth.getInstance();
        firestore = FirebaseFirestore.getInstance();

        // Initialize UI elements
        email = findViewById(R.id.email);
        password = findViewById(R.id.password);
        loginButton = findViewById(R.id.loginButton);

        // Handle login button click
        loginButton.setOnClickListener(v -> loginUser());
    }

    private void loginUser() {
        String userEmail = email.getText().toString().trim();
        String userPassword = password.getText().toString().trim();

        // Validate inputs
        if (TextUtils.isEmpty(userEmail) || !Patterns.EMAIL_ADDRESS.matcher(userEmail).matches()) {
            email.setError("Please enter a valid email");
            email.requestFocus();
            return;
        }

        if (TextUtils.isEmpty(userPassword)) {
            password.setError("Please enter your password");
            password.requestFocus();
            return;
        }

        // Check if the admin credentials are hardcoded
        if (userEmail.equals(ADMIN_EMAIL) && userPassword.equals(ADMIN_PASSWORD)) {
            // Redirect to Admin Homepage
            Toast.makeText(this, "Admin Login Successful", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(LoginActivity.this, AdminHomepageActivity.class));
            finish();
            return;
        }

        // Log in the user for other roles
        auth.signInWithEmailAndPassword(userEmail, userPassword)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        // Get the current user
                        FirebaseUser user = auth.getCurrentUser();
                        if (user != null) {
                            // Get the user ID
                            String userId = user.getUid();

                            // Check the user's role from Firestore
                            firestore.collection("users").document(userId)
                                    .get()
                                    .addOnCompleteListener(task1 -> {
                                        if (task1.isSuccessful()) {
                                            DocumentSnapshot document = task1.getResult();
                                            if (document.exists()) {
                                                // Get the user's role
                                                String role = document.getString("role");

                                                // Redirect based on the role
                                                switch (role) {
                                                    case "Patient":
                                                        Intent patientIntent = new Intent(LoginActivity.this, PatientHomepageActivity.class);
                                                        startActivity(patientIntent);
                                                        finish();  // Close login activity
                                                        break;

                                                    case "Doctor":
                                                        Intent doctorIntent = new Intent(LoginActivity.this, DoctorHomepageActivity.class);
                                                        startActivity(doctorIntent);
                                                        finish();  // Close login activity
                                                        break;

                                                    case "Nurse":
                                                        Intent nurseIntent = new Intent(LoginActivity.this, NurseHomepageActivity.class);
                                                        startActivity(nurseIntent);
                                                        finish();  // Close login activity
                                                        break;

                                                    default:
                                                        Toast.makeText(LoginActivity.this, "Access restricted or invalid role.", Toast.LENGTH_SHORT).show();
                                                        break;
                                                }
                                            } else {
                                                Toast.makeText(LoginActivity.this, "No user data found", Toast.LENGTH_SHORT).show();
                                            }
                                        } else {
                                            Toast.makeText(LoginActivity.this, "Failed to retrieve user data", Toast.LENGTH_SHORT).show();
                                        }
                                    });
                        }
                    } else {
                        Toast.makeText(LoginActivity.this, "Login failed. Please try again.", Toast.LENGTH_SHORT).show();
                    }
                });
    }
}

