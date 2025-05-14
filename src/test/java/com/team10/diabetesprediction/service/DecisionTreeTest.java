package com.team10.diabetesprediction.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class DecisionTreeTest {
    private DecisionTree decisionTree;
    private DataSet testDataset;

    @BeforeEach
    void setUp() {
        decisionTree = new DecisionTree();
        testDataset = new DataSet();
        
        // Setup test data from assignment example
        double[][] data = {
            {1, 0, 0, 0, 0, 1, 0, 80.0, 0, 1, 25.19, 6.6, 140, 0},
            {1, 0, 1, 0, 0, 0, 1, 54.0, 0, 0, 27.32, 6.6, 80, 0},
            {0, 1, 0, 0, 0, 1, 0, 28.0, 0, 0, 27.32, 5.7, 158, 0},
            {1, 0, 0, 0, 1, 0, 0, 36.0, 0, 0, 23.45, 5.0, 155, 0},
            {0, 1, 1, 1, 1, 0, 0, 76.0, 1, 1, 20.14, 4.8, 155, 0},
            {0, 1, 0, 0, 0, 1, 0, 20.0, 0, 0, 24.22, 6.2, 130, 1},
            {0, 1, 1, 0, 0, 1, 0, 68.0, 1, 0, 33.10, 6.7, 190, 1}
        };
        testDataset.setData(data);
    }

    @Test
    void testBuildTreeCreatesValidTree() {
        // Test case II from assignment
        decisionTree.buildTree(testDataset);
        
        // Verify tree was built
        assertNotNull(decisionTree.getRoot(), "Tree should have a root node");
        
        // Verify tree makes pure splits
        assertTrue(decisionTree.isTreePure(), "All leaf nodes should be pure");
    }

    @Test
    void testDeterministicTreeGeneration() {
        // Test case III from assignment
        // Build two trees with same data
        DecisionTree tree1 = new DecisionTree();
        DecisionTree tree2 = new DecisionTree();
        
        tree1.buildTree(testDataset);
        tree2.buildTree(testDataset);
        
        // Trees should be identical
        assertTrue(tree1.equals(tree2), "Trees should be identical when built with same data");
    }

    @Test
    void testGetClassification() {
        // Test case IV from assignment
        decisionTree.buildTree(testDataset);
        
        // Test data from assignment
        double[] userData = {1, 0, 0, 0, 0, 1, 0, 68.0, 1, 0, 33.10, 6.7, 190, 1};
        
        // Get classification
        int result = decisionTree.getClassification(userData);
        
        // Verify classification matches expected
        assertEquals(1, result, "Classification should match expected value");
        
        // Test consistency
        int secondResult = decisionTree.getClassification(userData);
        assertEquals(result, secondResult, "Classification should be consistent for same input");
    }
} 