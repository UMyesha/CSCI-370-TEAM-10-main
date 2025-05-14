package com.team10.diabetesprediction.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import java.util.List;
import java.util.Arrays;

public class DataServiceTest {
    private DataService dataService;
    private List<double[]> testDataset;

    @BeforeEach
    void setUp() {
        dataService = new DataService();
        
        // Setup test dataset from assignment example
        testDataset = Arrays.asList(
            new double[]{0, 1, 0, 0, 1, 0, 0, 60.0, 1, 0, 27.5, 5.4, 120, 0},  // Male, age 60
            new double[]{0, 1, 0, 0, 0, 0, 1, 45.0, 0, 0, 29.0, 6.0, 130, 1},  // Male, age 45
            new double[]{1, 0, 0, 0, 0, 0, 0, 50.0, 0, 1, 25.0, 5.9, 135, 0},  // Female, age 50
            new double[]{0, 0, 1, 0, 1, 0, 0, 35.0, 1, 0, 30.1, 5.5, 110, 1}   // Other gender, age 35
        );
        dataService.setDataset(testDataset);
    }

    @Test
    void testFilterAgeAndSexWithMatch() {
        // Test case VIII from assignment
        // Male, age 32
        double[] userData = {0, 1, 0, 0, 1, 0, 0, 32.0, 0, 0, 26.8, 5.9, 143, 0};
        
        // Add test data within age range
        testDataset.add(new double[]{0, 1, 0, 0, 1, 0, 0, 34.0, 0, 0, 28.5, 5.8, 125, 0});  // Male, age 34
        
        List<double[]> filtered = dataService.filterAgeAndSex(userData);
        
        // Should return matches within ±5 years and same gender
        assertFalse(filtered.isEmpty(), "Should find matching entries");
        for (double[] entry : filtered) {
            // Verify gender (Male)
            assertEquals(0, entry[0], "Should be male (first value 0)");
            assertEquals(1, entry[1], "Should be male (second value 1)");
            assertEquals(0, entry[2], "Should be male (third value 0)");
            
            // Verify age within range
            double age = entry[7];
            assertTrue(age >= 27.0 && age <= 37.0, "Age should be within ±5 years");
        }
    }

    @Test
    void testFilterAgeAndSexWithNoAgeMatch() {
        // Test case IX from assignment
        // Male, age 92
        double[] userData = {0, 1, 0, 0, 1, 0, 0, 92.0, 0, 0, 24.1, 6.1, 140, 0};
        
        List<double[]> filtered = dataService.filterAgeAndSex(userData);
        
        // Should return all male entries regardless of age
        assertFalse(filtered.isEmpty(), "Should find matching entries");
        assertEquals(2, filtered.size(), "Should find 2 male entries");
        
        for (double[] entry : filtered) {
            // Verify gender (Male)
            assertEquals(0, entry[0], "Should be male (first value 0)");
            assertEquals(1, entry[1], "Should be male (second value 1)");
            assertEquals(0, entry[2], "Should be male (third value 0)");
        }
    }

    @Test
    void testGetComparisonValuesWithValidDataset() {
        // Test case X from assignment
        List<double[]> filteredDataset = Arrays.asList(
            new double[]{1, 0, 0, 0, 0, 1, 0, 34.0, 0, 0, 24.1, 6.0, 142, 0},
            new double[]{1, 0, 0, 0, 0, 1, 0, 36.0, 0, 0, 25.5, 5.8, 145, 0},
            new double[]{1, 0, 0, 0, 0, 1, 0, 32.0, 0, 0, 23.8, 6.2, 138, 1}
        );
        
        double[] comparison = dataService.getComparisonValues(filteredDataset);
        
        assertNotNull(comparison, "Should return comparison values");
        assertEquals(34.0, comparison[7], "Age should be median of values");
        assertEquals(24.1, comparison[10], "BMI should be median of values");
        assertEquals(6.0, comparison[11], "HbA1c should be median of values");
        assertEquals(142.0, comparison[12], "Glucose should be median of values");
    }
} 