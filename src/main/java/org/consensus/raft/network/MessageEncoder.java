package org.consensus.raft.network;

import jakarta.websocket.Encoder;
import jakarta.websocket.EndpointConfig;
import java.io.ByteArrayOutputStream;
import java.io.ObjectOutputStream;
import java.nio.ByteBuffer;
import lombok.SneakyThrows;

public class MessageEncoder implements Encoder.Binary<NetworkMessage> {

    @Override
    public void destroy() {
    }

    @SneakyThrows
    @Override
    public ByteBuffer encode(NetworkMessage object) {

        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        ObjectOutputStream objectOutputStream = new ObjectOutputStream(byteArrayOutputStream);

        objectOutputStream.writeObject(object);
        objectOutputStream.flush();

        return ByteBuffer.wrap(byteArrayOutputStream.toByteArray());
    }

    @Override
    public void init(EndpointConfig config) {
    }

}
