package com.example.clinicease;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

public class HistoryActivity extends AppCompatActivity {

    private FirebaseAuth auth;
    private FirebaseFirestore firestore;
    private LinearLayout appointmentsLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history);

        auth = FirebaseAuth.getInstance();
        firestore = FirebaseFirestore.getInstance();
        appointmentsLayout = findViewById(R.id.appointmentsLayout);

        fetchPastAppointments();  // Fetch only past appointments (checkup notes)
    }

    // Fetch past appointments (checkup notes) for the logged-in user
    private void fetchPastAppointments() {
        String userId = auth.getCurrentUser().getUid();

        firestore.collection("users")
                .document(userId)
                .collection("checkup_notes")
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    appointmentsLayout.removeAllViews();  // Clear previous views

                    if (queryDocumentSnapshots.isEmpty()) {
                        showNoAppointmentsMessage("No past appointments found.");
                    } else {
                        for (DocumentSnapshot document : queryDocumentSnapshots) {
                            displayPastAppointment(document);
                        }
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e("HistoryActivity", "Failed to fetch past appointments", e);
                    showNoAppointmentsMessage("Failed to load past appointments.");
                });
    }

    // Display past appointment details
    private void displayPastAppointment(DocumentSnapshot document) {
        String notes = document.getString("notes");
        String prescription = document.getString("prescription");
        String timestamp = document.getString("timestamp");

        View appointmentView = LayoutInflater.from(this)
                .inflate(R.layout.item_past_appointment, null);

        TextView dateTimeTextView = appointmentView.findViewById(R.id.dateTimeTextView);
        TextView notesTextView = appointmentView.findViewById(R.id.notesTextView);
        TextView prescriptionTextView = appointmentView.findViewById(R.id.prescriptionTextView);

        dateTimeTextView.setText(String.format("Date & Time: %s", timestamp));
        notesTextView.setText(String.format("Doctor's Notes: %s", notes));
        prescriptionTextView.setText(String.format("Prescription: %s", prescription));

        appointmentsLayout.addView(appointmentView);
    }

    private void showNoAppointmentsMessage(String message) {
        appointmentsLayout.removeAllViews();
        TextView noAppointmentsTextView = new TextView(this);
        noAppointmentsTextView.setText(message);
        noAppointmentsTextView.setGravity(android.view.Gravity.CENTER);
        noAppointmentsTextView.setTextSize(18);
        appointmentsLayout.addView(noAppointmentsTextView);
    }
}
