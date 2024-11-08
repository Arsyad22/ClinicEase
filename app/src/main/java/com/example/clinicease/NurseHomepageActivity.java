package com.example.clinicease;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.Gravity;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class NurseHomepageActivity extends AppCompatActivity {

    private static final int PICK_IMAGE = 100;
    private TextView welcomeTextView, dateTimeTextView, patientListTitleTextView;
    private ImageView imagePreview;
    private EditText announcementTextInput;
    private Button chooseImageButton, publishAnnouncementButton;
    private LinearLayout patientListLayout;
    private FirebaseAuth auth;
    private FirebaseFirestore firestore;
    private FirebaseStorage firebaseStorage;
    private Uri imageUri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_nurse_homepage);

        // Initialize Firebase instances
        auth = FirebaseAuth.getInstance();
        firestore = FirebaseFirestore.getInstance();
        firebaseStorage = FirebaseStorage.getInstance();

        // Initialize UI components
        welcomeTextView = findViewById(R.id.welcomeTextView);
        dateTimeTextView = findViewById(R.id.dateTimeTextView);
        patientListTitleTextView = findViewById(R.id.patientListTitleTextView);
        patientListLayout = findViewById(R.id.patientListLayout);
        chooseImageButton = findViewById(R.id.chooseImageButton);
        publishAnnouncementButton = findViewById(R.id.publishAnnouncementButton);
        imagePreview = findViewById(R.id.imagePreview);
        announcementTextInput = findViewById(R.id.announcementTextInput);

        // Load nurse name, date, and patient list
        loadNurseName();
        loadCurrentDateTime();
        loadPatientList();

        // Open gallery to choose an image
        chooseImageButton.setOnClickListener(v -> openGallery());

        // Publish an announcement
        publishAnnouncementButton.setOnClickListener(v -> publishAnnouncement());

        // Initialize bottom navigation
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottomNavigationView);
        bottomNavigationView.setOnNavigationItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_home) {
                Toast.makeText(NurseHomepageActivity.this, "Home clicked", Toast.LENGTH_SHORT).show();
                return true;
            } else if (id == R.id.nav_history) {
                startActivity(new Intent(NurseHomepageActivity.this, NurseHistoryActivity.class));
                return true;
            } else if (id == R.id.nav_settings) {
                startActivity(new Intent(NurseHomepageActivity.this, NurseSettingsActivity.class));
                return true;
            } else if (id == R.id.nav_check_in) {
                startActivity(new Intent(NurseHomepageActivity.this, CheckInActivity.class));
                return true;
            } else {
                return false;
            }
        });
    }

    private void openGallery() {
        Intent gallery = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.INTERNAL_CONTENT_URI);
        startActivityForResult(gallery, PICK_IMAGE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK && requestCode == PICK_IMAGE) {
            imageUri = data.getData();
            imagePreview.setVisibility(ImageView.VISIBLE); // Show image preview
            imagePreview.setImageURI(imageUri);
        }
    }

    private void publishAnnouncement() {
        String announcementText = announcementTextInput.getText().toString().trim();
        if (announcementText.isEmpty()) {
            Toast.makeText(this, "Please enter announcement text", Toast.LENGTH_SHORT).show();
            return;
        }

        if (imageUri == null) {
            Toast.makeText(this, "Please choose an image", Toast.LENGTH_SHORT).show();
            return;
        }

        // Save announcement to Firebase Storage and Firestore
        String announcementId = firestore.collection("announcements").document().getId();
        StorageReference imageRef = firebaseStorage.getReference().child("announcements/" + announcementId);
        imageRef.putFile(imageUri).addOnSuccessListener(taskSnapshot ->
                imageRef.getDownloadUrl().addOnSuccessListener(uri -> {
                    Map<String, Object> announcement = new HashMap<>();
                    announcement.put("text", announcementText);
                    announcement.put("imageUri", uri.toString());

                    firestore.collection("announcements").document(announcementId).set(announcement)
                            .addOnSuccessListener(aVoid -> {
                                Toast.makeText(NurseHomepageActivity.this, "Announcement published!", Toast.LENGTH_SHORT).show();
                                announcementTextInput.setText("");  // Clear input
                                imagePreview.setVisibility(ImageView.GONE); // Hide image preview
                            })
                            .addOnFailureListener(e -> Toast.makeText(NurseHomepageActivity.this, "Failed to publish announcement", Toast.LENGTH_SHORT).show());
                }).addOnFailureListener(e -> Toast.makeText(NurseHomepageActivity.this, "Failed to upload image", Toast.LENGTH_SHORT).show())
        ).addOnFailureListener(e -> Toast.makeText(NurseHomepageActivity.this, "Image upload failed", Toast.LENGTH_SHORT).show());
    }

    private void loadNurseName() {
        String userId = auth.getCurrentUser().getUid();
        if (userId != null) {
            firestore.collection("users").document(userId).get().addOnSuccessListener(documentSnapshot -> {
                String nurseName = documentSnapshot.getString("name");
                welcomeTextView.setText("Welcome, " + (nurseName != null ? nurseName : "Nurse") + "!");
            });
        } else {
            welcomeTextView.setText("Welcome, Nurse!");
        }
    }

    private void loadCurrentDateTime() {
        SimpleDateFormat sdf = new SimpleDateFormat("EEEE, MMMM dd, yyyy - HH:mm", Locale.getDefault());
        String currentDateAndTime = sdf.format(new Date());
        dateTimeTextView.setText(currentDateAndTime);
    }

    private void loadPatientList() {
        CollectionReference usersRef = firestore.collection("users");
        usersRef.whereEqualTo("role", "Patient").get().addOnCompleteListener(task -> {
            if (task.isSuccessful() && task.getResult() != null) {
                patientListLayout.removeAllViews(); // Clear any existing views in layout
                QuerySnapshot querySnapshot = task.getResult();

                for (DocumentSnapshot document : querySnapshot.getDocuments()) {
                    String patientName = document.getString("name");
                    String patientId = document.getId();

                    if (patientName != null) {
                        // Create and style TextView for each patient
                        TextView patientTextView = new TextView(this);
                        patientTextView.setText(patientName);
                        patientTextView.setTextSize(18);
                        patientTextView.setPadding(16, 16, 16, 16);
                        patientTextView.setGravity(Gravity.CENTER_VERTICAL);
                        patientTextView.setOnClickListener(v -> openPatientDetails(patientId));

                        // Add patient TextView to layout
                        patientListLayout.addView(patientTextView);
                    }
                }
            } else {
                Toast.makeText(NurseHomepageActivity.this, "Error loading patient list", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void openPatientDetails(String patientId) {
        Intent intent = new Intent(this, EditPatientHealthActivity.class);
        intent.putExtra("PATIENT_ID", patientId);
        startActivity(intent);
    }
}
