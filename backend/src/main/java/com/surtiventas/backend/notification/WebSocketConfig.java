package com.surtiventas.backend.notification;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

/**
 * STOMP messaging setup for real-time notifications.
 *
 * <p>By default (single instance) an in-memory simple broker is used. When
 * running several backend instances behind a load balancer (HA), the in-memory
 * broker can't reach subscribers connected to a different instance, so the app
 * switches to an external STOMP broker relay (RabbitMQ): every instance relays
 * to the shared broker, so a notification published on any instance reaches the
 * subscriber wherever it is connected. Toggled with {@code app.broker.relay.*}.
 */
@Configuration
@EnableWebSocketMessageBroker
@RequiredArgsConstructor
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    private final StompAuthChannelInterceptor stompAuthChannelInterceptor;

    @Value("${app.broker.relay.enabled:false}")
    private boolean relayEnabled;
    @Value("${app.broker.relay.host:rabbitmq}")
    private String relayHost;
    @Value("${app.broker.relay.port:61613}")
    private int relayPort;
    @Value("${app.broker.relay.login:guest}")
    private String relayLogin;
    @Value("${app.broker.relay.passcode:guest}")
    private String relayPasscode;

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/ws").setAllowedOriginPatterns("*");
    }

    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        if (relayEnabled) {
            registry.enableStompBrokerRelay("/topic")
                    .setRelayHost(relayHost)
                    .setRelayPort(relayPort)
                    .setClientLogin(relayLogin)
                    .setClientPasscode(relayPasscode)
                    .setSystemLogin(relayLogin)
                    .setSystemPasscode(relayPasscode);
        } else {
            registry.enableSimpleBroker("/topic");
        }
        registry.setApplicationDestinationPrefixes("/app");
    }

    @Override
    public void configureClientInboundChannel(ChannelRegistration registration) {
        registration.interceptors(stompAuthChannelInterceptor);
    }
}
