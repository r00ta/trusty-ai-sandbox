package com.redhat.developer.decision.responses;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

public class ExecutionResponse {
    @JsonProperty("total")
    public int total;

    @JsonProperty("limit")
    public int limit;

    @JsonProperty("offset")
    public int offset;

    @JsonProperty("data")
    public List<ExecutionHeaderResponse> data;

    public ExecutionResponse(int total, int returnedRecords, int offset, List<ExecutionHeaderResponse> executionResponse){
        this.total = total;
        this.limit = returnedRecords;
        this.offset = offset;
        this.data = executionResponse;
    }
}
