package com.redhat.developer;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.enterprise.context.ApplicationScoped;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.redhat.developer.model.Feature;
import com.redhat.developer.model.FeatureImportance;
import com.redhat.developer.model.Output;
import com.redhat.developer.model.Prediction;
import com.redhat.developer.model.PredictionInput;
import com.redhat.developer.model.PredictionOutput;
import com.redhat.developer.model.Saliency;
import com.redhat.developer.model.Type;
import com.redhat.developer.model.Value;
import com.redhat.developer.requests.TypedData;
import com.redhat.developer.utils.DataUtils;
import com.redhat.developer.utils.HttpHelper;
import com.redhat.developer.utils.LinearModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@ApplicationScoped
public class LIMEishSaliencyExplanationProvider {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    private static final HttpHelper httpHelper = new HttpHelper("http://producer:1337/");

    /**
     * no. of samples to be generated for the local linear model training
     */
    private final int noOfSamples;

    /**
     * no. of perturbations to perform on a prediction
     */
    private final int noOfPerturbations;

    public LIMEishSaliencyExplanationProvider(int noOfSamples, int noOfPerturbations) {
        this.noOfSamples = noOfSamples;
        this.noOfPerturbations = noOfPerturbations;
    }

    public LIMEishSaliencyExplanationProvider(int noOfSamples) {
        this(noOfSamples, 1);
    }

    public LIMEishSaliencyExplanationProvider() {
        this(100, 1);
    }

    public Saliency explain(List<TypedData> inputs, List<TypedData> outputs, String modelName) {
        long start = System.currentTimeMillis();

        Prediction prediction = convert(inputs, outputs);
        List<FeatureImportance> saliencies = new LinkedList<>();
        PredictionInput predictionInput = prediction.getInput();
        List<Feature> features = predictionInput.getFeatures();
        List<Output> actualOutputs = prediction.getOutput().getOutputs();
        int noOfFeatures = features.size();
        double[] weights = new double[noOfFeatures];
        for (int o = 0; o < actualOutputs.size(); o++) {
            Collection<Prediction> training = new LinkedList<>();
            boolean separableDataset = false;

            List<PredictionInput> perturbedInputs = new LinkedList<>();
            List<PredictionOutput> predictionOutputs = new LinkedList<>();

            boolean classification = false;
            int sampleSize = noOfSamples;
            int tries = 3;
            Map<Double, Long> rawClassesBalance = new HashMap<>();
            while (!separableDataset && tries > 0) {
                List<PredictionInput> perturbed = getPerturbedInputs(predictionInput, noOfFeatures, sampleSize);
                List<PredictionOutput> perturbedOutputs = predict(perturbed, inputs, outputs, modelName);

                rawClassesBalance = perturbedOutputs.stream().map(p -> p.getOutputs().get(0).getValue()
                        .asNumber()).collect(Collectors.groupingBy(Double::doubleValue, Collectors.counting()));
                logger.debug("raw samples per class: {}", rawClassesBalance);

                if (rawClassesBalance.size() > 1) {
                    Long max = rawClassesBalance.values().stream().max(Long::compareTo).get();
                    if ((double) max / (double) perturbed.size() < 0.9) {
                        separableDataset = true;
                        perturbedInputs.addAll(perturbed);
                        predictionOutputs.addAll(perturbedOutputs);
                        classification = rawClassesBalance.size() == 2;
                    }
                } else {
                    sampleSize *= 2;
                    tries--;
                }
            }
            if (!separableDataset) {
                logger.warn("the perturbed inputs / outputs dataset is not (easily) separable: {}", rawClassesBalance);
            }
            for (int i = 0; i < perturbedInputs.size(); i++) {
                Output output = predictionOutputs.get(i).getOutputs().get(o);
                Prediction perturbedDataPrediction = new Prediction(perturbedInputs.get(i), new PredictionOutput(List.of(output)));
                training.add(perturbedDataPrediction);
            }

            DataUtils.encodeFeatures(training, prediction);

            double[] sampleWeights = getSampleWeights(prediction, noOfFeatures, training);

            LinearModel linearModel = new LinearModel(noOfFeatures, classification, sampleWeights);
            linearModel.fit(training);
            for (int i = 0; i < weights.length; i++) {
                weights[i] += linearModel.getWeights()[i] / (double) outputs.size();
            }
            logger.debug("weights updated for output {}", outputs.get(o).value);
        }
        for (int i = 0; i < weights.length; i++) {
            FeatureImportance featureImportance = new FeatureImportance(features.get(i), weights[i]);
            saliencies.add(featureImportance);
        }
        long end = System.currentTimeMillis();
        logger.info("explanation time: {}ms", (end - start));
        return new Saliency(saliencies);
    }

    private List<PredictionInput> getPerturbedInputs(PredictionInput predictionInput, int noOfFeatures, int noOfSamples) {
        List<PredictionInput> perturbedInputs = new LinkedList<>();
        double perturbedDataSize = Math.max(noOfSamples, Math.pow(2, noOfFeatures));
        for (int i = 0; i < perturbedDataSize; i++) {
            perturbedInputs.add(DataUtils.perturbDrop(predictionInput, noOfSamples, this.noOfPerturbations));
        }
        return perturbedInputs;
    }

