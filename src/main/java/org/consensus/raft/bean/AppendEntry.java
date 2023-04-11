package org.consensus.raft.bean;

import java.util.List;
import javax.validation.constraints.NotNull;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.experimental.SuperBuilder;
import org.consensus.raft.log.LogEntry;
import org.springframework.validation.annotation.Validated;

@SuperBuilder
@Data
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
@Validated
public class AppendEntry extends RaftMessage {

    // so follower can redirect clients
    @NotNull
    private String leaderId;

    // index of log entry immediately preceding
    @NotNull
    private int prevLogIndex;

    // term of prevLogIndex entry (check page 70 of slides "Log Matching")
    @NotNull
    private int prevLogTerm;

    // leader’s commitIndex
    // commit this append entry at this index
    @NotNull
    private int leaderCommitIndex;

    @NotNull
    private List<LogEntry> entries;

    // adding require id to add idempotent nature to append entry
    // private UUID requestId;

}
