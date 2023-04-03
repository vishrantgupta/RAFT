package org.consensus.raft.bean;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.experimental.SuperBuilder;
import org.consensus.raft.config.Node;

@SuperBuilder
@Data
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
public class RequestVote extends RaftMessage {

    // candidate requesting vote
    private Node candidateId;

    // index of candidate’s last log entry (§5.4)
    private int lastLogIndex;

    // term of candidate’s last log entry (§5.4)
    private int lastLogTerm;

}
