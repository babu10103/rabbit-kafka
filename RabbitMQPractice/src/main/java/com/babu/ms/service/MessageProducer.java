package com.babu.ms.service;

import com.babu.ms.config.RabbitMQConfig;
import lombok.AllArgsConstructor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class MessageProducer {
    private RabbitTemplate rabbitMQTemplate;

    public void sendCricketMessage(String message) {
        rabbitMQTemplate.convertAndSend(RabbitMQConfig.topicExchangeName, RabbitMQConfig.sportsCricketQueue, message);
        System.out.println("Sent cricket message: " + message);
    }

    public void sendFootballMessage(String message) {
        rabbitMQTemplate.convertAndSend(RabbitMQConfig.topicExchangeName, RabbitMQConfig.sportsFootballQueue, message);
        System.out.println("Sent football message: " + message);
    }

    public void sendAllMessage(String message) {
        rabbitMQTemplate.convertAndSend(RabbitMQConfig.topicExchangeName, RabbitMQConfig.sportsQueue, message);
        System.out.println("Sent message: " + message);
    }
}
