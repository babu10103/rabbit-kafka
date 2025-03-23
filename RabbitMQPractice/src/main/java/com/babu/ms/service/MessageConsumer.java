package com.babu.ms.service;

import com.babu.ms.config.RabbitMQConfig;
import com.babu.ms.models.SportsMessage;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.retry.interceptor.RetryOperationsInterceptor;
import org.springframework.retry.support.RetryTemplate;
import org.springframework.stereotype.Component;

@Component
public class MessageConsumer {
    @RabbitListener(queues = RabbitMQConfig.sportsCricketQueue)
    public void receiveCricketMessages(SportsMessage message) {
        System.out.println("Received Cricket message: " + message);
    }

    @RabbitListener(queues = RabbitMQConfig.sportsFootballQueue)
    public void receiveFootballMessages(SportsMessage message) {
        System.out.println("Received Football message: " + message);
    }

    @RabbitListener(queues = RabbitMQConfig.sportsQueue)
    public void receiveAllMessages(SportsMessage message) {
        System.out.println("Received message: " + message);
    }
}


@Configuration
class RabbitListenerConfig {

    /*
    -> If a consumer method throws an exception (e.g., due to invalid data
       or a downstream service failure), the message will be retried up
       to 3 times with a 1-second delay.
    -> After all retries fail, the message can be sent to a dead-letter queue (DLQ) for further analysis.
     */
    @Bean
    public SimpleRabbitListenerContainerFactory rabbitListenerContainerFactory(ConnectionFactory connectionFactory, RetryTemplate retryTemplate) {
        SimpleRabbitListenerContainerFactory factory = new SimpleRabbitListenerContainerFactory();
        factory.setConnectionFactory(connectionFactory);
        factory.setAdviceChain(new RetryOperationsInterceptor[] { retryInterceptor(retryTemplate) });
        return factory;
    }

    @Bean
    public RetryOperationsInterceptor retryInterceptor(RetryTemplate retryTemplate) {
        RetryOperationsInterceptor interceptor = new RetryOperationsInterceptor();
        interceptor.setRetryOperations(retryTemplate);
        return interceptor;
    }
}
