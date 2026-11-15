package habits.app.exception;

public class NonExistingUserException extends RuntimeException     {
    public NonExistingUserException(String message) {
        super(message);
    }

}
