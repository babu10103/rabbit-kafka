In RabbitMQ, the behavior of message distribution to consumers depends on the **queue configuration** and the **consumer acknowledgment mode**. Let me explain how it works and how it applies to your setup:

---

### Default Behavior: One Consumer per Message
RabbitMQ operates on a **"one message, one consumer"** model by default when multiple consumers are subscribed to the same queue. Here's how it works:

1. **Message Delivery**: When a message is published to a queue, RabbitMQ delivers it to **exactly one consumer** among all active consumers subscribed to that queue. This is done in a **round-robin** fashion (or based on consumer availability) unless otherwise configured.
2. **Message Removal**: Once the consumer acknowledges the message (either manually or automatically), RabbitMQ removes the message from the queue, and it is no longer available to other consumers.
3. **No Waiting**: RabbitMQ does **not** wait for all consumers to "use" or process the message. The message is assigned to a single consumer, and the queue moves on to the next message.

This ensures efficient load balancing across consumers but means that each message is processed only once, by one consumer.

---

### Your Current Setup
In your `MessageConsumer` class:

```java
@Component
public class MessageConsumer {
    @RabbitListener(queues = RabbitMQConfig.sportsCricketQueue)
    public void receiveCricketMessages(SportsMessage message) {
        System.out.println("Received cricket message: " + message);
    }

    @RabbitListener(queues = RabbitMQConfig.sportsFootballQueue)
    public void receiveFootballMessages(SportsMessage message) {
        System.out.println("Received football message: " + message);
    }

    @RabbitListener(queues = RabbitMQConfig.sportsQueue)
    public void receiveAllMessages(SportsMessage message) {
        System.out.println("Received message: " + message);
    }
}
```

- Each `@RabbitListener` annotation creates a consumer for the specified queue (`q.sports.cricket`, `q.sports.football`, `q.sports.all`).
- If you run multiple instances of your application or increase concurrency (e.g., via `setConcurrentConsumers`), multiple consumers may listen to the same queue.
- When a message arrives in, say, `q.sports.cricket`, RabbitMQ delivers it to **one** of the consumers listening to that queue. Once that consumer processes and acknowledges it, the message is removed from the queue and is **not** sent to other consumers of the same queue.

---

### Acknowledgment Mode
The behavior also depends on how acknowledgment is configured:

1. **Auto-Acknowledgment (Default)**:
    - Spring AMQP’s default behavior with `@RabbitListener` is `auto-ack`, meaning the message is acknowledged (and removed from the queue) as soon as it’s delivered to the consumer, assuming no exception is thrown.
    - If your consumer processes the message successfully, it’s gone from the queue immediately after delivery.

2. **Manual Acknowledgment**:
    - You can configure manual acknowledgment to control when the message is removed from the queue. For example:

      ```java
      @RabbitListener(queues = RabbitMQConfig.sportsCricketQueue)
      public void receiveCricketMessages(Message message, Channel channel, @Header(AmqpHeaders.DELIVERY_TAG) long deliveryTag) throws IOException {
          SportsMessage sportsMessage = (SportsMessage) rabbitTemplate.getMessageConverter().fromMessage(message);
          System.out.println("Received cricket message: " + sportsMessage);
          // Simulate processing
          channel.basicAck(deliveryTag, false); // Manually acknowledge
      }
      ```

        - Here, the message stays in the queue until `channel.basicAck` is called. If the consumer fails or crashes before acknowledging, the message is re-queued and delivered to another consumer.

    - To enable manual acknowledgment, update your container factory:

      ```java
      @Bean
      public SimpleRabbitListenerContainerFactory rabbitListenerContainerFactory(ConnectionFactory connectionFactory) {
          SimpleRabbitListenerContainerFactory factory = new SimpleRabbitListenerContainerFactory();
          factory.setConnectionFactory(connectionFactory);
          factory.setAcknowledgeMode(AcknowledgeMode.MANUAL); // Manual ACK
          return factory;
      }
      ```

---

### Does the Queue Wait for All Consumers?
No, the queue does **not** wait for all consumers to use the message. RabbitMQ is designed for **work distribution**, not broadcasting to all consumers of a single queue. If you want multiple consumers to process the same message, you need a different approach:

#### Alternative: Fanout Exchange for Broadcasting
If you want **all consumers** to receive the same message (e.g., all subscribers to cricket updates get every cricket message), use a **fanout exchange** instead of a topic exchange with a single queue:

1. **Update `RabbitMQConfig`**:
   ```java
   @Bean
   public FanoutExchange fanoutExchange() {
       return new FanoutExchange("fanout-exchange");
   }

   @Bean
   public Queue cricketQueue1() {
       return new Queue("q.cricket.subscriber1", false);
   }

   @Bean
   public Queue cricketQueue2() {
       return new Queue("q.cricket.subscriber2", false);
   }

   @Bean
   public Binding binding1(FanoutExchange fanoutExchange, Queue cricketQueue1) {
       return BindingBuilder.bind(cricketQueue1).to(fanoutExchange);
   }

   @Bean
   public Binding binding2(FanoutExchange fanoutExchange, Queue cricketQueue2) {
       return BindingBuilder.bind(cricketQueue2).to(fanoutExchange);
   }
   ```

2. **Update `MessageProducer`**:
   ```java
   public void sendCricketMessage(SportsMessage message) {
       rabbitMQTemplate.convertAndSend("fanout-exchange", "", message);
       System.out.println("Sent cricket message: " + message);
   }
   ```

3. **Update `MessageConsumer`**:
   ```java
   @RabbitListener(queues = "q.cricket.subscriber1")
   public void receiveCricketMessages1(SportsMessage message) {
       System.out.println("Subscriber 1 received: " + message);
   }

   @RabbitListener(queues = "q.cricket.subscriber2")
   public void receiveCricketMessages2(SportsMessage message) {
       System.out.println("Subscriber 2 received: " + message);
   }
   ```

- **Result**: Every message sent to the `fanout-exchange` is delivered to **all bound queues** (`q.cricket.subscriber1`, `q.cricket.subscriber2`, etc.), so all consumers process the same message.

---

### Summary
- **Current Behavior**: Your queues (`q.sports.cricket`, etc.) release a message to one consumer, and once acknowledged, it’s gone. It doesn’t wait for all consumers.
- **Retries**: If a consumer fails and doesn’t acknowledge (with manual ACK), the message is re-queued and sent to another consumer.
- **Broadcasting**: Use a fanout exchange if you want all consumers to process the same message.

Let me know if you want to adjust your setup for a specific behavior (e.g., retries, broadcasting)!