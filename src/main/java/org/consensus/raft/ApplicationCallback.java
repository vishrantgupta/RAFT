package org.consensus.raft;

public interface ApplicationCallback {

    String apply(Object command);

}

