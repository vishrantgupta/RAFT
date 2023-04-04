package org.consensus.raft.core.state;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.consensus.raft.config.Node;
import org.consensus.raft.config.RaftConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Data
@Component
@Slf4j
class VolatileState implements RaftState {

    private final RaftConfig config;

    private AtomicInteger commitIndex = new AtomicInteger(0);

    private AtomicInteger lastApplied = new AtomicInteger(0);

    // followers next index
    private volatile Map<String, Integer> followersNextIndex;

    // append entries that are known to be replicated
    private volatile Map<String, Integer> followersMatchIndex;

    @Autowired
    public VolatileState(RaftConfig config) {

        this.config = config;
        reset();
    }

    public int getCommitIndex() {
        return this.commitIndex.get();
    }

    public void setCommitIndex(int nextCommitIndex) {
        this.commitIndex.set(nextCommitIndex);
    }

    public Integer getFollowerNextIndexByNodeId(String nodeId) {
        return this.followersNextIndex.get(nodeId);
    }

    public int getLastApplied() {
        return this.lastApplied.get();
    }

    public void incrementLastApplied() {
        this.lastApplied.set(this.getLastApplied() + 1);
    }

    @Override
    public void reset() {
        this.commitIndex.set(0);
        this.lastApplied.set(0);

        this.followersNextIndex = new ConcurrentHashMap<>();
        this.followersMatchIndex = new ConcurrentHashMap<>();

        log.debug("reset the follower next index");

        for (Node follower : config.otherNodes()) {
            log.debug("updating next index for node " + follower);
            // System.out.println("init " + follower.getNodeId());
            this.followersNextIndex.put(follower.getId(), 1);
            this.followersMatchIndex.put(follower.getId(), 0);
        }
    }

    public void updateFollowersMatchIndex(String nodeId, Integer matchIdx) {
        // https://github.com/dabeaz-course/raft_2023_03/discussions/34#discussion-5032301
        followersMatchIndex.put(nodeId, Math.max(this.getFollowersMatchIndex().get(nodeId), matchIdx));
    }

    public void updateFollowersNextIndex(String nodeId, Integer nextIdx) {
        followersNextIndex.put(nodeId, nextIdx);
    }

}
