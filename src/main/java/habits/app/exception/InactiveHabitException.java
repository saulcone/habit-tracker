package habits.app.exception;

public class InactiveHabitException extends RuntimeException {
    public InactiveHabitException(String message) {
        super(message);
    }

    public InactiveHabitException(String message, Throwable cause) {
        super(message);
        this.initCause(cause);
    }
}
