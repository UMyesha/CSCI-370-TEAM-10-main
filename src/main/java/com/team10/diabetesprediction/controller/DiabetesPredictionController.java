package com.team10.diabetesprediction.controller;

import com.team10.diabetesprediction.model.DiabetesData;
import com.team10.diabetesprediction.service.RandomForestService;
import com.team10.diabetesprediction.security.AESEncryption;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.Map;
import java.util.HashMap;

@RestController
@RequestMapping("/api/v1")
@CrossOrigin(origins = "*")
public class DiabetesPredictionController {

    @Autowired
    private RandomForestService randomForestService;

    @Autowired
    private AESEncryption aesEncryption;

    @PostMapping("/predict")
    public ResponseEntity<Map<String, Object>> predict(@RequestBody DiabetesData data) {
        try {
            // Encrypt sensitive data
            String encryptedGender = aesEncryption.encrypt(data.getGender());
            String encryptedSmokingHistory = aesEncryption.encrypt(data.getSmokingHistory());
            
            // Make prediction
            int prediction = randomForestService.predict(data);
            double[] probabilities = randomForestService.predictProbability(data);
            
            // Prepare response
            Map<String, Object> response = new HashMap<>();
            response.put("prediction", prediction);
            response.put("isDiabetic", prediction == 1);
            response.put("probability", Map.of(
                "nonDiabetic", probabilities[0],
                "diabetic", probabilities[1]
            ));
            response.put("encryptedData", Map.of(
                "gender", encryptedGender,
                "smokingHistory", encryptedSmokingHistory
            ));
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", "Prediction failed: " + e.getMessage());
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }

    @GetMapping("/health")
    public ResponseEntity<String> healthCheck() {
        return ResponseEntity.ok("Service is healthy");
    }
} 