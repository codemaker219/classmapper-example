package de.classmapper.example;

import de.classmapper.example.messages.ExampleRequestMsg;
import de.classmapper.example.messages.ExampleResponseMsg;
import de.classmapper.example.sender.GenericSender;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.web.server.ResponseStatusException;

import java.util.UUID;

import static junit.framework.TestCase.assertEquals;
import static junit.framework.TestCase.assertNull;

@RunWith(SpringRunner.class)
@SpringBootTest
public class ExampleApplicationTests {

    @Autowired
    private GenericSender sender;

    @Test
    public void ok() {
        String hello = UUID.randomUUID().toString();
        ExampleResponseMsg response = sender.sendAndReceive(new ExampleRequestMsg(hello, Receiver.EXAMPLE_EXCHANGE, Receiver.SUCCESSFUL));
        assertEquals(hello, response.getWorld());
    }

    @Test(expected = ResponseStatusException.class)
    public void transportableException() {
        sender.sendAndReceive(new ExampleRequestMsg(UUID.randomUUID().toString(), Receiver.EXAMPLE_EXCHANGE, Receiver.TRANSPORTABLE_EXCEPTION));
    }

    @Test
    public void exception() {
        Object response = sender.sendAndReceive(new ExampleRequestMsg(UUID.randomUUID().toString(), Receiver.EXAMPLE_EXCHANGE, Receiver.EXCEPTION));
        assertNull(response);
    }

}
