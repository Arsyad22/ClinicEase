package com.example.clinicease;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.List;

public class PatientListAdapter extends ArrayAdapter<String> {

    private final Context context;
    private final List<String> patients;

    public PatientListAdapter(Context context, List<String> patients) {
        super(context, R.layout.patient_list_item, patients);
        this.context = context;
        this.patients = patients;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.patient_list_item, parent, false);
        }

        TextView patientNameTextView = convertView.findViewById(R.id.patientNameTextView);
        patientNameTextView.setText(patients.get(position));

        return convertView;
    }
}
