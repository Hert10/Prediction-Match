package com.example.demo.service;


import jakarta.annotation.PostConstruct;
import ml.dmlc.xgboost4j.java.Booster;
import ml.dmlc.xgboost4j.java.DMatrix;
import ml.dmlc.xgboost4j.java.XGBoost;
import org.springframework.stereotype.Service;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
@Service
public class EmbeddedPredictionService {

    private Booster model;

    @PostConstruct
    public void init() throws IOException, ml.dmlc.xgboost4j.java.XGBoostError {

        String modelName = "footballModel.json";

        try (InputStream in = getClass().getClassLoader().getResourceAsStream(modelName)) {
            if (in == null) {
                throw new IOException("Model file not found in resources: " + modelName);
            }

            File tempFile = File.createTempFile("xgboost_model", ".json");
            tempFile.deleteOnExit();

            try (OutputStream out = new FileOutputStream(tempFile)) {
                byte[] buffer = new byte[1024];
                int bytesRead;
                while ((bytesRead = in.read(buffer)) != -1) {
                    out.write(buffer, 0, bytesRead);
                }
            }

            this.model = XGBoost.loadModel(tempFile.getAbsolutePath());
        }
    }

    public Map<String, Double> predict(double homePoints, double homeGoals, double homeConceded,
                                       double awayPoints, double awayGoals, double awayConceded) throws ml.dmlc.xgboost4j.java.XGBoostError {


        float[] data = new float[] {
                (float) homePoints,
                (float) homeGoals,
                (float) homeConceded,
                (float) awayPoints,
                (float) awayGoals,
                (float) awayConceded
        };

        DMatrix matrix = new DMatrix(data, 1, 6, Float.NaN);


        float[][] predictions = model.predict(matrix);
        float[] probs = predictions[0];

        Map<String, Double> result = new HashMap<>();
        result.put("Away", (double) probs[0]);
        result.put("Draw", (double) probs[1]);
        result.put("Home", (double) probs[2]);

        return result;
    }
}
