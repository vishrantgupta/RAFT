package org.consensus.raft.core.eventhandler;

import java.util.Map.Entry;
import lombok.extern.slf4j.Slf4j;
import org.consensus.raft.bean.AppendEntryResponse;
import org.consensus.raft.config.Node;
import org.consensus.raft.core.state.ClusterState;
import org.consensus.raft.event.AppendEntryResponseEvent;
import org.consensus.raft.exception.RaftException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class AppendEntryResponseEventListener implements BaseListener<AppendEntryResponseEvent> {

    private final ClusterState clusterState;

    @Autowired
    public AppendEntryResponseEventListener(ClusterState clusterState) {
        this.clusterState = clusterState;
    }

    @Override
    public ClusterState getClusterState() {
        return this.clusterState;
    }

    @Override
    public void onApplicationEvent(AppendEntryResponseEvent event) {

        Entry<AppendEntryResponse, Node> entry = (Entry<AppendEntryResponse, Node>) event.getSource();

        Node originator = entry.getValue();
        AppendEntryResponse appendEntryResponse = entry.getKey();

        termHandler(appendEntryResponse);

        // log.info("handling append entry response " + appendEntryResponse + " originator " + originator);

        // only leader receives append entry response
        if (!this.clusterState.isLeader()) {
            throw new RaftException("not a leader");
        }

        if (appendEntryResponse.isSuccess()) {

            // the append in the follower node was successful; incrementing the index
            this.clusterState.updateFollowersNextIndex(originator, appendEntryResponse.getMatchIndex());
            this.clusterState.updateFollowersMatchIndex(originator, appendEntryResponse.getMatchIndex());

            this.clusterState.setCommitIndex(appendEntryResponse.getCurrentTerm());

        } else {
            this.clusterState.decrementFollowerNextIndex(originator);
        }

    }

}
