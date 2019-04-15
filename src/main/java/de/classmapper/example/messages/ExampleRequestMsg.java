package de.classmapper.example.messages;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ExampleRequestMsg {
    private String hello;

    private String exchange;
    private String routingKey;
}
