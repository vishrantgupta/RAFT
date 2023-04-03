package org.consensus.raft.core.eventhandler;

import org.consensus.raft.bean.RaftMessage;
import org.consensus.raft.core.state.ClusterState;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

public interface BaseListener<Event extends ApplicationEvent> extends ApplicationListener<Event> {

    // TODO change this to follow template pattern
    default void termHandler(RaftMessage message) {
        if (message.getCurrentTerm() > this.getClusterState().getCurrentTerm()) {
            this.getClusterState().updateCurrentTerm(message.getCurrentTerm());
            this.getClusterState().updateVotedFor(null);
            this.getClusterState().transitionToFollower();
        }
    }

    ClusterState getClusterState();

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    default void handleAfterCommit(Event event) throws Exception {
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMPLETION)
    default void handleAfterCompletion(Event event) throws Exception {
    }

    @TransactionalEventListener(phase = TransactionPhase.BEFORE_COMMIT)
    default void handleBeforeCommit(Event event) throws Exception {
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_ROLLBACK)
    default void handleRollback(Event event) throws Exception {
    }

}
