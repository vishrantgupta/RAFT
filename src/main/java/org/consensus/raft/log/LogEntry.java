package org.consensus.raft.log;

import java.io.Serializable;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class LogEntry implements Serializable {

    private int term;
    private Object command;

}
