package org.consensus.raft.core.eventhandler;

import java.util.Map.Entry;
import lombok.extern.slf4j.Slf4j;
import org.consensus.raft.bean.RequestVote;
import org.consensus.raft.bean.RequestVoteResponse;
import org.consensus.raft.config.Node;
import org.consensus.raft.config.RaftConfig;
import org.consensus.raft.core.state.ClusterState;
import org.consensus.raft.event.RequestVoteEvent;
import org.consensus.raft.network.MessageType;
import org.consensus.raft.network.Network;
import org.consensus.raft.network.NetworkMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class RequestVoteEventListener implements BaseListener<RequestVoteEvent> {

    private final Network network;
    private final RaftConfig config;

    private final ClusterState clusterState;

    @Autowired
    public RequestVoteEventListener(Network network, RaftConfig config, ClusterState clusterState) {
        this.network = network;
        this.config = config;
        this.clusterState = clusterState;
    }

    @Override
    public ClusterState getClusterState() {
        return this.clusterState;
    }

    @Override
    public void onApplicationEvent(RequestVoteEvent event) {
        // another node has called for the leader election
        // this event will respond back if it votes for the new candidate or not

        Entry<RequestVote, Node> entry = (Entry<RequestVote, Node>) event.getSource();

        RequestVote requestVote = entry.getKey();
        Node candidateNode = entry.getValue();

        termHandler(requestVote);

        log.debug("handling request vote " + requestVote);

        boolean myVote = true;

        boolean currentNodeTermIsHigher = this.clusterState.getCurrentTerm() > requestVote.getCurrentTerm();
        boolean alreadyVotedForAnotherNode = this.clusterState.getVotedFor() != null && this.clusterState.getVotedFor() != requestVote.getCandidateId();
        boolean currentNodeLogTermIsHigher = this.clusterState.getLastLogTerm() > requestVote.getLastLogTerm();
        boolean currentNodeHasHigherLogEntries = this.clusterState.getLastLogTerm() == requestVote.getLastLogTerm() && this.clusterState.getLastLogIndex() > requestVote.getLastLogIndex();

        if (currentNodeTermIsHigher // current term is higher
          || alreadyVotedForAnotherNode // already voted for someone else
          || currentNodeLogTermIsHigher // this node last term index is higher
          || currentNodeHasHigherLogEntries // the log size is higher in current term
        ) {
            myVote = false;
        }

        RequestVoteResponse requestVoteResponse = RequestVoteResponse.builder()
          .currentTerm(this.clusterState.getCurrentTerm())
          .voteGranted(myVote)
          .build();

        NetworkMessage message = NetworkMessage.builder()
          .requestVoteResponse(requestVoteResponse)
          .messageType(MessageType.REQUEST_VOTE_RESPONSE)
          .destination(candidateNode)
          .source(config.getCurrentNodeConfig())
          .build();

        network.sendTo(message);

    }
}
