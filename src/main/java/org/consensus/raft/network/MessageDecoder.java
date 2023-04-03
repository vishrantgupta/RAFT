package org.consensus.raft.network;

import jakarta.websocket.Decoder;
import jakarta.websocket.EndpointConfig;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.nio.ByteBuffer;

public class MessageDecoder implements Decoder.Binary<NetworkMessage> {

    @Override
    public NetworkMessage decode(ByteBuffer bytes) {
        try (ByteArrayInputStream bis = new ByteArrayInputStream(bytes.array()); ObjectInputStream ois = new ObjectInputStream(bis)) {
            return (NetworkMessage) ois.readObject();
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public void destroy() {
    }

    @Override
    public void init(EndpointConfig config) {
    }

    @Override
    public boolean willDecode(ByteBuffer bytes) {
        try {
            new ObjectInputStream(new ByteArrayInputStream(bytes.array())).close();
            return true;
        } catch (IOException e) {
            return false;
        }
    }
}
