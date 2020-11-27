package com.redhat.developer.replay.models;

import com.redhat.developer.replay.kafka.messaging.dto.ReplayRequestDto;

public class ReplayRequest {

    public static ReplayRequest fromKafkaCloudEvent(ReplayRequestDto dto){
        return new ReplayRequest();
    }
}
