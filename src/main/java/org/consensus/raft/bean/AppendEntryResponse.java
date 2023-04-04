package org.consensus.raft.bean;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.experimental.SuperBuilder;

@SuperBuilder
@Data
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
public class AppendEntryResponse extends RaftMessage {

    private boolean success;

    private int matchIndex;

}
