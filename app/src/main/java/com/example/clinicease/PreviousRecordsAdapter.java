package com.example.clinicease;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class PreviousRecordsAdapter extends RecyclerView.Adapter<PreviousRecordsAdapter.ViewHolder> {

    private List<PrescriptionRecord> records;

    public PreviousRecordsAdapter(List<PrescriptionRecord> records) {
        this.records = records;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.cardview_previous_records, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        PrescriptionRecord record = records.get(position);
        holder.prescriptionTextView.setText("Prescription: " + record.getPrescription());
        holder.notesTextView.setText("Notes: " + record.getNotes());
        holder.dateTextView.setText("Date: " + record.getDate());
    }

    @Override
    public int getItemCount() {
        return records.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public TextView prescriptionTextView;
        public TextView notesTextView;
        public TextView dateTextView;

        public ViewHolder(View itemView) {
            super(itemView);
            prescriptionTextView = itemView.findViewById(R.id.prescriptionTextView);
            notesTextView = itemView.findViewById(R.id.notesTextView);
            dateTextView = itemView.findViewById(R.id.dateTextView);
        }
    }
}
