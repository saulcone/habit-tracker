package habits.app.exception;

public class ExistingEmailException extends RuntimeException     {
    public ExistingEmailException(String message) {
        super(message);
    }

    public ExistingEmailException(String message, Throwable cause){
        super(message);
        this.initCause(cause);
    }
}
