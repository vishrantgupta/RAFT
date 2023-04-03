package org.consensus.raft.bean;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.experimental.SuperBuilder;
import org.consensus.raft.log.LogEntry;

@SuperBuilder
@Data
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
public class AppendEntry extends RaftMessage {

    // so follower can redirect clients
    private String leaderId;

    // index of log entry immediately preceding
    // new ones
    private int prevLogIndex;

    // term of prevLogIndex entry (check page 70 of slides "Log Matching")
    private int prevLogTerm;

    // leader’s commitIndex
    // commit this append entry at this index
    private int leaderCommitIndex;

    private LogEntry[] entries;
    
    // adding require id to add idempotent nature to append entry
    // private UUID requestId;

}
