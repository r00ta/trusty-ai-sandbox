package com.redhat.developer.replay.kafka;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import com.redhat.developer.replay.IReplayService;
import com.redhat.developer.replay.kafka.messaging.dto.ReplayEventDto;
import com.redhat.developer.replay.models.ReplayRequest;
import org.eclipse.microprofile.reactive.messaging.Incoming;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@ApplicationScoped
public class KafkaConsumer extends KafkaAbstractConsumer {

    private static final Logger LOGGER = LoggerFactory.getLogger(KafkaConsumer.class);

    @Inject
    IReplayService replayService;

    @Override
    @Incoming("kogito-replay-request")
    public void onProcessInstanceEvent(ReplayEventDto event) {
        super.onProcessInstanceEvent(event);
    }

    protected void processEvent(ReplayEventDto event) {
        LOGGER.debug("Processing a new replay request");
        try{
            ReplayRequest replayRequest = ReplayRequest.fromKafkaCloudEvent(event.data);
            replayService.processRequest(replayRequest);
        }
        catch (Exception e){
            LOGGER.warn("error", e);
        }
    }
}
