package habits.app.exception;

public class IncorrectHabitLogDateException extends RuntimeException {

    public IncorrectHabitLogDateException(String message) {
        super(message);
    }

    public IncorrectHabitLogDateException(String message, Throwable cause) {
        super(message);
        this.initCause(cause);
    }
}
