package org.consensus.raft.network;

import org.consensus.raft.config.RaftConfig;
import org.springframework.boot.web.server.WebServerFactoryCustomizer;
import org.springframework.boot.web.servlet.server.ConfigurableServletWebServerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.server.standard.ServerEndpointExporter;

@Configuration
public class WebSocketConfig implements WebServerFactoryCustomizer<ConfigurableServletWebServerFactory> {

    private final RaftConfig config;

    public WebSocketConfig(RaftConfig config) {
        this.config = config;
    }

    @Bean
    public ServerEndpointExporter serverEndpointExporter() {
        return new ServerEndpointExporter();
    }

    @Override
    public void customize(ConfigurableServletWebServerFactory factory) {
        factory.setPort(config.getCurrentNodeConfig().getPort());
    }

}
