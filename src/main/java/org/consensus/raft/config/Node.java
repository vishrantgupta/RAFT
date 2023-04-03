package org.consensus.raft.config;

import java.io.Serializable;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@Data
@AllArgsConstructor
@ToString
@Builder
@EqualsAndHashCode
public class Node implements Serializable {

    private String ipAddress;

    private Integer port;

    private String id;
}
