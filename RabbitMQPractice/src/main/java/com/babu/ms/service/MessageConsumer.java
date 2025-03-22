package com.babu.ms.service;

import com.babu.ms.config.RabbitMQConfig;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
public class MessageConsumer {
    @RabbitListener(queues = RabbitMQConfig.sportsCricketQueue)
    public void receiveCricketMessages(String message) {
        System.out.println("Received message: " + message);
    }

    @RabbitListener(queues = RabbitMQConfig.sportsFootballQueue)
    public void receiveFootballMessages(String message) {
        System.out.println("Received message: " + message);
    }

    @RabbitListener(queues = RabbitMQConfig.sportsQueue)
    public void receiveAllMessages(String message) {
        System.out.println("Received message: " + message);
    }
}
