package com.redhat.developer.replay.kafka.messaging.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public class ReplayEventDto {

    @JsonProperty("data")
    public ReplayRequestDto data;

    @JsonProperty("id")
    public String id;

    @JsonProperty("source")
    public String source;

    @JsonProperty("specversion")
    public String specversion;

    @JsonProperty("type")
    public String type;
}
