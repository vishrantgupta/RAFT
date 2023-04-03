package org.consensus.raft;

public interface ApplicationCallback {

    void apply(Object command);

}

