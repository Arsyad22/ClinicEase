package com.example.clinicease;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class SearchPatientActivity extends AppCompatActivity {

    private EditText searchEditText;
    private ListView patientListView;
    private ArrayList<String> patientList;
    private PatientListAdapter adapter;
    private FirebaseFirestore firestore;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_patient);

        // Initialize Firebase Firestore
        firestore = FirebaseFirestore.getInstance();

        // Initialize UI components
        searchEditText = findViewById(R.id.searchEditText);
        patientListView = findViewById(R.id.patientListView);
        patientList = new ArrayList<>();
        adapter = new PatientListAdapter(this, patientList);
        patientListView.setAdapter(adapter);

        // Search function
        searchEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {}

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                String searchText = charSequence.toString();
                searchPatients(searchText);
            }

            @Override
            public void afterTextChanged(Editable editable) {}
        });

        // Load the initial patient list
        loadAllPatients();
    }

    private void searchPatients(String searchText) {
        // Clear the previous search results
        patientList.clear();

        // Query Firestore for patients matching the search
        firestore.collection("users")
                .whereEqualTo("role", "Patient")
                .whereGreaterThanOrEqualTo("name", searchText)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            String patientName = document.getString("name");
                            patientList.add(patientName);
                        }
                        adapter.notifyDataSetChanged();
                    } else {
                        Toast.makeText(SearchPatientActivity.this, "Error searching patients", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void loadAllPatients() {
        // Load all patients initially
        firestore.collection("users")
                .whereEqualTo("role", "Patient")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            String patientName = document.getString("name");
                            patientList.add(patientName);
                        }
                        adapter.notifyDataSetChanged();
                    } else {
                        Toast.makeText(SearchPatientActivity.this, "Error loading patient list", Toast.LENGTH_SHORT).show();
                    }
                });
    }
}
