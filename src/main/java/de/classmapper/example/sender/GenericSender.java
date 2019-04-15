package de.classmapper.example.sender;

import de.classmapper.example.messages.ExampleRequestMsg;
import de.classmapper.example.wrapper.MessageContainer;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

@Service
public class GenericSender {
    private RabbitTemplate rabbitTemplate;

    public GenericSender(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    public void send(ExampleRequestMsg msg) {
        rabbitTemplate.convertAndSend(msg.getExchange(), msg.getRoutingKey(), msg);
    }

    @SuppressWarnings("unchecked")
    public <T> T sendAndReceive(ExampleRequestMsg msg) {
        MessageContainer<T> response = (MessageContainer<T>) rabbitTemplate.convertSendAndReceive(msg.getExchange(), msg.getRoutingKey(), msg);
        if (response == null) {
          return null;
        }
        return (T) response.extractResult();
    }
}
