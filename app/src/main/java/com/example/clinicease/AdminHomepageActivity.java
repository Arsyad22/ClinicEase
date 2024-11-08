package com.example.clinicease;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class AdminHomepageActivity extends AppCompatActivity {

    private FirebaseAuth auth;
    private FirebaseFirestore firestore;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_homepage);

        // Initialize Firebase instances
        auth = FirebaseAuth.getInstance();
        firestore = FirebaseFirestore.getInstance();

        // Initialize UI components
        EditText name = findViewById(R.id.name);
        EditText email = findViewById(R.id.email);
        EditText password = findViewById(R.id.password);
        EditText workingId = findViewById(R.id.working_id);
        EditText icNumber = findViewById(R.id.ic_number);
        EditText contactNumber = findViewById(R.id.contact_number);
        Spinner genderSpinner = findViewById(R.id.gender_spinner);

        Button addDoctorButton = findViewById(R.id.addDoctorButton);
        Button addNurseButton = findViewById(R.id.addNurseButton);
        Button logoutButton = findViewById(R.id.logoutButton);

        // Populate the gender spinner
        ArrayAdapter<CharSequence> genderAdapter = ArrayAdapter.createFromResource(this,
                R.array.gender_array, android.R.layout.simple_spinner_item);
        genderAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        genderSpinner.setAdapter(genderAdapter);

        // Set onClickListeners
        addDoctorButton.setOnClickListener(v -> addAccount("Doctor", name, email, password, workingId, icNumber, contactNumber, genderSpinner));
        addNurseButton.setOnClickListener(v -> addAccount("Nurse", name, email, password, workingId, icNumber, contactNumber, genderSpinner));
        logoutButton.setOnClickListener(v -> logout());
    }

    private void logout() {
        auth.signOut();
        Intent intent = new Intent(AdminHomepageActivity.this, LoginActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish();
        Toast.makeText(AdminHomepageActivity.this, "Logged out successfully", Toast.LENGTH_SHORT).show();
    }

    private void addAccount(String role, EditText name, EditText email, EditText password, EditText workingId, EditText icNumber, EditText contactNumber, Spinner genderSpinner) {
        String userName = name.getText().toString().trim();
        String userEmail = email.getText().toString().trim();
        String userPassword = password.getText().toString().trim();
        String userWorkingId = workingId.getText().toString().trim();
        String userGender = genderSpinner.getSelectedItem().toString();
        String userIcNumber = icNumber.getText().toString().trim();
        String userContactNumber = contactNumber.getText().toString().trim();

        if (!validateInputs(userName, userEmail, userPassword, userWorkingId, userIcNumber, userContactNumber, name, email, password, workingId, icNumber, contactNumber)) {
            return;
        }

        auth.createUserWithEmailAndPassword(userEmail, userPassword)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser currentUser = auth.getCurrentUser();
                        if (currentUser != null) {
                            saveUserDataToFirestore(currentUser.getUid(), userName, userEmail, role, userWorkingId, userGender, userIcNumber, userContactNumber);
                        }
                    } else {
                        showError(task.getException());
                    }
                });
    }

    private boolean validateInputs(String userName, String userEmail, String userPassword, String userWorkingId, String userIcNumber, String userContactNumber,
                                   EditText name, EditText email, EditText password, EditText workingId, EditText icNumber, EditText contactNumber) {
        if (TextUtils.isEmpty(userName)) {
            name.setError("Please enter a name");
            name.requestFocus();
            return false;
        }
        if (TextUtils.isEmpty(userEmail) || !Patterns.EMAIL_ADDRESS.matcher(userEmail).matches()) {
            email.setError("Please enter a valid email");
            email.requestFocus();
            return false;
        }
        if (TextUtils.isEmpty(userPassword) || userPassword.length() < 6) {
            password.setError("Password must be at least 6 characters");
            password.requestFocus();
            return false;
        }
        if (TextUtils.isEmpty(userWorkingId)) {
            workingId.setError("Please enter a working ID");
            workingId.requestFocus();
            return false;
        }
        if (TextUtils.isEmpty(userIcNumber)) {
            icNumber.setError("Please enter an IC number");
            icNumber.requestFocus();
            return false;
        }
        if (TextUtils.isEmpty(userContactNumber)) {
            contactNumber.setError("Please enter a contact number");
            contactNumber.requestFocus();
            return false;
        }
        return true;
    }

    private void saveUserDataToFirestore(String userId, String userName, String userEmail, String role, String userWorkingId, String userGender, String userIcNumber, String userContactNumber) {
        Map<String, Object> userData = new HashMap<>();
        userData.put("name", userName);
        userData.put("email", userEmail);
        userData.put("role", role);
        userData.put("working_id", userWorkingId);
        userData.put("gender", userGender);
        userData.put("ic_number", userIcNumber);
        userData.put("contact_number", userContactNumber);

        firestore.collection("users").document(userId)
                .set(userData)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(AdminHomepageActivity.this, role + " added successfully!", Toast.LENGTH_SHORT).show();
                    clearInputFields();
                })
                .addOnFailureListener(e -> Toast.makeText(AdminHomepageActivity.this, "Failed to add " + role + ": " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }

    private void showError(Exception exception) {
        String message = exception != null ? exception.getMessage() : "An error occurred. Please try again.";
        Toast.makeText(AdminHomepageActivity.this, "Error: " + message, Toast.LENGTH_SHORT).show();
    }

    private void clearInputFields() {
        EditText name = findViewById(R.id.name);
        EditText email = findViewById(R.id.email);
        EditText password = findViewById(R.id.password);
        EditText workingId = findViewById(R.id.working_id);
        EditText icNumber = findViewById(R.id.ic_number);
        EditText contactNumber = findViewById(R.id.contact_number);
        Spinner genderSpinner = findViewById(R.id.gender_spinner);

        name.setText("");
        email.setText("");
        password.setText("");
        workingId.setText("");
        icNumber.setText("");
        contactNumber.setText("");
        genderSpinner.setSelection(0);
    }
}
