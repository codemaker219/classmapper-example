package de.classmapper.example.wrapper;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.util.Optional;

@ToString
@NoArgsConstructor
public class MessageContainer<T> {
    @Getter
    private T value;
    private ExceptionMsg exception;

    public MessageContainer(T value) {
        this.value = value;
    }

    public MessageContainer(ExceptionMsg exception) {
        this.exception = exception;
    }

    public T extractResult() {
        return Optional.ofNullable(value)
                .orElseThrow(() -> exception == null ? new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "No value present") : new ResponseStatusException(HttpStatus.resolve(exception.getStatusCode()), exception.getMessage()));
    }


}
