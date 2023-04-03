package org.consensus.raft.bean;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.experimental.SuperBuilder;

@SuperBuilder
@Data
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
public class RequestVoteResponse extends RaftMessage {

    // true means candidate received vote
    private boolean voteGranted;

}
