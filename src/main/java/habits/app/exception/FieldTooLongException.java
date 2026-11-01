package habits.app.exception;

public class FieldTooLongException extends RuntimeException {

    public FieldTooLongException(String message) {
        super(message);
    }
    public FieldTooLongException(String message, Throwable cause) {
        super(message);
        this.initCause(cause);
    }
}
