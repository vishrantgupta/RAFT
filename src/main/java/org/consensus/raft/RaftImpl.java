package org.consensus.raft;

import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.consensus.raft.bean.AppendEntry;
import org.consensus.raft.config.RaftConfig;
import org.consensus.raft.core.state.ClusterState;
import org.consensus.raft.core.state.RaftRole;
import org.consensus.raft.exception.RaftException;
import org.consensus.raft.log.LogEntry;
import org.consensus.raft.network.MessageType;
import org.consensus.raft.network.Network;
import org.consensus.raft.network.NetworkMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class RaftImpl implements Consensus {

    private final ClusterState clusterState;
    private final RaftConfig config;
    private final Network network;

    @Autowired
    public RaftImpl(ClusterState clusterState, RaftConfig config, Network network) {
        this.clusterState = clusterState;
        this.config = config;
        this.network = network;
    }

    @Override
    public synchronized boolean getConsensus(Object command) {

        if (!this.clusterState.isLeader()) {
            throw new RaftException("not a leater, can only append when the state is " + RaftRole.LEADER.name() + ", current state is " + this.clusterState.getCurrentRole().name());
        }

        LogEntry logEntry = new LogEntry(this.clusterState.getCurrentTerm(), command);

        int previousLogIndex = this.clusterState.getMatchIndex();

        AppendEntry entry = AppendEntry.builder()
          .currentTerm(this.clusterState.getCurrentTerm())
          .leaderId(this.config.getCurrentNodeConfig().getId())
          .prevLogIndex(previousLogIndex).prevLogTerm(this.clusterState.getLastLogTerm())
          .leaderCommitIndex(this.clusterState.getCommitIndex())
          .entries(List.of(logEntry))
          .build();

        // this should always work as it's happening on leader
        boolean leaderAppended = this.clusterState.appendEntry(entry);

        if (leaderAppended) {
            network.broadcast(NetworkMessage.builder()
              .appendEntry(entry)
              .messageType(MessageType.APPEND_ENTRY)
              .source(config.getCurrentNodeConfig())
              .build());
        }

        log.debug("Appended on leader " + leaderAppended + " waiting for consensus");

        long startTime = System.currentTimeMillis();
        long timeout = config.getConsensusTimeoutMillis(); // 10 seconds in milliseconds

        while (this.clusterState.getCommitIndex() > previousLogIndex + 1) {
            if (System.currentTimeMillis() - startTime > timeout) {
                // timeout exceeded, do something
                throw new RaftException("timed out waiting for consensus");
            }
            // wait
        }

        log.debug("Got the consensus for command " + command);

        return true;
    }

}
