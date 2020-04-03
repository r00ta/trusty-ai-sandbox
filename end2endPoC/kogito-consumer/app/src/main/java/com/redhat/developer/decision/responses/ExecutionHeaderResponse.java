package com.redhat.developer.decision.responses;

import java.util.Date;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.redhat.developer.decision.storage.model.ExecutionEnum;

public class ExecutionHeaderResponse {
    @JsonProperty("executionId")
    public String executionId;

    @JsonProperty("executionDate")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS")
    public Date executionDate;

    @JsonProperty("executionSucceeded")
    public boolean executionSucceeded;

    @JsonProperty("executorName")
    public String executorName;

    @JsonProperty("executionType")
    public ExecutionEnum executionType = ExecutionEnum.DECISION;

    public ExecutionHeaderResponse(String executionId, Date executionDate, boolean executionSucceeded, String executorName){
        this.executionId = executionId;
        this.executionDate = executionDate;
        this.executionSucceeded = executionSucceeded;
        this.executorName = executorName;
    }
}
