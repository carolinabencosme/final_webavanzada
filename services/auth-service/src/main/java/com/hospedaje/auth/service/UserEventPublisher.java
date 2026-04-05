package com.hospedaje.auth.service;

import com.hospedaje.auth.config.RabbitMQConfig;
import com.hospedaje.auth.event.UserRegisteredEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserEventPublisher {
    private final RabbitTemplate rabbitTemplate;

    public void publishUserRegistered(UserRegisteredEvent event) {
        try {
            rabbitTemplate.convertAndSend(RabbitMQConfig.EXCHANGE, RabbitMQConfig.USER_REGISTERED_KEY, event);
            log.info("Published user registered event for: {}", event.getEmail());
        } catch (Exception e) {
            log.error("Failed to publish user registered event: {}", e.getMessage());
        }
    }
}
