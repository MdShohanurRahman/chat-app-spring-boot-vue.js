package com.example.chatapp.component;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.AbstractSubProtocolEvent;
import org.springframework.web.socket.messaging.SessionConnectEvent;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;

import java.util.Objects;

@Component
@RequiredArgsConstructor
@Slf4j
public class WebSocketEventListener {

    @EventListener
    public void handleWebSocketConnectListener(SessionConnectEvent event) {
        String username = getUserNameFromSessionEvent(event);
        log.info("Connected user: {}", username);
    }

    @EventListener
    public void handleWebSocketDisconnectListener(SessionDisconnectEvent event) {
        String username = getUserNameFromSessionEvent(event);
        log.info("Disconnected user: {}", username);
    }

    private String getUserNameFromSessionEvent(AbstractSubProtocolEvent event) {
        try {
            StompHeaderAccessor headerAccessor = StompHeaderAccessor.wrap(event.getMessage());
            return (String) Objects.requireNonNull(headerAccessor.getSessionAttributes()).get("username");
        } catch (Exception ex) {
            return null;
        }
    }
}

