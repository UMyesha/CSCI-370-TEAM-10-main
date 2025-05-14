package com.team10.diabetesprediction.service;

import java.util.ArrayList;
import java.util.List;

public class DataSet {
    private double[][] data;
    private int numFeatures;
    private static final int LABEL_INDEX = 13; // Last column is the label (diabetes)

    public DataSet() {
        this.data = new double[0][0];
        this.numFeatures = 0;
    }

    public void setData(double[][] data) {
        this.data = data;
        if (data.length > 0) {
            this.numFeatures = data[0].length - 1; // Exclude label column
        }
    }

    public boolean isPure() {
        if (data.length == 0) return true;
        double firstLabel = data[0][LABEL_INDEX];
        for (double[] row : data) {
            if (row[LABEL_INDEX] != firstLabel) return false;
        }
        return true;
    }

    public int getMajorityLabel() {
        if (data.length == 0) return 0;
        int ones = 0;
        int zeros = 0;
        for (double[] row : data) {
            if (row[LABEL_INDEX] == 1.0) ones++;
            else zeros++;
        }
        return ones >= zeros ? 1 : 0;
    }

    public DataSet[] split(int featureIndex, double splitValue) {
        List<double[]> leftData = new ArrayList<>();
        List<double[]> rightData = new ArrayList<>();

        for (double[] row : data) {
            if (row[featureIndex] <= splitValue) {
                leftData.add(row);
            } else {
                rightData.add(row);
            }
        }

        DataSet leftSet = new DataSet();
        DataSet rightSet = new DataSet();
        
        leftSet.setData(leftData.toArray(new double[0][]));
        rightSet.setData(rightData.toArray(new double[0][]));

        return new DataSet[]{leftSet, rightSet};
    }
} 