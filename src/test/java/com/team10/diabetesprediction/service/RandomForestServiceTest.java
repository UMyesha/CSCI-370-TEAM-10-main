package com.team10.diabetesprediction.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class RandomForestServiceTest {
    private RandomForestService randomForest;

    @BeforeEach
    void setUp() {
        randomForest = new RandomForestService();
    }

    @Test
    void testAddTreeIncreasesForestSize() {
        // Test case V from assignment
        // Initial condition: empty forest
        assertEquals(0, randomForest.getTreeCount(), "Forest should start empty");

        // Create and add a decision tree
        DecisionTree tree = new DecisionTree();
        randomForest.addTree(tree);

        // Verify tree was added
        assertEquals(1, randomForest.getTreeCount(), "Forest should contain exactly one tree");
    }

    @Test
    void testAggregatePredictionWithTie() {
        // Test case VI from assignment
        // Setup predictions with tie (equal 0s and 1s)
        int[] predictions = {1, 0, 1, 0, 1, 0};
        
        // Get aggregated prediction
        int result = randomForest.aggregatePrediction(predictions);
        
        // Should default to 1 (Diabetic) in tie case
        assertEquals(1, result, "Should return 1 (Diabetic) when votes are tied");
    }

    @Test
    void testAggregatePredictionWithMajority() {
        // Test case VII from assignment
        // Setup predictions with majority of 1s
        int[] predictions = {1, 1, 1, 0, 0, 0, 1};
        
        // Get aggregated prediction
        int result = randomForest.aggregatePrediction(predictions);
        
        // Should return majority (1)
        assertEquals(1, result, "Should return majority prediction (1)");
    }
} 