package org.kie.trusty.m2x.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;

/**
 * The prediction generated by a model
 */
@Schema(name="Prediction")
public class Prediction {

    private ModelInfo modelInfo;

    private PredictionInput predictionInput;

    private PredictionOutput predictionOutput;

    public Prediction() {
        this.modelInfo = null;
        this.predictionInput = new PredictionInput();
        this.predictionOutput = new PredictionOutput();
    }

    @JsonCreator
    public Prediction(@JsonProperty("modelInfo") ModelInfo modelInfo, @JsonProperty("predictionInput") PredictionInput predictionInput,
                      @JsonProperty("predictionOutput") PredictionOutput output) {
        this.modelInfo = modelInfo;
        this.predictionInput = predictionInput;
        this.predictionOutput = output;
    }

    public ModelInfo getModelInfo() {
        return modelInfo;
    }

    public PredictionInput getPredictionInput() {
        return predictionInput;
    }

    public PredictionOutput getPredictionOutput() {
        return predictionOutput;
    }

    public void setPredictionInput(PredictionInput predictionInput) {
        this.predictionInput = predictionInput;
    }

    public void setModelInfo(ModelInfo modelInfo) {
        this.modelInfo = modelInfo;
    }

    public void setPredictionOutput(PredictionOutput predictionOutput) {
        this.predictionOutput = predictionOutput;
    }
}