package com.redhat.developer.model.dmn;

import java.util.ArrayList;
import java.util.List;

import com.redhat.developer.model.Feature;
import com.redhat.developer.model.FeatureFactory;
import com.redhat.developer.model.Output;
import com.redhat.developer.model.Prediction;
import com.redhat.developer.model.PredictionInput;
import com.redhat.developer.model.PredictionOutput;
import com.redhat.developer.model.Type;
import com.redhat.developer.model.Value;

/**
 * Utility class to handle DMN types for input and output.
 */
public class DMNUtils {

    public static Prediction convert(List<TypedData> inputs, List<TypedData> outputs) {
        PredictionInput predictionInput = new PredictionInput(extractInputFeatures(inputs));
        PredictionOutput predictionOutput = new PredictionOutput(extractOutputs(outputs));
        return new Prediction(predictionInput, predictionOutput);
    }

    public static List<Feature> extractInputFeatures(List<TypedData> data) {
        List<Feature> features = new ArrayList<>();
        for (TypedData input : data) {
            List<Feature> result = getFlatBuiltInInput(input);
            features.addAll(result);
        }
        return features;
    }

    public static List<Output> extractOutputs(List<TypedData> data) {
        List<Output> features = new ArrayList<>();
        for (TypedData input : data) {
            List<Output> result = getFlatBuiltInOutputs(input);
            features.addAll(result);
        }
        return features;
    }

    public static List<Output> getFlatBuiltInOutputs(TypedData input) {
        List<Output> features = new ArrayList<>();
        if (input.typeRef != null && input.typeRef.equals("string")) {
            features.add(new Output(input.inputName, Type.TEXT, new Value<>((String) input.value), 0));
            return features;
        }
        if (input.typeRef != null && input.typeRef.equals("number")) {
            features.add(new Output(input.inputName, Type.NUMBER, new Value<>(Double.valueOf(String.valueOf(input.value))), 0));
            return features;
        }
        if (input.typeRef != null && input.typeRef.equals("boolean")) {
            features.add(new Output(input.inputName, Type.BOOLEAN, new Value<>((Boolean) input.value), 0));
            return features;
        }

        if (input.components != null) {
            input.components.forEach(x -> features.addAll(getFlatBuiltInOutputs(x)));
            return features;
        } else {
            return features;
        }
    }

    public static List<Feature> getFlatBuiltInInput(TypedData input) {
        List<Feature> features = new ArrayList<>();
        if (input.typeRef != null && input.typeRef.equals("string")) {
            features.add(FeatureFactory.newTextFeature(input.inputName, (String) input.value));
            return features;
        }
        if (input.typeRef != null && input.typeRef.equals("number")) {
            features.add(FeatureFactory.newNumericalFeature(input.inputName, Double.valueOf(String.valueOf(input.value))));
            return features;
        }
        if (input.components != null) {
            input.components.forEach(x -> features.addAll(getFlatBuiltInInput(x)));
            return features;
        } else {
            return features;
        }
    }
}
