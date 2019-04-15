package de.classmapper.example.wrapper;

import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Created by jansen on 23.03.18.
 */
@Data
@NoArgsConstructor
public class ExceptionMsg {
    private int statusCode;
    private String message;
    private String exceptionClass;

    private ExceptionMsg cause;

    public ExceptionMsg(TransportableException exception) {
        this.statusCode = exception.getStatusCode();
        this.message = exception.getMessage();
        this.exceptionClass = exception.getClass().getSimpleName();
        if (exception.getCause() != null) {
            this.cause = new ExceptionMsg(exception.getCause());
        } else {
            this.cause = null;
        }
    }

    private ExceptionMsg(Throwable cause) {
        this.message = cause.getMessage();
        this.statusCode = -1;
        this.exceptionClass = cause.getClass().getSimpleName();
        if (cause.getCause() != null) {
            this.cause = new ExceptionMsg(cause.getCause());
        } else {
            this.cause = null;
        }
    }


}
