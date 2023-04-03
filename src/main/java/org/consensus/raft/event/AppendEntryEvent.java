package org.consensus.raft.event;

import java.util.Map.Entry;
import org.consensus.raft.bean.AppendEntry;
import org.consensus.raft.config.Node;
import org.springframework.context.ApplicationEvent;

public class AppendEntryEvent extends ApplicationEvent {

    public AppendEntryEvent(Entry<AppendEntry, Node> entry) {
        super(entry);
    }
}
