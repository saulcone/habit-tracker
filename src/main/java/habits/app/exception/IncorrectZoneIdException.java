package habits.app.exception;

public class IncorrectZoneIdException extends RuntimeException {
    public IncorrectZoneIdException(String message, Throwable cause) {
        super(message);
        this.initCause(cause);
    }
}
