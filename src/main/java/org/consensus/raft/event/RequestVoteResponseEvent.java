package org.consensus.raft.event;

import java.util.Map.Entry;
import org.consensus.raft.bean.RequestVoteResponse;
import org.consensus.raft.config.Node;
import org.springframework.context.ApplicationEvent;

public class RequestVoteResponseEvent extends ApplicationEvent {

    public RequestVoteResponseEvent(Entry<RequestVoteResponse, Node> entry) {
        super(entry);
    }
}
