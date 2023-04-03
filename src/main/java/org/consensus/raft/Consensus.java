package org.consensus.raft;

public interface Consensus {

    boolean getConsensus(Object command);

}
