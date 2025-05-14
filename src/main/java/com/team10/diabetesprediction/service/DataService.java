package com.team10.diabetesprediction.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Arrays;
import java.util.Collections;

public class DataService {
    private List<double[]> dataset;
    private static final int AGE_INDEX = 7;
    private static final int GENDER_START_INDEX = 0; // First 3 indices are one-hot encoded gender

    public DataService() {
        this.dataset = new ArrayList<>();
    }

    public void setDataset(List<double[]> dataset) {
        this.dataset = new ArrayList<>(dataset);
    }

    public List<double[]> filterAgeAndSex(double[] userData) {
        List<double[]> filtered = new ArrayList<>();
        double userAge = userData[AGE_INDEX];
        
        // Check gender match (first 3 values are one-hot encoded gender)
        for (double[] entry : dataset) {
            boolean genderMatch = true;
            for (int i = 0; i < 3; i++) {
                if (entry[i] != userData[i]) {
                    genderMatch = false;
                    break;
                }
            }
            
            if (genderMatch) {
                double age = entry[AGE_INDEX];
                // Check if age is within ±5 years
                if (Math.abs(age - userAge) <= 5.0) {
                    filtered.add(entry);
                }
            }
        }
        
        // If no age matches found, return all gender matches
        if (filtered.isEmpty()) {
            for (double[] entry : dataset) {
                boolean genderMatch = true;
                for (int i = 0; i < 3; i++) {
                    if (entry[i] != userData[i]) {
                        genderMatch = false;
                        break;
                    }
                }
                if (genderMatch) {
                    filtered.add(entry);
                }
            }
        }
        
        return filtered;
    }

    public double[] getComparisonValues(List<double[]> filteredDataset) {
        if (filteredDataset == null || filteredDataset.isEmpty()) {
            return null;
        }

        int numFeatures = filteredDataset.get(0).length;
        double[] comparison = new double[numFeatures];

        // For each feature
        for (int i = 0; i < numFeatures; i++) {
            List<Double> values = new ArrayList<>();
            for (double[] entry : filteredDataset) {
                values.add(entry[i]);
            }
            Collections.sort(values);

            // Use median for numeric features (age, bmi, HbA1c, glucose)
            if (i == 7 || i == 10 || i == 11 || i == 12) {
                int middle = values.size() / 2;
                if (values.size() % 2 == 0) {
                    comparison[i] = (values.get(middle - 1) + values.get(middle)) / 2.0;
                } else {
                    comparison[i] = values.get(middle);
                }
            }
            // Use mode for categorical features
            else {
                double mode = values.get(0);
                int maxCount = 1;
                int currentCount = 1;
                double currentValue = values.get(0);

                for (int j = 1; j < values.size(); j++) {
                    if (values.get(j).equals(currentValue)) {
                        currentCount++;
                    } else {
                        if (currentCount > maxCount) {
                            maxCount = currentCount;
                            mode = currentValue;
                        }
                        currentValue = values.get(j);
                        currentCount = 1;
                    }
                }
                if (currentCount > maxCount) {
                    mode = currentValue;
                }
                comparison[i] = mode;
            }
        }

        return comparison;
    }
} 