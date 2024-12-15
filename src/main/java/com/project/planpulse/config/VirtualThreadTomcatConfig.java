package com.project.planpulse.config;

import org.apache.coyote.http11.Http11NioProtocol;
import org.springframework.boot.web.embedded.tomcat.TomcatProtocolHandlerCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.Executors;

@Configuration
public class VirtualThreadTomcatConfig {

    @Bean
    public TomcatProtocolHandlerCustomizer<Http11NioProtocol> protocolHandlerVirtualThreadCustomizer() {
        return protocolHandler -> {
            // set a virtual thread executor for the HTTP protocol handler
            protocolHandler.setExecutor(Executors.newVirtualThreadPerTaskExecutor());
        };
    }

}
