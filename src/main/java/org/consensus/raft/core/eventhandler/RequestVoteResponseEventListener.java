package org.consensus.raft.core.eventhandler;

import java.util.Map.Entry;
import lombok.extern.slf4j.Slf4j;
import org.consensus.raft.bean.RequestVoteResponse;
import org.consensus.raft.config.Node;
import org.consensus.raft.core.state.ClusterState;
import org.consensus.raft.event.RequestVoteResponseEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class RequestVoteResponseEventListener implements BaseListener<RequestVoteResponseEvent> {

    private final ClusterState clusterState;

    @Autowired
    public RequestVoteResponseEventListener(ClusterState clusterState) {
        this.clusterState = clusterState;
    }

    @Override
    public ClusterState getClusterState() {
        return this.clusterState;
    }

    @Override
    public void onApplicationEvent(RequestVoteResponseEvent event) {

        Entry<RequestVoteResponse, Node> entry = (Entry<RequestVoteResponse, Node>) event.getSource();

        Node raftNode = entry.getValue();
        RequestVoteResponse requestVoteResponse = entry.getKey();

        log.debug("handling request vote response " + requestVoteResponse);

        termHandler(requestVoteResponse);

        if (requestVoteResponse.getCurrentTerm() < this.clusterState.getCurrentTerm()) {
            return;
        }

        if (this.clusterState.isCandidate()) {
            this.clusterState.registerVote(raftNode, requestVoteResponse.isVoteGranted());

            if (this.clusterState.acquiredMajorityVoteToBecomeLeader()) {
                this.clusterState.transitionToLeader();
            }
        }
    }
}
