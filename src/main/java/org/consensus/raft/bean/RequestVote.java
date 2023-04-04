package org.consensus.raft.bean;

import javax.validation.constraints.NotNull;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.experimental.SuperBuilder;
import org.consensus.raft.config.Node;
import org.springframework.validation.annotation.Validated;

@SuperBuilder
@Data
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
@Validated
public class RequestVote extends RaftMessage {

    // candidate requesting vote
    @NotNull
    private Node candidateId;

    // index of candidate’s last log entry (§5.4)
    @NotNull
    private int lastLogIndex;

    // term of candidate’s last log entry (§5.4)
    @NotNull
    private int lastLogTerm;

}
