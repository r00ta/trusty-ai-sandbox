package com.redhat.developer.execution.models;

import com.fasterxml.jackson.annotation.JsonProperty;

public enum OutcomeEvaluationStatusModel {
    @JsonProperty("NOT_EVALUATED")
    NOT_EVALUATED,
    @JsonProperty("EVALUATING")
    EVALUATING,
    @JsonProperty("SUCCEEDED")
    SUCCEEDED,
    @JsonProperty("SKIPPED")
    SKIPPED,
    @JsonProperty("FAILED")
    FAILED
}

