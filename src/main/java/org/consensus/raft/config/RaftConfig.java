package org.consensus.raft.config;

import java.util.ArrayList;
import java.util.List;
import javax.validation.constraints.NotNull;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

@Component
// @Configuration
// @EnableConfigurationProperties
@ConfigurationProperties(prefix = "raft")
@Data
@Validated
public class RaftConfig {

    // @Value("${raft.size:3}")
    private int size;

    // @Value("${raft.heartbeatIntervalMillis:50}")
    private int heartbeatIntervalMillis;

    // @Value("${raft.electionTimeoutMillis:500}")
    private int electionTimeoutMillis;

    // @Value("${raft.randomTimeoutMillis:2500}")
    private int randomTimeoutMillis = 2500;

    // @Value("${raft.nodes}")
    @NotNull
    private List<Node> nodes = new ArrayList<>();

    // NOTE: the node id will be passed from CLI using --raft.nodeId
    // @Value("${raft.nodeId}")
    @NotNull
    private String nodeId;

    // @Value("${raft.connectionTimeoutMillis:1000}")
    private int connectionTimeoutMillis;

    // @Value("${raft.consensusTimeoutMillis:100}")
    private int consensusTimeoutMillis;

    public Node getCurrentNodeConfig() {
        for (Node node : nodes) {
            if (node.getId().equals(nodeId)) {
                return node;
            }
        }
        return null;
    }

    public List<Node> otherNodes() {

        List<Node> followers = new ArrayList<>();

        for (Node node : nodes) {
            if (!node.getId().equals(this.nodeId)) {
                followers.add(node);
            }
        }

        return followers;
    }

}
