package com.redhat.developer.replay;

import com.redhat.developer.replay.models.ReplayRequest;

public interface IReplayService {

    void processRequest(ReplayRequest request);
}
