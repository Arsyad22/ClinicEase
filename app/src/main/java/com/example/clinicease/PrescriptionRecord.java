package com.example.clinicease;

public class PrescriptionRecord {
    private String prescription;
    private String notes;
    private String date;

    public PrescriptionRecord() {
        // Default constructor required for Firebase
    }

    public PrescriptionRecord(String prescription, String notes, String date) {
        this.prescription = prescription;
        this.notes = notes;
        this.date = date;
    }

    public String getPrescription() {
        return prescription;
    }

    public void setPrescription(String prescription) {
        this.prescription = prescription;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }
}
