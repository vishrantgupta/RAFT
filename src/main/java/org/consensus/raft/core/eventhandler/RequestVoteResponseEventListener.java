package org.consensus.raft.core.eventhandler;

import java.util.Map.Entry;
import lombok.extern.slf4j.Slf4j;
import org.consensus.raft.bean.RequestVoteResponse;
import org.consensus.raft.config.Node;
import org.consensus.raft.config.RaftConfig;
import org.consensus.raft.core.state.ClusterState;
import org.consensus.raft.event.RequestVoteResponseEvent;
import org.consensus.raft.network.Network;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class RequestVoteResponseEventListener extends BaseListener<RequestVoteResponseEvent> {

    @Autowired
    public RequestVoteResponseEventListener(ClusterState clusterState, Network network, RaftConfig config) {
        super(clusterState, network, config);
    }

    @Override
    public void handleEvent(RequestVoteResponseEvent event) {

        Entry<RequestVoteResponse, Node> entry = (Entry<RequestVoteResponse, Node>) event.getSource();

        Node raftNode = entry.getValue();
        RequestVoteResponse requestVoteResponse = entry.getKey();

        log.debug("handling request vote response " + requestVoteResponse);

        if (requestVoteResponse.getCurrentTerm() < this.getClusterState().getCurrentTerm()) {
            return;
        }

        if (this.getClusterState().isCandidate()
          || this.getClusterState().isLeader()) { // a candidate might already transition to leader, in that just just register the node
            this.getClusterState().registerVote(raftNode, requestVoteResponse.isVoteGranted());
        }

        // transition to leader only if current state is a candidate
        if (this.getClusterState().isCandidate()) {
            if (this.getClusterState().acquiredMajorityVoteToBecomeLeader()) {
                this.getClusterState().transitionToLeader();
            }
        }
    }
}
