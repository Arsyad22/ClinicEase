package com.example.clinicease;  // Ensure this matches your package structure

public class Appointment {
    private String name;         // Patient's name
    private String icNumber;     // Patient's IC number
    private String date;         // Appointment date
    private String time;         // Appointment time

    // Default constructor required for Firebase
    public Appointment() {
    }

    // Parameterized constructor to initialize the appointment
    public Appointment(String name, String icNumber, String date, String time) {
        this.name = name;
        this.icNumber = icNumber;
        this.date = date;
        this.time = time;
    }

    // Getters (for accessing the data)
    public String getName() {
        return name;
    }

    public String getIcNumber() {
        return icNumber;
    }

    public String getDate() {
        return date;
    }

    public String getTime() {
        return time;
    }

    // Setters (for modifying the data)
    public void setName(String name) {
        this.name = name;
    }

    public void setIcNumber(String icNumber) {
        this.icNumber = icNumber;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public void setTime(String time) {
        this.time = time;
    }
}

