package org.consensus.raft.network;

import java.io.Serializable;
import javax.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Data;
import org.consensus.raft.bean.AppendEntry;
import org.consensus.raft.bean.AppendEntryResponse;
import org.consensus.raft.bean.RequestVote;
import org.consensus.raft.bean.RequestVoteResponse;
import org.consensus.raft.config.Node;
import org.springframework.validation.annotation.Validated;

@Data
@Builder
@Validated
public class NetworkMessage implements Serializable {

    @NotNull
    private Node source;

    // @NonNull
    // can be null when it's a broadcast message;
    // the broadcast takes care of adding the destination
    private Node destination;

    @NotNull
    private MessageType messageType;

    private AppendEntry appendEntry;

    private AppendEntryResponse appendEntryResponse;

    private RequestVoteResponse requestVoteResponse;

    private RequestVote requestVote;

}
