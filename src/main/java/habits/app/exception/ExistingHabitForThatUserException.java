package habits.app.exception;

public class NonExistingUserException extends RuntimeException     {
    public NonExistingUserException(String message) {
        super(message);
    }

    public NonExistingUserException(String message, Throwable cause){
        super(message);
        this.initCause(cause);
    }
}
