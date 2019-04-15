package de.classmapper.example.wrapper;

import lombok.Getter;

/**
 * Created by jansen on 23.03.18.
 */
public class TransportableException extends RuntimeException {

    @Getter
    private final int statusCode;

    public TransportableException(int statusCode) {
        this(statusCode, null, null);
    }

    public TransportableException(int statusCode, String message) {
        this(statusCode, message, null);
    }

    public TransportableException(int statusCode, Throwable cause) {
        this(statusCode, null, cause);
    }

    public TransportableException(int statusCode, String message, Throwable cause) {
        super(message, cause);
        this.statusCode = statusCode;
    }
}
