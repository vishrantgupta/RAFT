package org.consensus.raft.event;

import java.util.Map.Entry;
import org.consensus.raft.bean.RequestVote;
import org.consensus.raft.config.Node;
import org.springframework.context.ApplicationEvent;

public class RequestVoteEvent extends ApplicationEvent {

    public RequestVoteEvent(Entry<RequestVote, Node> entry) {
        super(entry);
    }
}
