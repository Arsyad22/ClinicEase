package com.example.clinicease;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.MutableData;
import com.google.firebase.database.Transaction;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.zxing.BinaryBitmap;
import com.google.zxing.Result;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;
import com.google.zxing.qrcode.QRCodeReader;
import com.google.zxing.common.HybridBinarizer;
import com.google.zxing.RGBLuminanceSource;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class CheckInActivity extends AppCompatActivity {

    private TextView patientDetailsTextView;
    private Button confirmArrivalButton, cancelButton;
    private ImageButton selectImageButton;
    private String icNumber; // IC number retrieved from QR code

    // QR Scan launcher
    private final ActivityResultLauncher<Intent> qrScanLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() == RESULT_OK) {
                    IntentResult intentResult = IntentIntegrator.parseActivityResult(
                            result.getResultCode(), result.getData());
                    if (intentResult != null) {
                        retrievePatientDetails(intentResult.getContents());
                    }
                }
            });

    // Gallery launcher
    private final ActivityResultLauncher<Intent> galleryLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    Uri selectedImage = result.getData().getData();
                    processSelectedImage(selectedImage);
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_check_in);

        patientDetailsTextView = findViewById(R.id.patientDetailsTextView);
        confirmArrivalButton = findViewById(R.id.confirmArrivalButton);
        cancelButton = findViewById(R.id.cancelButton);
        selectImageButton = findViewById(R.id.selectImageButton);

        // Start QR scanner on activity start
        startQRScanner();

        // Button to open the gallery
        selectImageButton.setOnClickListener(v -> openGallery());

        // Confirm arrival button action
        confirmArrivalButton.setOnClickListener(v -> confirmArrival(icNumber));

        // Cancel button action
        cancelButton.setOnClickListener(v -> cancelCheckIn());
    }

    // Method to start the QR scanner
    private void startQRScanner() {
        IntentIntegrator integrator = new IntentIntegrator(this);
        integrator.setDesiredBarcodeFormats(IntentIntegrator.QR_CODE);
        integrator.setPrompt("Align QR code within the frame");
        integrator.setCameraId(0);
        integrator.setBeepEnabled(false);
        integrator.setBarcodeImageEnabled(true);
        qrScanLauncher.launch(integrator.createScanIntent());
    }

    // Open gallery for selecting an image
    private void openGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        galleryLauncher.launch(intent);
    }

    // Process the selected image from gallery
    private void processSelectedImage(Uri imageUri) {
        try {
            Bitmap bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), imageUri);
            String qrContent = decodeQRCodeFromBitmap(bitmap);
            if (qrContent != null) {
                retrievePatientDetails(qrContent);
            } else {
                Toast.makeText(this, "No QR code found in image", Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Decode QR code from Bitmap image
    private String decodeQRCodeFromBitmap(Bitmap bitmap) {
        int[] intArray = new int[bitmap.getWidth() * bitmap.getHeight()];
        bitmap.getPixels(intArray, 0, bitmap.getWidth(), 0, 0, bitmap.getWidth(), bitmap.getHeight());
        RGBLuminanceSource source = new RGBLuminanceSource(bitmap.getWidth(), bitmap.getHeight(), intArray);
        BinaryBitmap binaryBitmap = new BinaryBitmap(new HybridBinarizer(source));
        QRCodeReader reader = new QRCodeReader();
        try {
            Result result = reader.decode(binaryBitmap);
            return result.getText();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    // Retrieve patient details based on QR code content
    private void retrievePatientDetails(String qrCodeContent) {
        Log.d("QRContent", "Scanned QR Code Content: " + qrCodeContent);
        try {
            // Parse JSON format
            JSONObject json = new JSONObject(qrCodeContent);
            String name = json.getString("name");
            icNumber = json.getString("ic_number");

            if (icNumber.length() != 12) {
                Toast.makeText(this, "Invalid IC number format. IC number must be 12 digits.", Toast.LENGTH_SHORT).show();
                return;
            }

            String date = json.getString("date");
            String time = json.getString("time");

            patientDetailsTextView.setText(getString(R.string.patient_details_format, name, icNumber, date, time));

        } catch (Exception e) {
            Log.e("QRParseError", "Failed to parse QR code content: " + e.getMessage());

            // Fallback to parse as a delimited string format
            String[] parts = qrCodeContent.split("\\|");
            if (parts.length == 4) {
                String name = parts[0];
                icNumber = parts[1];

                if (icNumber.length() != 12) {
                    Toast.makeText(this, "Invalid IC number format. IC number must be 12 digits.", Toast.LENGTH_SHORT).show();
                    return;
                }

                String date = parts[2];
                String time = parts[3];

                patientDetailsTextView.setText(getString(R.string.patient_details_format, name, icNumber, date, time));
            } else {
                patientDetailsTextView.setText(R.string.invalid_qr_content);
                Toast.makeText(this, "Invalid QR code format", Toast.LENGTH_SHORT).show();
            }
        }

        // Make the confirmation buttons visible
        findViewById(R.id.patientDetailsLayout).setVisibility(View.VISIBLE);
        confirmArrivalButton.setVisibility(View.VISIBLE);
        cancelButton.setVisibility(View.VISIBLE);
    }

    // Confirm patient arrival and update Firebase queue
    private void confirmArrival(String icNumber) {
        if (icNumber == null || icNumber.length() != 12) {
            Toast.makeText(this, "Invalid IC number", Toast.LENGTH_SHORT).show();
            return;
        }

        // Extract patient name from the displayed details
        String patientName = patientDetailsTextView.getText().toString().split("\n")[0]; // Assuming name is the first line

        DatabaseReference queueRef = FirebaseDatabase.getInstance().getReference("clinic_queue");

        queueRef.runTransaction(new Transaction.Handler() {
            @NonNull
            @Override
            public Transaction.Result doTransaction(@NonNull MutableData mutableData) {
                Integer currentCount = mutableData.child("current_count").getValue(Integer.class);
                if (currentCount == null) {
                    currentCount = 0;
                }

                // Check if the patient is already in the queue
                if (mutableData.child("patients_in_queue").hasChild(icNumber)) {
                    return Transaction.abort();
                }

                // Prepare patient data with name and status
                Map<String, Object> patientData = new HashMap<>();
                patientData.put("name", patientName);
                patientData.put("status", "checked_in");

                // Add patient data to the queue and increment count
                mutableData.child("patients_in_queue").child(icNumber).setValue(patientData);
                mutableData.child("current_count").setValue(currentCount + 1);

                return Transaction.success(mutableData);
            }

            @Override
            public void onComplete(@Nullable DatabaseError databaseError, boolean committed, @Nullable DataSnapshot dataSnapshot) {
                if (committed) {
                    Toast.makeText(CheckInActivity.this, "Patient check-in confirmed", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(CheckInActivity.this, "Patient is already checked in", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    // Cancel the check-in
    private void cancelCheckIn() {
        patientDetailsTextView.setText("");
        findViewById(R.id.patientDetailsLayout).setVisibility(View.GONE);
    }
}
