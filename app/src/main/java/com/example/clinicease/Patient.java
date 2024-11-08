package com.example.clinicease;

public class Patient {
    private String id;
    private String name;
    private String healthData;

    // Constructor
    public Patient(String id, String name, String healthData) {
        this.id = id;
        this.name = name;
        this.healthData = healthData;
    }

    // Getters
    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getHealthData() {
        return healthData;
    }
}
