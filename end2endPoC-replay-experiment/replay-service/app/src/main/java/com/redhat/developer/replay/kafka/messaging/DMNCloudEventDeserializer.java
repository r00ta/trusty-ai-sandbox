package com.redhat.developer.replay.kafka.messaging;

import com.redhat.developer.replay.kafka.messaging.dto.ReplayEventDto;

public class DMNCloudEventDeserializer extends AbstractCloudEventDeserializer<ReplayEventDto> {

    public DMNCloudEventDeserializer() {
        super(ReplayEventDto.class);
    }
}
