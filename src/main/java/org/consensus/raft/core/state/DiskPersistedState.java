package org.consensus.raft.core.state;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.consensus.raft.config.Node;
import org.consensus.raft.log.LogEntry;
import org.consensus.raft.log.WriteAheadLog;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@EqualsAndHashCode(callSuper = false)
@Data
@Component
class DiskPersistedState implements RaftState {

    private final WriteAheadLog<LogEntry> log;

    // latest term server has seen (initialized to 0 on first boot, increases monotonically)
    private volatile int currentTerm;

    // candidateId that received vote in current term (or null if none)
    private volatile Node votedFor;

    @Autowired
    public DiskPersistedState(WriteAheadLog<LogEntry> log) {
        this.log = log;
    }

    @Override
    public void reset() {
        this.currentTerm = 0;
        this.votedFor = null;
    }
}
