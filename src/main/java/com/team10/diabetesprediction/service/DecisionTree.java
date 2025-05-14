package com.team10.diabetesprediction.service;

public class DecisionTree {
    private Node root;

    private static class Node {
        double splitValue;
        int featureIndex;
        Node left;
        Node right;
        Integer prediction; // null for internal nodes, 0 or 1 for leaf nodes
    }

    public DecisionTree() {
        this.root = null;
    }

    public void buildTree(DataSet dataset) {
        // Build decision tree recursively
        this.root = buildTreeRecursive(dataset);
    }

    private Node buildTreeRecursive(DataSet dataset) {
        Node node = new Node();
        
        // If all samples have same label, create leaf node
        if (dataset.isPure()) {
            node.prediction = dataset.getMajorityLabel();
            return node;
        }

        // Find best split
        BestSplit split = findBestSplit(dataset);
        if (split == null) {
            // Cannot split further, create leaf node
            node.prediction = dataset.getMajorityLabel();
            return node;
        }

        node.featureIndex = split.featureIndex;
        node.splitValue = split.splitValue;

        // Split data and build subtrees
        DataSet[] splits = dataset.split(split.featureIndex, split.splitValue);
        node.left = buildTreeRecursive(splits[0]);
        node.right = buildTreeRecursive(splits[1]);

        return node;
    }

    public Node getRoot() {
        return root;
    }

    public boolean isTreePure() {
        return isNodePure(root);
    }

    private boolean isNodePure(Node node) {
        if (node == null) return true;
        if (node.prediction != null) return true; // Leaf node is pure by definition
        return isNodePure(node.left) && isNodePure(node.right);
    }

    public int getClassification(double[] userData) {
        return classifyRecursive(root, userData);
    }

    private int classifyRecursive(Node node, double[] userData) {
        if (node.prediction != null) {
            return node.prediction;
        }

        if (userData[node.featureIndex] <= node.splitValue) {
            return classifyRecursive(node.left, userData);
        } else {
            return classifyRecursive(node.right, userData);
        }
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof DecisionTree)) return false;
        DecisionTree other = (DecisionTree) obj;
        return nodesEqual(this.root, other.root);
    }

    private boolean nodesEqual(Node n1, Node n2) {
        if (n1 == null && n2 == null) return true;
        if (n1 == null || n2 == null) return false;
        
        if (n1.prediction != null && n2.prediction != null) {
            return n1.prediction.equals(n2.prediction);
        }
        
        return n1.featureIndex == n2.featureIndex &&
               Math.abs(n1.splitValue - n2.splitValue) < 1e-10 &&
               nodesEqual(n1.left, n2.left) &&
               nodesEqual(n1.right, n2.right);
    }

    private static class BestSplit {
        int featureIndex;
        double splitValue;
    }
} 