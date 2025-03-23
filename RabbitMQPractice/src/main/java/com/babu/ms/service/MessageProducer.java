package com.babu.ms.service;

import com.babu.ms.config.RabbitMQConfig;
import com.babu.ms.models.SportsMessage;
import lombok.AllArgsConstructor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class MessageProducer {
    private RabbitTemplate rabbitMQTemplate;

    @FunctionalInterface
    interface MessageSender {
        void send(SportsMessage message, RabbitTemplate template, String exchange, String routingKey);
    }

    private void sendMessage(SportsMessage message, String routingKey, MessageSender sender) {
        try {
            sender.send(message, rabbitMQTemplate, RabbitMQConfig.topicExchangeName,  routingKey);
            System.out.println("Sent message: " + message);
        } catch (Exception e) {
            System.err.println("Failed to send message after retries: " + e.getMessage());
        }
    }

    private final MessageSender defaultSender = (message, template, exchange, routingKey) -> template.convertAndSend(exchange, routingKey, message);

    public void sendCricketMessage(SportsMessage message) {
        sendMessage(message, "sports.cricket", defaultSender);
    }

    public void sendFootballMessage(SportsMessage message) {
        sendMessage(message, "sports.football", defaultSender);
    }

    public void sendAllMessage(SportsMessage message) {
        sendMessage(message, "sports", defaultSender);
    }
}
