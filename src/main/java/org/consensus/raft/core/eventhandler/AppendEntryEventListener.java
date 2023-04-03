package org.consensus.raft.core.eventhandler;

import java.util.Map.Entry;
import lombok.extern.slf4j.Slf4j;
import org.consensus.raft.bean.AppendEntry;
import org.consensus.raft.bean.AppendEntryResponse;
import org.consensus.raft.config.Node;
import org.consensus.raft.config.RaftConfig;
import org.consensus.raft.core.state.ClusterState;
import org.consensus.raft.event.AppendEntryEvent;
import org.consensus.raft.exception.RaftException;
import org.consensus.raft.network.MessageType;
import org.consensus.raft.network.Network;
import org.consensus.raft.network.NetworkMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class AppendEntryEventListener implements BaseListener<AppendEntryEvent> {

    // append entries are handled by follower

    private final ClusterState clusterState;

    private final Network network;
    private final RaftConfig config;

    @Autowired
    public AppendEntryEventListener(ClusterState clusterState, Network network, RaftConfig config) {
        this.clusterState = clusterState;
        this.network = network;
        this.config = config;
    }

    @Override
    public ClusterState getClusterState() {
        return this.clusterState;
    }

    @Override
    public void onApplicationEvent(AppendEntryEvent event) {

        Entry<AppendEntry, Node> entry = (Entry<AppendEntry, Node>) event.getSource();

        AppendEntry appendEntry = entry.getKey();
        Node raftNode = entry.getValue();

        log.debug("handling append entry " + appendEntry);

        termHandler(appendEntry);

        // only follower receives the appends entry
        if (!this.clusterState.isFollower()) {
            throw new RaftException("only follower appends the entry, current state is " + this.clusterState.getRole().name());
        }

        clusterState.setLeaderActive();

        if (!this.clusterState.canAppendLogEntryGivenTerm(appendEntry.getCurrentTerm())) {
            return;
        }

        boolean success = this.clusterState.appendEntry(appendEntry);

        if (!success) {
            log.warn("could not append the entry " + appendEntry);
        }

        this.clusterState.applyCommittedIndex(appendEntry.getLeaderCommitIndex());

        AppendEntryResponse appendEntryResponse = AppendEntryResponse.builder()
          .currentTerm(this.clusterState.getCurrentTerm())
          .success(success)
          .matchIndex(success ? this.clusterState.getMatchIndex() : -1)
          .build();

        network.sendTo(NetworkMessage.builder()
          .appendEntryResponse(appendEntryResponse)
          .messageType(MessageType.APPEND_ENTRY_RESPONSE)
          .destination(raftNode)
          .source(config.getCurrentNodeConfig())
          .build());

    }
}
