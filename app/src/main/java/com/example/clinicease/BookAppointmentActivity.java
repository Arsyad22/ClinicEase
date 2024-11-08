package com.example.clinicease;

import android.app.DatePickerDialog;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.journeyapps.barcodescanner.BarcodeEncoder;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class BookAppointmentActivity extends AppCompatActivity {

    private EditText editTextDate;
    private Spinner spinnerTimeSlots;
    private ImageView qrCodeImageView;
    private TextView textAppointmentDetails;

    private FirebaseFirestore firestore;
    private FirebaseStorage storage;
    private String userId;
    private String patientName;
    private String patientIC;
    private String selectedDate;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_book_appointment);

        // Initialize Firebase instances
        FirebaseAuth auth = FirebaseAuth.getInstance();
        firestore = FirebaseFirestore.getInstance();
        storage = FirebaseStorage.getInstance();

        // Initialize UI components
        editTextDate = findViewById(R.id.editTextDate);
        spinnerTimeSlots = findViewById(R.id.spinnerTimeSlots);
        Button buttonConfirmBooking = findViewById(R.id.buttonConfirmBooking);
        qrCodeImageView = findViewById(R.id.qrCodeImageView);
        textAppointmentDetails = findViewById(R.id.textAppointmentDetails);

        // Set initial visibility for QR code and details to GONE
        qrCodeImageView.setVisibility(View.GONE);
        textAppointmentDetails.setVisibility(View.GONE);

        // Get current user
        FirebaseUser user = auth.getCurrentUser();
        if (user != null) {
            userId = user.getUid();
            fetchPatientDetails();  // Fetch user data from Firestore
        } else {
            Toast.makeText(this, "User not authenticated.", Toast.LENGTH_SHORT).show();
            finish();
        }

        // Show DatePicker when the EditText is clicked
        editTextDate.setOnClickListener(v -> showDatePicker());

        // Confirm booking on button click
        buttonConfirmBooking.setOnClickListener(v -> confirmBooking());
    }

    private void fetchPatientDetails() {
        firestore.collection("users").document(userId).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        patientName = documentSnapshot.getString("name");
                        patientIC = documentSnapshot.getString("ic_number");

                        if (patientName == null || patientIC == null) {
                            Log.e("Firestore", "Patient details are missing.");
                            Toast.makeText(this, "Patient details missing.", Toast.LENGTH_SHORT).show();
                        } else {
                            Log.d("Firestore", "Patient details fetched: " + patientName + ", " + patientIC);
                        }
                    } else {
                        Log.e("Firestore", "No patient data found.");
                        Toast.makeText(this, "No patient data found.", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e("Firestore", "Failed to fetch patient details: " + e.getMessage());
                    Toast.makeText(this, "Failed to load patient details.", Toast.LENGTH_SHORT).show();
                });
    }

    private void showDatePicker() {
        Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(this, (view, selectedYear, selectedMonth, selectedDay) -> {
            selectedDate = String.format(Locale.US, "%d-%02d-%02d", selectedYear, selectedMonth + 1, selectedDay);
            editTextDate.setText(selectedDate);
            loadAvailableTimeSlots();
        }, year, month, day);

        datePickerDialog.getDatePicker().setMinDate(calendar.getTimeInMillis());
        datePickerDialog.show();
    }

    private void loadAvailableTimeSlots() {
        List<String> availableSlots = new ArrayList<>();
        for (int hour = 8; hour < 18; hour++) {
            availableSlots.add(hour + ":00");
            availableSlots.add(hour + ":30");
        }

        firestore.collection("appointments")
                .whereEqualTo("date", selectedDate)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    for (DocumentSnapshot snapshot : querySnapshot) {
                        String bookedTime = snapshot.getString("time");
                        if (bookedTime != null) {
                            availableSlots.remove(bookedTime);
                        }
                    }

                    if (availableSlots.isEmpty()) {
                        availableSlots.add("No available slots");
                    }

                    // Update Spinner
                    ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, availableSlots);
                    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                    spinnerTimeSlots.setAdapter(adapter);
                })
                .addOnFailureListener(e -> {
                    Log.e("Firestore", "Failed to load available slots: " + e.getMessage());
                    Toast.makeText(this, "Failed to load available slots.", Toast.LENGTH_SHORT).show();
                });
    }

    private void confirmBooking() {
        if (spinnerTimeSlots.getSelectedItem() == null || selectedDate == null || selectedDate.isEmpty()) {
            Toast.makeText(this, "Please select a valid date and time slot.", Toast.LENGTH_SHORT).show();
            return;
        }

        String selectedSlot = spinnerTimeSlots.getSelectedItem().toString();
        if (selectedSlot.equals("No available slots")) {
            Toast.makeText(this, "No available time slots.", Toast.LENGTH_SHORT).show();
            return;
        }

        if (patientName != null && patientIC != null) {
            Appointment appointment = new Appointment(patientName, patientIC, selectedDate, selectedSlot);

            // Save the appointment in Firestore
            firestore.collection("appointments")
                    .document(patientName + "_" + patientIC)
                    .set(appointment)
                    .addOnSuccessListener(aVoid -> {
                        Toast.makeText(this, "Appointment booked for " + selectedDate + " at " + selectedSlot, Toast.LENGTH_SHORT).show();
                        generateAndUploadQRCode(appointment);
                    })
                    .addOnFailureListener(e -> {
                        Log.e("Firestore", "Failed to save appointment: " + e.getMessage());
                        Toast.makeText(this, "Failed to book appointment.", Toast.LENGTH_SHORT).show();
                    });
        } else {
            Toast.makeText(this, "Failed to retrieve patient details.", Toast.LENGTH_SHORT).show();
        }
    }

    private void generateAndUploadQRCode(Appointment appointment) {
        try {
            String qrData = "{\"name\":\"" + appointment.getName() + "\",\"ic_number\":\"" + appointment.getIcNumber() + "\",\"date\":\"" + appointment.getDate() + "\",\"time\":\"" + appointment.getTime() + "\"}";
            BarcodeEncoder barcodeEncoder = new BarcodeEncoder();
            Bitmap bitmap = barcodeEncoder.encodeBitmap(qrData, com.google.zxing.BarcodeFormat.QR_CODE, 400, 400);

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, baos);
            byte[] data = baos.toByteArray();

            StorageReference qrCodeRef = storage.getReference().child("qrcodes/" + appointment.getName() + "_" + appointment.getIcNumber() + ".png");
            UploadTask uploadTask = qrCodeRef.putBytes(data);

            uploadTask.addOnSuccessListener(taskSnapshot ->
                    qrCodeRef.getDownloadUrl().addOnSuccessListener(uri -> {
                        String downloadUrl = uri.toString();
                        firestore.collection("appointments")
                                .document(patientName + "_" + patientIC)
                                .update("qrCodeUrl", downloadUrl)
                                .addOnSuccessListener(aVoid -> {
                                    displayQrCodeAndDetails(downloadUrl, appointment);
                                    Toast.makeText(this, "QR code saved successfully!", Toast.LENGTH_SHORT).show();
                                });
                    })
            ).addOnFailureListener(e -> {
                Log.e("Storage", "Failed to upload QR code: " + e.getMessage());
                Toast.makeText(this, "Failed to upload QR code.", Toast.LENGTH_SHORT).show();
            });
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Failed to generate QR code.", Toast.LENGTH_SHORT).show();
        }
    }

    private void displayQrCodeAndDetails(String qrCodeUrl, Appointment appointment) {
        textAppointmentDetails.setText(getString(R.string.appointment_details,
                appointment.getName(),
                appointment.getIcNumber(),
                appointment.getDate(),
                appointment.getTime()));
        textAppointmentDetails.setVisibility(View.VISIBLE);

        // Load the QR code into the ImageView
        Glide.with(this).load(qrCodeUrl).into(qrCodeImageView);
        qrCodeImageView.setVisibility(View.VISIBLE);
    }
}
