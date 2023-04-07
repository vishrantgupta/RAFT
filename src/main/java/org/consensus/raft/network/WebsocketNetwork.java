package org.consensus.raft.network;

import jakarta.websocket.ContainerProvider;
import jakarta.websocket.Session;
import jakarta.websocket.WebSocketContainer;
import java.net.URI;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.consensus.raft.config.Node;
import org.consensus.raft.config.RaftConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.socket.client.WebSocketClient;
import org.springframework.web.socket.client.standard.StandardWebSocketClient;

@Service
@EnableScheduling
@Slf4j
public class WebsocketNetwork implements Network {

    private final Map<Node, Session> sessions;

    private final Map<URI, Node> followerRaftNode;

    private final MessageHandler messageHandler;

    @Bean
    public WebSocketClient webSocketClient() {
        return new StandardWebSocketClient();
    }

    @SneakyThrows
    @Autowired
    public WebsocketNetwork(RaftConfig config, MessageHandler messageHandler) {

        this.messageHandler = messageHandler;

        this.sessions = new HashMap<>();
        // this.eventPublisher = eventPublisher;

        this.followerRaftNode = new HashMap<>();

        List<Node> nodes = config.otherNodes();
        for (Node node : nodes) {
            URI uri = new URI(String.format("ws://%s:%d/websocket", node.getIpAddress(), node.getPort()));
            followerRaftNode.put(uri, node);
        }

        // establishConnection();

        // ping();
    }

    @Override
    public void broadcast(final NetworkMessage message) {

        // log.debug("sending message " + message);

        if (message == null) {
            return;
        }

        this.sessions.forEach((node, session) -> {
            if (session.isOpen()) {
                message.setDestination(node);
                try {
                    session.getAsyncRemote().sendObject(message);
                } catch (Exception e) {
                    log.warn("Error while sending message to node " + node, e);
                }
            } else {
                log.warn("Session is close for node " + node);
            }
        });
    }

    // @EventListener(value = ApplicationReadyEvent.class)
    @Scheduled(fixedDelay = 5000)
    public void establishConnection() {

        WebSocketContainer container = ContainerProvider.getWebSocketContainer();
        // List<RaftNode> nodes = config.getNodes();

        for (Entry<URI, Node> nodeEntry : followerRaftNode.entrySet()) {

            URI address = nodeEntry.getKey();
            Node raftNode = nodeEntry.getValue();

            // for (String address : nodeAddress) {
            Session session = this.sessions.getOrDefault(raftNode, null);

            if (session == null || !session.isOpen()) {
                try {
                    this.sessions.put(raftNode, container.connectToServer(this.messageHandler, address));
                } catch (Exception e) {
                    log.error("Failed to establish connection on node " + raftNode + " " + e.getMessage());
                }
            }
        }
    }

    @Override
    public void sendTo(NetworkMessage message) {

        if (sessions.get(message.getDestination()) != null && sessions.get(message.getDestination()).isOpen()) {
            sessions.get(message.getDestination()).getAsyncRemote().sendObject(message);
        } else {
            log.debug("Could not send the message to " + message.getDestination());
        }

    }
}
