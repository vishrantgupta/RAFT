package org.consensus.raft.event;

import java.util.Map.Entry;
import org.consensus.raft.bean.AppendEntryResponse;
import org.consensus.raft.config.Node;
import org.springframework.context.ApplicationEvent;

public class AppendEntryResponseEvent extends ApplicationEvent {

    public AppendEntryResponseEvent(Entry<AppendEntryResponse, Node> entry) {
        super(entry);
    }
}
