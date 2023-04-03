package org.consensus.raft.network;

public interface Network {

    void broadcast(NetworkMessage message);

    void sendTo(NetworkMessage message);

}
