package com.babu.ms.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.retry.backoff.FixedBackOffPolicy;
import org.springframework.retry.policy.SimpleRetryPolicy;
import org.springframework.retry.support.RetryTemplate;

//    Exchange Type |   Routing Based On	Matching Logic	            Best For                        |
//    --------------|-----------------------------------------------------------------------------------|
//    Direct	    |   Routing key	        Exact match	                Specific, one-to-one delivery   |
//    Topic	        |   Routing key	        Wildcard pattern (*, #)     Filtered pub-sub                |
//    Fanout	    |   None (broadcast)	No matching, all queues	    Broad pub-sub                   |
//    Headers	    |   Message headers     Header key-value match      Metadata-driven routing         |


@Configuration
public class RabbitMQConfig {
    public static final String topicExchangeName = "topic-exchange";

    public static final String sportsCricketQueue = "q.sports.cricket";
    public static final String sportsFootballQueue = "q.sports.football";
    public static final String sportsQueue = "q.sports.all";

    public static final String cricketBindingKey = "sports.cricket.#";
    public static final String footballBindingKey = "sports.football.#";
    public static final String allSportsBindingKey = "sports.#";

    @Bean
    public Queue queueFootball() {
        return QueueBuilder.nonDurable(sportsFootballQueue)
                .withArgument("x-dead-letter-exchange", topicExchangeName)
                .withArgument("x-dead-letter-routing-key", "sports.dlq")
                .build();
    }

    @Bean
    public Queue queueCricket() {
        return QueueBuilder.nonDurable(sportsCricketQueue)
                .withArgument("x-dead-letter-exchange", topicExchangeName)
                .withArgument("x-dead-letter-routing-key", "sports.dlq")
                .build();
    }

    @Bean
    public Queue queueAll() {
        return QueueBuilder.nonDurable()
                .withArgument("x-dead-letter-exchange", topicExchangeName)
                .withArgument("x-dead-letter-routing-key", "sports.dlq")
                .build();
    }

    //  DLQ
    @Bean
    public Queue deadLetterQueue() {
        return new Queue("q.sports.dlq", false);
    }

    @Bean
    public TopicExchange exchange() {
        return new TopicExchange(topicExchangeName);
    }

    @Bean
    public Binding bindingCricket(@Qualifier("queueCricket") Queue queueCricket, TopicExchange exchange) {
        return BindingBuilder.bind(queueCricket).to(exchange).with(cricketBindingKey);
    }

    @Bean
    public Binding bindingFootball(@Qualifier("queueFootball") Queue queueFootball, TopicExchange exchange) {
        return BindingBuilder.bind(queueFootball).to(exchange).with(footballBindingKey);
    }

    @Bean
    public Binding bindingAllSports(@Qualifier("queueAll") Queue queueAll, TopicExchange exchange) {
        return BindingBuilder.bind(queueAll).to(exchange).with(allSportsBindingKey);
    }

    // Add Jackson2JsonMessageConverter bean
    @Bean
    public MessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    @Bean
    public RetryTemplate retryTemplate() {
        RetryTemplate retryTemplate = new RetryTemplate();

        // Retry policy: retry up to 3 times
        SimpleRetryPolicy retryPolicy = new SimpleRetryPolicy();
        retryPolicy.setMaxAttempts(3);
        retryTemplate.setRetryPolicy(retryPolicy);

        // Backoff policy: wait 1 second between retries
        FixedBackOffPolicy backOffPolicy = new FixedBackOffPolicy();
        backOffPolicy.setBackOffPeriod(1000);
        retryTemplate.setBackOffPolicy(backOffPolicy);

        return retryTemplate;
    }

    // Configure RabbitTemplate with the message converter
    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory,
                                         MessageConverter jsonMessageConverter) {
        RabbitTemplate rabbitTemplate = new RabbitTemplate(connectionFactory);
        rabbitTemplate.setMessageConverter(jsonMessageConverter);
        rabbitTemplate.setRetryTemplate(retryTemplate());
        return rabbitTemplate;
    }
}
