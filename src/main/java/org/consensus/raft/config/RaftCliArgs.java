package org.consensus.raft.config;

import lombok.Data;
import org.kohsuke.args4j.Option;
import org.springframework.stereotype.Component;

/**
 * CLI Args.
 */
@Data
@Component
public class RaftCliArgs {

    @Option(name = "--nodeId", usage = "Raft node id", required = true)
    private String nodeId;

}
