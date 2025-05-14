package com.team10.diabetesprediction.model;

import lombok.Data;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DiabetesData {
    private String gender;
    private float age;
    private int hypertension;
    private int heartDisease;
    private String smokingHistory;
    private float bmi;
    private float hba1cLevel;
    private int bloodGlucoseLevel;
    private Integer diabetes; // Target variable, null for predictions

    public double[] toFeatureArray() {
        return new double[] {
            age,
            hypertension,
            heartDisease,
            bmi,
            hba1cLevel,
            bloodGlucoseLevel
            // Gender and smoking history will be one-hot encoded
        };
    }
} 