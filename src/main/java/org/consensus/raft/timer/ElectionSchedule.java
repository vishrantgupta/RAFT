package org.consensus.raft.timer;

import java.util.concurrent.TimeUnit;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.consensus.raft.bean.RequestVote;
import org.consensus.raft.config.RaftConfig;
import org.consensus.raft.core.state.ClusterState;
import org.consensus.raft.core.state.RaftRole;
import org.consensus.raft.network.MessageType;
import org.consensus.raft.network.Network;
import org.consensus.raft.network.NetworkMessage;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

@EnableScheduling
@Configuration
@Slf4j
public class ElectionSchedule {

    private final ClusterState clusterState;
    private final RaftConfig config;
    private final Network network;

    public ElectionSchedule(ClusterState clusterState, RaftConfig config, Network network) {
        this.config = config;
        this.network = network;
        this.clusterState = clusterState;
    }

    @SneakyThrows
    @Scheduled(fixedDelay = 5, initialDelay = 3, timeUnit = TimeUnit.SECONDS)
    public void election() {

        if (this.clusterState.getCurrentRole() != RaftRole.LEADER) {
            if (!this.clusterState.isLeaderAlive()) {

                log.debug("Calling for election...");

                // assuming the leader is not alive
                if (this.clusterState.transitionToCandidate()) {

                    int currentTerm = this.clusterState.getCurrentTerm();

                    int previousIndex = this.clusterState.getLastLogIndex();
                    int lastTerm = this.clusterState.getLastLogTerm();

                    RequestVote requestVote = RequestVote.builder()
                      .currentTerm(currentTerm)
                      .candidateId(this.config.getCurrentNodeConfig())
                      .lastLogIndex(previousIndex)
                      .lastLogTerm(lastTerm).build();

                    network.broadcast(NetworkMessage.builder()
                      .requestVote(requestVote)
                      .messageType(MessageType.REQUEST_VOTE)
                      .source(config.getCurrentNodeConfig())
                      .build());

                }

            }

            this.clusterState.transitionLeaderUnavailable();
        }

        Thread.sleep(config.getRandomTimeoutMillis());
    }
}
