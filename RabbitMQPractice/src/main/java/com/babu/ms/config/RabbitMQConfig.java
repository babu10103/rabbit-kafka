package com.babu.ms.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;


//    Exchange Type |   Routing Based On	Matching Logic	            Best For                        |
//    --------------|-----------------------------------------------------------------------------------|
//    Direct	    |   Routing key	        Exact match	                Specific, one-to-one delivery   |
//    Topic	        |   Routing key	        Wildcard pattern (*, #)     Filtered pub-sub                |
//    Fanout	    |   None (broadcast)	No matching, all queues	    Broad pub-sub                   |
//    Headers	    |   Message headers     Header key-value match      Metadata-driven routing         |


@Configuration
public class RabbitMQConfig
{
    public static final String topicExchangeName = "topic-exchange";

    public static final String sportsCricketQueue = "q.sports.cricket";
    public static final String sportsFootballQueue = "q.sports.football";
    public static final String sportsQueue = "q.sports.all";

//    binding keys
    public static final String cricketBindingKey = "sports.cricket.#";
    public static final String footballBindingKey = "sports.football.#";
    public static final String allSportsBindingKey = "sports.#";

    @Bean
    public Queue queueFootball() {
        return new Queue(sportsFootballQueue, false);
    }

    @Bean
    public Queue queueCricket() {
        return new Queue(sportsCricketQueue, false);
    }


    @Bean
    public Queue queueAll() {
        return new Queue(sportsQueue, false);
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
}
