package org.consensus.raft.core.eventhandler;

import java.util.Map.Entry;
import lombok.Getter;
import org.consensus.raft.bean.RaftMessage;
import org.consensus.raft.config.Node;
import org.consensus.raft.config.RaftConfig;
import org.consensus.raft.core.state.ClusterState;
import org.consensus.raft.network.Network;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationListener;

@Getter
public abstract class BaseListener<Event extends ApplicationEvent> implements ApplicationListener<Event> {

    private final ClusterState clusterState;

    private final Network network;
    private final RaftConfig config;

    public BaseListener(ClusterState clusterState, Network network, RaftConfig config) {

        this.clusterState = clusterState;
        this.network = network;
        this.config = config;
    }

    protected abstract void handleEvent(Event event);

    @Override
    public void onApplicationEvent(Event event) {

        Entry<? extends RaftMessage, Node> entry = (Entry<? extends RaftMessage, Node>) event.getSource();
        RaftMessage message = entry.getKey();

        // handle term
        termHandler(message);

        handleEvent(event);
    }

    private void termHandler(RaftMessage message) {
        if (message.getCurrentTerm() > this.getClusterState().getCurrentTerm()) {
            this.getClusterState().updateCurrentTerm(message.getCurrentTerm());
            this.getClusterState().updateVotedFor(null);
            this.getClusterState().transitionToFollower();
        }
    }

}
