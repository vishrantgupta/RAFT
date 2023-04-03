package org.consensus.raft.timer;

import java.util.List;
import org.consensus.raft.bean.AppendEntry;
import org.consensus.raft.config.Node;
import org.consensus.raft.config.RaftConfig;
import org.consensus.raft.core.state.ClusterState;
import org.consensus.raft.core.state.RaftRole;
import org.consensus.raft.log.LogEntry;
import org.consensus.raft.network.MessageType;
import org.consensus.raft.network.Network;
import org.consensus.raft.network.NetworkMessage;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

@EnableScheduling
@Configuration
public class RaftHeartbeatSchedule {

    private final ClusterState clusterState;
    private final RaftConfig config;
    private final Network network;

    public RaftHeartbeatSchedule(ClusterState clusterState, RaftConfig config, Network network) {
        this.config = config;
        this.network = network;
        this.clusterState = clusterState;
    }

    @Scheduled(fixedDelay = 50)
    public void heartbeat() {
        // the update to followers only happens from the leader
        if (this.clusterState.getRole() == RaftRole.LEADER) {

            for (Node followerNode : this.config.otherNodes()) {

                List<LogEntry> logEntries = this.clusterState.getFollowerLogEntries(followerNode);

                int prevLogIndex = this.clusterState.getFollowerNextIndexByNodeId(followerNode) - 1;
                int prevLogTerm = this.clusterState.getLogTermByIndex(prevLogIndex);

                AppendEntry entry = AppendEntry.builder()
                  .currentTerm(this.clusterState.getCurrentTerm())
                  .leaderId(this.config.getCurrentNodeConfig().getId())
                  .prevLogIndex(prevLogIndex).prevLogTerm(prevLogTerm)
                  .leaderCommitIndex(this.clusterState.getCommitIndex())
                  .entries(logEntries.toArray(new LogEntry[logEntries.size()]))
                  .build();

                network.broadcast(NetworkMessage.builder()
                  .appendEntry(entry)
                  .messageType(MessageType.APPEND_ENTRY)
                  .source(config.getCurrentNodeConfig())
                  .build());
            }
        }
    }

}
