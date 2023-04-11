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
public class AppendEntryEventListener extends BaseListener<AppendEntryEvent> {

    // append entries are handled by follower

    @Autowired
    public AppendEntryEventListener(ClusterState clusterState, Network network, RaftConfig config) {

        super(clusterState, network, config);
    }

    @Override
    public void handleEvent(AppendEntryEvent event) {

        Entry<AppendEntry, Node> entry = (Entry<AppendEntry, Node>) event.getSource();

        AppendEntry appendEntry = entry.getKey();
        Node leaderNode = entry.getValue();

        this.getClusterState().setLeaderActive();

        // only follower receives the appends entry
        if (!this.getClusterState().isFollower()) {
            throw new RaftException("only follower appends the entry, current state is " + this.getClusterState().getCurrentRole().name());
        }

        if (!this.getClusterState().canAppendLogEntryGivenTerm(appendEntry.getCurrentTerm())) {
            return;
        }

        boolean success = this.getClusterState().appendEntry(appendEntry);

        if (!success) {
            log.warn("could not append the entry " + appendEntry);
        }

        this.getClusterState().applyCommittedIndex(appendEntry.getLeaderCommitIndex());

        AppendEntryResponse appendEntryResponse = AppendEntryResponse.builder()
          .currentTerm(this.getClusterState().getCurrentTerm())
          .success(success)
          .matchIndex(success ? this.getClusterState().getMatchIndex() : -1)
          .build();

        this.getNetwork().sendTo(NetworkMessage.builder()
          .appendEntryResponse(appendEntryResponse)
          .messageType(MessageType.APPEND_ENTRY_RESPONSE)
          .destination(leaderNode)
          .source(this.getConfig().getCurrentNodeConfig())
          .build());

    }
}
