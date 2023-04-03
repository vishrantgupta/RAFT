package org.consensus.raft.core.state;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Map.Entry;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.consensus.raft.ApplicationCallback;
import org.consensus.raft.bean.AppendEntry;
import org.consensus.raft.config.Node;
import org.consensus.raft.config.RaftConfig;
import org.consensus.raft.exception.RaftException;
import org.consensus.raft.log.LogEntry;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Component
@Scope("singleton")
@Setter
@Slf4j
public class ClusterStateImpl implements ClusterState {

    private final RaftConfig config;

    private final ApplicationCallback callback;

    private volatile RaftRole role;

    private volatile Map<Node, Integer> votesReceived;

    // is leader alive before election timeout
    private volatile boolean isLeaderAlive;

    @Getter
    private volatile DiskPersistedState persistedState;

    @Getter
    private volatile VolatileState volatileState;

    @Autowired
    public ClusterStateImpl(RaftConfig config, DiskPersistedState persistedState, VolatileState volatileState, ApplicationCallback callback) {
        this.config = config;

        this.persistedState = persistedState;
        this.volatileState = volatileState;

        this.callback = callback;

        this.transitionToFollower();
    }

    @Override
    public synchronized boolean acquiredMajorityVoteToBecomeLeader() {
        Collection<Integer> votes = this.votesReceived.values();

        int totalVotes = 0;
        for (int vote : votes) {
            totalVotes += vote;
        }

        // if this is a candidate then only promote it; if it has already become a leader then ignore it
        return this.role == RaftRole.CANDIDATE && totalVotes > this.config.getNodes().size() / 2;
    }

    @Override
    public boolean appendEntry(AppendEntry appendEntry) {
        return this.getPersistedState()
          .getLog()
          .appendLogEntry(appendEntry.getPrevLogIndex(), appendEntry.getPrevLogTerm(), appendEntry.getEntries());
    }

    @Override
    public synchronized void applyCommittedIndex(int untilIndex) {
        if (this.getVolatileState().getLastApplied() < untilIndex) {

            log.debug("entries got committed applying from " + this.getVolatileState().getLastApplied() + " " + untilIndex);

            ListIterator<LogEntry> itr = this.getPersistedState().getLog().listIterator(this.getVolatileState().getLastApplied() + 1);

            while (itr.hasNext()) {
                Object command = itr.next().getCommand();
                log.debug("apply command on follower " + command);

                callback.apply(command);
                this.getVolatileState().incrementLastApplied();
            }
        }
    }

    @Override
    public synchronized boolean canAppendLogEntryGivenTerm(int leaderTerm) {
        if (this.getPersistedState().getCurrentTerm() > leaderTerm) {
            return false;
        }

        if (this.getPersistedState().getCurrentTerm() < leaderTerm) {
            this.transitionToFollower();
            this.getPersistedState().setCurrentTerm(leaderTerm);
            this.getPersistedState().setVotedFor(null);
        }

        return true;
    }

    @Override
    public void decrementFollowerNextIndex(Node follower) {
        this.getVolatileState().decrementFollowerNextIndex(follower.getId());
    }

    @Override
    public int getCommitIndex() {
        return getVolatileState().getCommitIndex();
    }

    @Override
    public void setCommitIndex(int currentTerm) {
        // updating the commit index
        List<Integer> collection = new ArrayList<>(this.getVolatileState().getFollowersMatchIndex().values());
        Collections.sort(collection);

        // if committed on majority of the followers then update the commit index
        int nextCommitIndex = collection.get(this.config.getNodes().size() / 2);
        if (this.getVolatileState().getCommitIndex() < nextCommitIndex
          && this.getPersistedState().getCurrentTerm() == currentTerm) { // the server is allowed to commit only if the leader is in it's current term (slide page 132 Raft paper figure 8)
            this.getVolatileState().setCommitIndex(nextCommitIndex);
        }
    }

    @Override
    public int getCurrentTerm() {
        return this.getPersistedState().getCurrentTerm();
    }

    @Override
    public List<LogEntry> getFollowerLogEntries(Node followerNode) {

        // *expected* next log index of follower
        Integer followerNextLogIdx = this.getFollowerNextIndexByNodeId(followerNode);

        List<LogEntry> logEntries;
        if (followerNextLogIdx < this.getPersistedState().getLog().size()) {
            logEntries = this.getPersistedState().getLog().subList(followerNextLogIdx, this.getPersistedState().getLog().size());
        } else {
            // sending empty list for heartbeat
            logEntries = new LinkedList<>();
        }

        return logEntries;
    }

