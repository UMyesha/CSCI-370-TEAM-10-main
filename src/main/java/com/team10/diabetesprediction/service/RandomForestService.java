package com.team10.diabetesprediction.service;

import com.team10.diabetesprediction.model.DiabetesData;
import org.springframework.stereotype.Service;
import smile.classification.RandomForest;
import smile.data.DataFrame;
import smile.data.formula.Formula;
import smile.data.vector.BaseVector;
import smile.data.vector.DoubleVector;
import smile.data.vector.IntVector;
import smile.data.type.DataTypes;
import smile.data.type.StructType;
import smile.data.type.StructField;
import java.util.List;
import java.util.ArrayList;
import java.util.Properties;
import org.springframework.beans.factory.annotation.Value;
import jakarta.annotation.PostConstruct;
import java.io.BufferedReader;
import java.io.FileReader;
import java.util.Arrays;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
public class RandomForestService {
    private static final Logger logger = LoggerFactory.getLogger(RandomForestService.class);
    
    private RandomForest model;
    private List<String> genderCategories;
    private List<String> smokingCategories;
    private StructType schema;

    @Value("${dataset.path}")
    private String datasetPath;

    @PostConstruct
    public void init() throws Exception {
        // Load and preprocess data
        List<DiabetesData> data = loadData();
        DataFrame df = preprocessData(data);
        
        // Train model with properties
        Formula formula = Formula.lhs("diabetes");
        Properties props = new Properties();
        props.setProperty("smile.random.forest.trees", "100");
        model = RandomForest.fit(formula, df, props);
        
        logger.info("Random Forest model trained with {} samples", data.size());
    }

