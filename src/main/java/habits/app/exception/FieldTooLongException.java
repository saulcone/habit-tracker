package habits.app.exception;

public class FieldTooLongException extends RuntimeException {

    public FieldTooLongException(String message) {
        super(message);
    }
}