    private double[] getSampleWeights(Prediction prediction, int noOfFeatures, Collection<Prediction> training) {
        double[] x = new double[noOfFeatures];
        Arrays.fill(x, 1);

        return training.stream().map(Prediction::getInput).map(DataUtils::toNumbers).map(
                d -> DataUtils.euclidean(x, d)).map(d -> DataUtils.exponentialSmoothingKernel(d, 0.75 *
                Math.sqrt(noOfFeatures))).mapToDouble(Double::doubleValue).toArray();
    }

    protected List<PredictionOutput> predict(List<PredictionInput> perturbedInputs, List<TypedData> originalInput,
                                             List<TypedData> originalOutputs, String modelName) {
        List<PredictionOutput> result = new ArrayList<>();
        for (PredictionInput perturbatedInput : perturbedInputs) {
            String request = perturbatedInput.toKogitoRequestJson(originalInput).toString();
            String response = null;
            try {
                response = httpHelper.doPost("/" + modelName + "?tracing=false", request);
            } catch (IOException e) {
                e.printStackTrace();
            }
            logger.info(request);
            Map<String, Object> outcome = null;
            try {
                outcome = new ObjectMapper().readValue(response, new HashMap<String, Object>().getClass());
            } catch (JsonProcessingException e) {
                e.printStackTrace();
            }
            result.add(new PredictionOutput(flattenDmnResult(outcome, originalOutputs.stream().map(x -> x.inputName).collect(Collectors.toList()))));
        }
        return result;
    }

    private List<Output> flattenDmnResult(Map<String, Object> dmnResult, List<String> validOutcomeNames) {
        List<Output> result = new ArrayList<>();
        dmnResult.entrySet().stream().filter(x -> validOutcomeNames.contains(x.getKey())).forEach(x -> result.addAll(flattenOutput(x.getKey(), x.getValue())));
        return result;
    }

    private List<Output> flattenOutput(String key, Object value) {
        List<Output> result = new ArrayList<>();
        if (value instanceof Double || value instanceof Float) {
            result.add(new Output(key, Type.NUMBER, new Value<>((Double) value), 0));
            return result;
        }

        if (value instanceof Integer) {
            result.add(new Output(key, Type.NUMBER, new Value<>((Integer) value), 0));
            return result;
        }

        if (value instanceof Boolean) {
            Boolean vv = (Boolean) value;
            result.add(new Output(key, Type.NUMBER, new Value<>(vv ? 1d : 0d), 0));
            return result;
        }

        if (value instanceof String) {
            result.add(new Output(key, Type.STRING, new Value<>((String) value), 0));
            return result;
        }

        Map<String, Object> aa = (Map) value;

        aa.entrySet().forEach(x -> result.addAll(flattenOutput(x.getKey(), x.getValue())));

        return result;
    }

    private Prediction convert(List<TypedData> inputs, List<TypedData> outputs) {
        PredictionInput predictionInput = new PredictionInput(extractInputFeatures(inputs));
        PredictionOutput predictionOutput = new PredictionOutput(extractOutputs(outputs));
        return new Prediction(predictionInput, predictionOutput);
    }

    private List<Feature> extractInputFeatures(List<TypedData> data) {
        List<Feature> features = new ArrayList<>();
        for (TypedData input : data) {
            List<Feature> result = getFlatBuiltInInput(input);
            features.addAll(result);
        }
        return features;
    }

    private List<Output> extractOutputs(List<TypedData> data) {
        List<Output> features = new ArrayList<>();
        for (TypedData input : data) {
            List<Output> result = getFlatBuiltInOutputs(input);
            features.addAll(result);
        }
        return features;
    }

    private List<Output> getFlatBuiltInOutputs(TypedData input) {
        List<Output> features = new ArrayList<>();
        if (input.typeRef.equals("string")) {
            features.add(new Output(input.inputName, Type.STRING, new Value<>((String) input.value), 0));
            return features;
        }
        if (input.typeRef.equals("number")) {
            features.add(new Output(input.inputName, Type.NUMBER, new Value<>(Double.valueOf(String.valueOf(input.value))), 0));
            return features;
        }
        if (input.typeRef.equals("boolean")) {
            features.add(new Output(input.inputName, Type.BOOLEAN, new Value<>((Boolean) input.value), 0));
            return features;
        }

        input.components.forEach(x -> features.addAll(getFlatBuiltInOutputs(x)));
        return features;
    }

    private List<Feature> getFlatBuiltInInput(TypedData input) {
        List<Feature> features = new ArrayList<>();
        if (input.typeRef.equals("string")) {
            features.add(new Feature(input.inputName, Type.STRING, new Value<>((String) input.value)));
            return features;
        }
        if (input.typeRef.equals("number")) {
            features.add(new Feature(input.inputName, Type.NUMBER, new Value<>(Double.valueOf(String.valueOf(input.value)))));
            return features;
        }
        input.components.forEach(x -> features.addAll(getFlatBuiltInInput(x)));
        return features;
    }
}
