package org.consensus.raft.network;

import jakarta.websocket.ClientEndpoint;
import jakarta.websocket.OnClose;
import jakarta.websocket.OnError;
import jakarta.websocket.OnMessage;
import jakarta.websocket.OnOpen;
import jakarta.websocket.Session;
import jakarta.websocket.server.ServerEndpoint;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.consensus.raft.event.AppendEntryEvent;
import org.consensus.raft.event.AppendEntryResponseEvent;
import org.consensus.raft.event.RequestVoteEvent;
import org.consensus.raft.event.RequestVoteResponseEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

@Component
@ServerEndpoint(value = "/websocket", decoders = MessageDecoder.class, encoders = MessageEncoder.class, configurator = WebSocketServerConfigurator.class)
@ClientEndpoint(decoders = MessageDecoder.class, encoders = MessageEncoder.class)
@Slf4j
public class MessageHandler {

    private final ApplicationEventPublisher eventPublisher;

    @Autowired
    public MessageHandler(ApplicationEventPublisher eventPublisher) {
        this.eventPublisher = eventPublisher;
    }

    @OnClose
    public void onClose(Session session) {
        log.warn("Disconnected from server: " + session.getId());
    }

    @OnOpen
    public void onOpen(Session session) {
        log.debug("Connected to server: " + session.getId());
    }

    @OnError
    public void onError(Session session, Throwable throwable) {
        // handle error

        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        throwable.printStackTrace(pw);
        String stackTraceAsString = sw.toString();

        log.warn("Error occurred: " + stackTraceAsString);
    }

    // incoming messages
    @OnMessage
    public void receive(NetworkMessage message) {

        if (message == null) {
            log.warn("received a null message");
            return;
        }

        // log.info("handling message of type " + message.getMessageType() + " from node " + message.getSource());

        switch (message.getMessageType()) {
            // should be received by follower
            case APPEND_ENTRY -> eventPublisher.publishEvent(new AppendEntryEvent(Map.entry(message.getAppendEntry(), message.getSource())));

            // should be received by leader
            case APPEND_ENTRY_RESPONSE -> eventPublisher.publishEvent(new AppendEntryResponseEvent(Map.entry(message.getAppendEntryResponse(), message.getSource())));

            // should be raised by a candidate
            case REQUEST_VOTE -> eventPublisher.publishEvent(new RequestVoteEvent(Map.entry(message.getRequestVote(), message.getSource())));

            // should be received by candidate
            case REQUEST_VOTE_RESPONSE -> eventPublisher.publishEvent(new RequestVoteResponseEvent(Map.entry(message.getRequestVoteResponse(), message.getSource())));
        }
    }

}
