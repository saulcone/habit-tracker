package habits.app.exception;

public class NonExistingHabitException extends RuntimeException {
    public NonExistingHabitException(String message){
        super(message);
    }

}
