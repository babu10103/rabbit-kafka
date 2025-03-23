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

    public void sendCricketMessage(SportsMessage message) {
        try {
            rabbitMQTemplate.convertAndSend(RabbitMQConfig.topicExchangeName, "sports.cricket", message);
            System.out.println("Sent cricket message: " + message);
        } catch (Exception e) {
            System.err.println("Failed to send cricket message after retries: " + e.getMessage());
        }
    }

    public void sendFootballMessage(SportsMessage message) {
        try {
            rabbitMQTemplate.convertAndSend(RabbitMQConfig.topicExchangeName, "sports.football", message);
            System.out.println("Sent football message: " + message);
        } catch (Exception e) {
            System.err.println("Failed to send football message after retries: " + e.getMessage());
        }
    }

    public void sendAllMessage(SportsMessage message) {
        try {
            rabbitMQTemplate.convertAndSend(RabbitMQConfig.topicExchangeName, "sports", message);
            System.out.println("Sent message: " + message);
        } catch (Exception e) {
            System.err.println("Failed to send message after retries: " + e.getMessage());
        }
    }
}
