package org.consensus.raft.bean;

import javax.validation.constraints.NotNull;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.experimental.SuperBuilder;
import org.springframework.validation.annotation.Validated;

@SuperBuilder
@Data
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
@Validated
public class RequestVoteResponse extends RaftMessage {

    // true mean candidate received vote
    @NotNull
    private boolean voteGranted;

}
