package com.redhat.developer.replay.kafka;

import java.util.concurrent.CompletableFuture;

import com.redhat.developer.replay.kafka.messaging.dto.ReplayEventDto;

public abstract class KafkaAbstractConsumer {

    protected void onProcessInstanceEvent(ReplayEventDto event) {
        CompletableFuture.runAsync(() -> {
            processEvent(event);
        });
    }

    protected abstract void processEvent(ReplayEventDto event);
}
