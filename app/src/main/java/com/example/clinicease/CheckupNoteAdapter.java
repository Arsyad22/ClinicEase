package com.example.clinicease;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.clinicease.R;
import com.google.firebase.firestore.DocumentSnapshot;

import java.util.List;
import java.util.Map;

public class CheckupNoteAdapter extends RecyclerView.Adapter<CheckupNoteAdapter.ViewHolder> {

    private final List<DocumentSnapshot> checkupNotes;

    public CheckupNoteAdapter(List<DocumentSnapshot> checkupNotes) {
        this.checkupNotes = checkupNotes;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_checkup_note, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        DocumentSnapshot document = checkupNotes.get(position);
        Map<String, Object> data = document.getData();
        if (data != null) {
            holder.notesTextView.setText((String) data.get("notes"));
            holder.prescriptionTextView.setText((String) data.get("prescription"));
            holder.timestampTextView.setText((String) data.get("timestamp"));
        }
    }

    @Override
    public int getItemCount() {
        return checkupNotes.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public TextView notesTextView;
        public TextView prescriptionTextView;
        public TextView timestampTextView;

        public ViewHolder(View itemView) {
            super(itemView);
            notesTextView = itemView.findViewById(R.id.notesTextView);
            prescriptionTextView = itemView.findViewById(R.id.prescriptionTextView);
            timestampTextView = itemView.findViewById(R.id.timestampTextView);
        }
    }
}
