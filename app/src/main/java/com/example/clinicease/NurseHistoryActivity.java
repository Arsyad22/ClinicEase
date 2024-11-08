package com.example.clinicease;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class NurseHistoryActivity extends AppCompatActivity {

    private RecyclerView nurseHistoryRecyclerView;
    private AppointmentAdapter appointmentAdapter;
    private List<Map<String, Object>> appointmentsList = new ArrayList<>();
    private FirebaseFirestore db = FirebaseFirestore.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_nurse_history);

        nurseHistoryRecyclerView = findViewById(R.id.nurseHistoryRecyclerView);
        nurseHistoryRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        appointmentAdapter = new AppointmentAdapter(this, appointmentsList);
        nurseHistoryRecyclerView.setAdapter(appointmentAdapter);

        loadBookedAppointments();
    }

    private void loadBookedAppointments() {
        db.collection("appointments")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        QuerySnapshot querySnapshot = task.getResult();
                        if (querySnapshot != null) {
                            appointmentsList.clear();
                            for (DocumentSnapshot document : querySnapshot.getDocuments()) {
                                Map<String, Object> appointment = new HashMap<>();
                                appointment.put("name", document.getString("name"));
                                appointment.put("date", document.getString("date"));
                                appointment.put("time", document.getString("time"));
                                appointment.put("qrCodeUrl", document.getString("qrCodeUrl"));
                                appointmentsList.add(appointment);
                            }
                            appointmentAdapter.notifyDataSetChanged();
                        }
                    } else {
                        Toast.makeText(this, "Failed to load appointments.", Toast.LENGTH_SHORT).show();
                    }
                });
    }
}
