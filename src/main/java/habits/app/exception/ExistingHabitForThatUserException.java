package habits.app.exception;

public class ExistingHabitForThatUserException extends RuntimeException {
    public ExistingHabitForThatUserException(String message, Throwable cause) {
        super(message);
        this.initCause(cause);
    }
}
