package de.classmapper.example.exceptions;

import de.classmapper.example.wrapper.TransportableException;
import org.springframework.http.HttpStatus;

public class NotFoundException extends TransportableException {

    public static final int STATUS_CODE = HttpStatus.NOT_FOUND.value();

    public NotFoundException() {
        super(STATUS_CODE);
    }

    public NotFoundException(String message) {
        super(STATUS_CODE, message);
    }

    public NotFoundException(Throwable cause) {
        super(STATUS_CODE, cause);
    }

    public NotFoundException(String message, Throwable cause) {
        super(STATUS_CODE, message, cause);
    }
}
