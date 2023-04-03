package org.consensus.raft.network;

import jakarta.websocket.server.ServerEndpointConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

@Component
public class WebSocketServerConfigurator extends ServerEndpointConfig.Configurator {

    private static ApplicationContext applicationContext;

    @Autowired
    public void setApplicationContext(ApplicationContext applicationContext) {
        WebSocketServerConfigurator.applicationContext = applicationContext;
    }

    @Override
    public <T> T getEndpointInstance(Class<T> endpointClass) throws InstantiationException {
        return applicationContext.getBean(endpointClass);
    }
}
