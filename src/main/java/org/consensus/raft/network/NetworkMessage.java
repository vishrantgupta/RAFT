package org.consensus.raft.network;

import java.io.Serializable;
import lombok.Builder;
import lombok.Data;
import lombok.NonNull;
import org.consensus.raft.bean.AppendEntry;
import org.consensus.raft.bean.AppendEntryResponse;
import org.consensus.raft.bean.RequestVote;
import org.consensus.raft.bean.RequestVoteResponse;
import org.consensus.raft.config.Node;

@Data
@Builder
public class NetworkMessage implements Serializable {

    @NonNull
    private Node source;

    // @NonNull
    // can be null when it's a broadcast message;
    // the broadcast takes care of adding the destination
    private Node destination;

    @NonNull
    private MessageType messageType;

    private AppendEntry appendEntry;

    private AppendEntryResponse appendEntryResponse;

    private RequestVoteResponse requestVoteResponse;

    private RequestVote requestVote;

}