    private List<DiabetesData> loadData() throws Exception {
        List<DiabetesData> dataList = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader(datasetPath))) {
            String line;
            br.readLine(); // Skip header
            while ((line = br.readLine()) != null) {
                String[] values = line.split(",");
                DiabetesData data = DiabetesData.builder()
                    .gender(values[0])
                    .age(Float.parseFloat(values[1]))
                    .hypertension(Integer.parseInt(values[2]))
                    .heartDisease(Integer.parseInt(values[3]))
                    .smokingHistory(values[4])
                    .bmi(Float.parseFloat(values[5]))
                    .hba1cLevel(Float.parseFloat(values[6]))
                    .bloodGlucoseLevel(Integer.parseInt(values[7]))
                    .diabetes(Integer.parseInt(values[8]))
                    .build();
                dataList.add(data);
            }
        }
        return dataList;
    }

    private DataFrame preprocessData(List<DiabetesData> dataList) {
        // Extract unique categories
        genderCategories = dataList.stream()
            .map(DiabetesData::getGender)
            .distinct()
            .sorted()
            .toList();
        
        smokingCategories = dataList.stream()
            .map(DiabetesData::getSmokingHistory)
            .distinct()
            .sorted()
            .toList();

        // Create schema
        int numFeatures = 6 + genderCategories.size() + smokingCategories.size();
        StructField[] fields = new StructField[numFeatures + 1];
        
        // Add numeric features
        fields[0] = new StructField("age", DataTypes.DoubleType);
        fields[1] = new StructField("hypertension", DataTypes.IntegerType);
        fields[2] = new StructField("heartDisease", DataTypes.IntegerType);
        fields[3] = new StructField("bmi", DataTypes.DoubleType);
        fields[4] = new StructField("hba1cLevel", DataTypes.DoubleType);
        fields[5] = new StructField("bloodGlucoseLevel", DataTypes.IntegerType);
        
        // Add one-hot encoded features
        int idx = 6;
        for (String gender : genderCategories) {
            fields[idx++] = new StructField("gender_" + gender, DataTypes.DoubleType);
        }
        for (String smoking : smokingCategories) {
            fields[idx++] = new StructField("smoking_" + smoking, DataTypes.DoubleType);
        }
        
        // Add target variable
        fields[numFeatures] = new StructField("diabetes", DataTypes.IntegerType);
        schema = DataTypes.struct(fields);

        // Create feature vectors
        BaseVector[] vectors = new BaseVector[numFeatures + 1];
        
        // Initialize arrays for each feature
        double[] age = new double[dataList.size()];
        int[] hypertension = new int[dataList.size()];
        int[] heartDisease = new int[dataList.size()];
        double[] bmi = new double[dataList.size()];
        double[] hba1c = new double[dataList.size()];
        int[] glucose = new int[dataList.size()];
        double[][] genderOneHot = new double[genderCategories.size()][dataList.size()];
        double[][] smokingOneHot = new double[smokingCategories.size()][dataList.size()];
        int[] diabetes = new int[dataList.size()];
        
        // Fill the arrays
        for (int i = 0; i < dataList.size(); i++) {
            DiabetesData data = dataList.get(i);
            age[i] = data.getAge();
            hypertension[i] = data.getHypertension();
            heartDisease[i] = data.getHeartDisease();
            bmi[i] = data.getBmi();
            hba1c[i] = data.getHba1cLevel();
            glucose[i] = data.getBloodGlucoseLevel();
            
            // One-hot encode gender
            int genderIndex = genderCategories.indexOf(data.getGender());
            genderOneHot[genderIndex][i] = 1.0;
            
            // One-hot encode smoking history
            int smokingIndex = smokingCategories.indexOf(data.getSmokingHistory());
            smokingOneHot[smokingIndex][i] = 1.0;
            
            diabetes[i] = data.getDiabetes();
        }
        
        // Create vectors
        vectors[0] = DoubleVector.of("age", age);
        vectors[1] = IntVector.of("hypertension", hypertension);
        vectors[2] = IntVector.of("heartDisease", heartDisease);
        vectors[3] = DoubleVector.of("bmi", bmi);
        vectors[4] = DoubleVector.of("hba1cLevel", hba1c);
        vectors[5] = IntVector.of("bloodGlucoseLevel", glucose);
        
        // Add one-hot encoded vectors
        idx = 6;
        for (int j = 0; j < genderCategories.size(); j++) {
            vectors[idx++] = DoubleVector.of("gender_" + genderCategories.get(j), genderOneHot[j]);
        }
        for (int j = 0; j < smokingCategories.size(); j++) {
            vectors[idx++] = DoubleVector.of("smoking_" + smokingCategories.get(j), smokingOneHot[j]);
        }
        
        // Add target vector
        vectors[numFeatures] = IntVector.of("diabetes", diabetes);

        return DataFrame.of(vectors);
    }

    public int predict(DiabetesData input) {
        // Create a single row DataFrame for prediction
        BaseVector[] vectors = new BaseVector[schema.length() - 1]; // Exclude target variable
        
        // Fill numeric features
        vectors[0] = DoubleVector.of("age", new double[]{input.getAge()});
        vectors[1] = IntVector.of("hypertension", new int[]{input.getHypertension()});
        vectors[2] = IntVector.of("heartDisease", new int[]{input.getHeartDisease()});
        vectors[3] = DoubleVector.of("bmi", new double[]{input.getBmi()});
        vectors[4] = DoubleVector.of("hba1cLevel", new double[]{input.getHba1cLevel()});
        vectors[5] = IntVector.of("bloodGlucoseLevel", new int[]{input.getBloodGlucoseLevel()});
        
        // Fill one-hot encoded features
        int idx = 6;
        for (String gender : genderCategories) {
            vectors[idx++] = DoubleVector.of("gender_" + gender, 
                new double[]{gender.equals(input.getGender()) ? 1.0 : 0.0});
        }
        for (String smoking : smokingCategories) {
            vectors[idx++] = DoubleVector.of("smoking_" + smoking,
                new double[]{smoking.equals(input.getSmokingHistory()) ? 1.0 : 0.0});
        }
        
        DataFrame row = DataFrame.of(vectors);
        return model.predict(row.get(0));
    }

    public double[] predictProbability(DiabetesData input) {
        double[] posteriori = new double[2]; // Binary classification
        // Create prediction DataFrame same as above
        BaseVector[] vectors = new BaseVector[schema.length() - 1];
        vectors[0] = DoubleVector.of("age", new double[]{input.getAge()});
        vectors[1] = IntVector.of("hypertension", new int[]{input.getHypertension()});
        vectors[2] = IntVector.of("heartDisease", new int[]{input.getHeartDisease()});
        vectors[3] = DoubleVector.of("bmi", new double[]{input.getBmi()});
        vectors[4] = DoubleVector.of("hba1cLevel", new double[]{input.getHba1cLevel()});
        vectors[5] = IntVector.of("bloodGlucoseLevel", new int[]{input.getBloodGlucoseLevel()});
        
        int idx = 6;
        for (String gender : genderCategories) {
            vectors[idx++] = DoubleVector.of("gender_" + gender, 
                new double[]{gender.equals(input.getGender()) ? 1.0 : 0.0});
        }
        for (String smoking : smokingCategories) {
            vectors[idx++] = DoubleVector.of("smoking_" + smoking,
                new double[]{smoking.equals(input.getSmokingHistory()) ? 1.0 : 0.0});
        }
        
        DataFrame row = DataFrame.of(vectors);
        model.predict(row.get(0), posteriori);
        return posteriori;
    }
} 