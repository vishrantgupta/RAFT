package org.consensus.raft.core.state;

import java.util.List;
import org.consensus.raft.bean.AppendEntry;
import org.consensus.raft.config.Node;
import org.consensus.raft.log.LogEntry;


public interface ClusterState {

    boolean acquiredMajorityVoteToBecomeLeader();

    boolean appendEntry(AppendEntry appendEntry);

    void applyCommittedIndex(int untilIndex);

    boolean canAppendLogEntryGivenTerm(int leaderTerm);

    void decrementFollowerNextIndex(Node follower);

    int getCommitIndex();

    void setCommitIndex(int currentTerm);

    int getCurrentTerm();

    List<LogEntry> getFollowerLogEntries(Node followerNode);

    int getFollowerNextIndexByNodeId(Node followerNode);

    int getLastLogIndex();

    int getLastLogTerm();

    int getLogTermByIndex(int index);

    int getMatchIndex();

    DiskPersistedState getPersistedState();

    RaftRole getRole();

    VolatileState getVolatileState();

    Node getVotedFor();

    boolean isCandidate();

    boolean isFollower();

    boolean isLeader();

    // is leader alive before election timeout
    boolean isLeaderAlive();

    void registerVote(Node voteFrom, Boolean voted);

    void setLeaderActive();

    boolean transitionLeaderUnavailable();

    boolean transitionToCandidate();

    boolean transitionToFollower();

    boolean transitionToLeader();

    void updateCurrentTerm(int currentTerm);

    void updateFollowersMatchIndex(Node follower, int matchIndex);

    void updateFollowersNextIndex(Node follower, int matchIndex);

    void updateVotedFor(Node voteFor);
}
