package habits.app.exception;

public class NonExistingHabitException extends RuntimeException {
    public NonExistingHabitException(String message){
        super(message);
    }

    public NonExistingHabitException(String message, Throwable cause) {
        super(message);
        this.initCause(cause);
    }
}
