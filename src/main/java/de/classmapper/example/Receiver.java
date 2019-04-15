package de.classmapper.example;

import de.classmapper.example.exceptions.NotFoundException;
import de.classmapper.example.messages.ExampleRequestMsg;
import de.classmapper.example.messages.ExampleResponseMsg;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class Receiver {


    public static final String EXAMPLE_EXCHANGE = "example.exchange";
    public static final String SUCCESSFUL = "OK";
    public static final String TRANSPORTABLE_EXCEPTION = "TRANSPORTABLE";
    public static final String EXCEPTION = "EXCEPTION";

    @RabbitListener(bindings = @QueueBinding(
            value = @Queue(durable = "false", autoDelete = "true"),
            exchange = @Exchange(value = EXAMPLE_EXCHANGE), key = SUCCESSFUL)
    )
    public ExampleResponseMsg processSuccessful(ExampleRequestMsg request) {
        log.info("Received request {}", request);
        return new ExampleResponseMsg(request.getHello());
    }


    @RabbitListener(bindings = @QueueBinding(
            value = @Queue(durable = "false", autoDelete = "true"),
            exchange = @Exchange(value = EXAMPLE_EXCHANGE), key = TRANSPORTABLE_EXCEPTION)
    )
    public ExampleResponseMsg processThrowTransportable(ExampleRequestMsg request) {
        log.info("Received request {}", request);
        throw new NotFoundException("because of reasons");
    }


    @RabbitListener(bindings = @QueueBinding(
            value = @Queue(durable = "false", autoDelete = "true"),
            exchange = @Exchange(value = EXAMPLE_EXCHANGE), key = EXCEPTION)
    )
    public ExampleResponseMsg processThrow(ExampleRequestMsg request) {
        log.info("Received request {}", request);
        throw new NullPointerException();
    }
}
