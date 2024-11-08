package com.example.clinicease;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import java.util.List;
import java.util.Map;

public class AppointmentAdapter extends RecyclerView.Adapter<AppointmentAdapter.ViewHolder> {

    private Context context;
    private List<Map<String, Object>> appointments;

    public AppointmentAdapter(Context context, List<Map<String, Object>> appointments) {
        this.context = context;
        this.appointments = appointments;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_appointment, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Map<String, Object> appointment = appointments.get(position);

        holder.nameTextView.setText((String) appointment.get("name"));
        holder.dateTextView.setText((String) appointment.get("date"));
        holder.timeTextView.setText((String) appointment.get("time"));

        String qrCodeUrl = (String) appointment.get("qrCodeUrl");
        if (qrCodeUrl != null && !qrCodeUrl.isEmpty()) {
            Glide.with(context).load(qrCodeUrl).into(holder.qrCodeImageView);
        }
    }

    @Override
    public int getItemCount() {
        return appointments.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView nameTextView, dateTextView, timeTextView;
        ImageView qrCodeImageView;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            nameTextView = itemView.findViewById(R.id.nameTextView);
            dateTextView = itemView.findViewById(R.id.dateTextView);
            timeTextView = itemView.findViewById(R.id.timeTextView);
            qrCodeImageView = itemView.findViewById(R.id.qrCodeImageView);
        }
    }
}