    @Override
    public int getFollowerNextIndexByNodeId(Node followerNode) {
        return getVolatileState().getFollowerNextIndexByNodeId(followerNode.getId());
    }

    @Override
    public int getLastLogIndex() {
        return this.getPersistedState().getLog().size() - 1;
    }

    @Override
    public int getLastLogTerm() {
        return this.getPersistedState().getLog().getLast().getTerm();
    }

    @Override
    public int getLogTermByIndex(int index) {
        return getPersistedState().getLog().get(index).getTerm();
    }

    @Override
    public int getMatchIndex() {
        return this.getPersistedState().getLog().size() - 1;
    }

    @Override
    public synchronized RaftRole getRole() {
        return this.role;
    }

    @Override
    public Node getVotedFor() {
        return this.getPersistedState().getVotedFor();
    }

    @Override
    public synchronized boolean isCandidate() {
        return this.role == RaftRole.CANDIDATE;
    }

    @Override
    public synchronized boolean isFollower() {
        return this.role == RaftRole.FOLLOWER;
    }

    @Override
    public synchronized boolean isLeader() {
        return this.role == RaftRole.LEADER;
    }

    @Override
    public synchronized boolean isLeaderAlive() {
        return this.isLeaderAlive;
    }

    public synchronized void registerVote(Node voteFrom, Boolean voted) {
        this.votesReceived.put(voteFrom, voted ? 1 : 0);
    }

    public synchronized void setLeaderActive() {
        this.isLeaderAlive = true;
    }

    @Override
    public synchronized boolean transitionLeaderUnavailable() {
        this.isLeaderAlive = false;
        return true;
    }

    public synchronized boolean transitionToCandidate() {

        this.role = RaftRole.CANDIDATE;

        this.persistedState.setCurrentTerm(this.persistedState.getCurrentTerm() + 1);
        this.persistedState.setVotedFor(this.config.getCurrentNodeConfig()); // vote for self

        this.votesReceived = new HashMap<>();
        this.votesReceived.put(this.config.getCurrentNodeConfig(), 1);

        // Reinitialized after election (Raft Paper Figure 2)
        this.volatileState = new VolatileState(config);

        this.setLeaderActive();

        log.info("Transitioned to CANDIDATE in term " + this.persistedState.getCurrentTerm());

        return true;
    }

    @Override
    public synchronized boolean transitionToFollower() {

        this.role = RaftRole.FOLLOWER;
        log.info("Became " + RaftRole.FOLLOWER.name() + " in term " + this.persistedState.getCurrentTerm());

        return false;
    }

    @Override
    public synchronized boolean transitionToLeader() {

        if (this.role != RaftRole.CANDIDATE) {
            throw new RaftException("cannot be promoted to a leader as this is not in " + RaftRole.CANDIDATE.name() + " state");
        }

        if (this.persistedState.getVotedFor() != config.getCurrentNodeConfig()) {
            log.warn("voted for someone else");
            return false;
        }

        this.role = RaftRole.LEADER;
        this.setLeaderActive();

        // initializing the follower's next index to the size of leader log; it will be adjusted based on the followers response
        // Map<String, Integer> followersNextIndex = this.volatileState.getFollowersNextIndex();
        for (Entry<String, Integer> entry : this.volatileState.getFollowersNextIndex().entrySet()) {
            this.volatileState.getFollowersNextIndex().put(entry.getKey(), this.persistedState.getLog().size());
        }

        // initially match index is 0, it will be adjusted based on the AppendEntryResponse
        // Map<String, Integer> followersMatchIndex = this.volatileState.getFollowersMatchIndex();
        for (Entry<String, Integer> entry : this.volatileState.getFollowersMatchIndex().entrySet()) {
            this.volatileState.getFollowersMatchIndex().put(entry.getKey(), 0);
        }

        log.info("Got promoted to " + RaftRole.LEADER.name() + " with term " + this.persistedState.getCurrentTerm());

        return true;
    }

    @Override
    public void updateCurrentTerm(int currentTerm) {
        getPersistedState().setCurrentTerm(currentTerm);
    }

    @Override
    public void updateFollowersMatchIndex(Node follower, int matchIndex) {
        this.getVolatileState().updateFollowersMatchIndex(follower.getId(), matchIndex);
    }

    @Override
    public void updateFollowersNextIndex(Node follower, int matchIndex) {
        this.getVolatileState().updateFollowersNextIndex(follower.getId(), matchIndex + 1);
    }

    @Override
    public void updateVotedFor(Node voteFor) {
        getPersistedState().setVotedFor(voteFor);
    }

}
