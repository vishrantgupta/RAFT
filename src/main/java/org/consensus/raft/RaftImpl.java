package org.consensus.raft;

import org.consensus.raft.bean.AppendEntry;
import org.consensus.raft.config.RaftConfig;
import org.consensus.raft.core.state.ClusterState;
import org.consensus.raft.core.state.RaftRole;
import org.consensus.raft.exception.RaftException;
import org.consensus.raft.log.LogEntry;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class RaftImpl implements Consensus {

    private final ClusterState clusterState;
    private final RaftConfig config;

    @Autowired
    public RaftImpl(ClusterState clusterState, RaftConfig config) {
        this.clusterState = clusterState;
        this.config = config;
    }

    @Override
    public boolean getConsensus(Object command) {

        System.out.println("got a command " + command);

        if (!this.clusterState.isLeader()) {
            System.out.println("not a leader");
            throw new RaftException("not a leater, can only append when the state is " + RaftRole.LEADER.name() + ", current state is " + this.clusterState.getCurrentRole().name());
        }

        LogEntry logEntry = new LogEntry(this.clusterState.getCurrentTerm(), command);

        int previousLogIndex = this.clusterState.getMatchIndex();

        AppendEntry entry = AppendEntry.builder()
          .currentTerm(this.clusterState.getCurrentTerm())
          .leaderId(this.config.getCurrentNodeConfig().getId())
          .prevLogIndex(previousLogIndex).prevLogTerm(this.clusterState.getLastLogTerm())
          .leaderCommitIndex(this.clusterState.getCommitIndex())
          .entries(new LogEntry[]{logEntry})
          .build();

        // this should always work as it's happening on leader
        boolean leaderAppended = this.clusterState.appendEntry(entry);

        System.out.println("Appended on leader " + leaderAppended + " waiting for consensus");

        while (this.clusterState.getCommitIndex() > previousLogIndex + 1) {
            // wait
        }

        // store.apply(command);

        System.out.println("got the consensus for command " + command);

        return true;
    }

}
