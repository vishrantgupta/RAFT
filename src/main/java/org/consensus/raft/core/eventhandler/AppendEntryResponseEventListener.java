package org.consensus.raft.core.eventhandler;

import java.util.Map.Entry;
import lombok.extern.slf4j.Slf4j;
import org.consensus.raft.bean.AppendEntryResponse;
import org.consensus.raft.config.Node;
import org.consensus.raft.config.RaftConfig;
import org.consensus.raft.core.state.ClusterState;
import org.consensus.raft.event.AppendEntryResponseEvent;
import org.consensus.raft.exception.RaftException;
import org.consensus.raft.network.Network;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class AppendEntryResponseEventListener extends BaseListener<AppendEntryResponseEvent> {

    @Autowired
    public AppendEntryResponseEventListener(ClusterState clusterState, Network network, RaftConfig config) {
        super(clusterState, network, config);
    }

    @Override
    public void handleEvent(AppendEntryResponseEvent event) {

        Entry<AppendEntryResponse, Node> entry = (Entry<AppendEntryResponse, Node>) event.getSource();

        Node originator = entry.getValue();
        AppendEntryResponse appendEntryResponse = entry.getKey();

        // only leader receives append entry response
        if (!this.getClusterState().isLeader()) {
            throw new RaftException("not a leader");
        }

        if (appendEntryResponse.isSuccess()) {

            // the append in the follower node was successful; incrementing the index
            this.getClusterState().updateFollowersNextIndex(originator, appendEntryResponse.getMatchIndex());
            this.getClusterState().updateFollowersMatchIndex(originator, appendEntryResponse.getMatchIndex());

            this.getClusterState().setCommitIndex(appendEntryResponse.getCurrentTerm());

        } else {
            this.getClusterState().decrementFollowerNextIndex(originator);
        }

    }

}
