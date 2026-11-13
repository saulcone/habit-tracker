package habits.app.api.controller;

import habits.app.exception.*;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import org.slf4j.Logger;

@RestControllerAdvice("habits.app.api.controller")
public class ApiExceptionHandler extends ResponseEntityExceptionHandler{

    private static final Logger log = LoggerFactory.getLogger(ApiExceptionHandler.class);

    @ExceptionHandler({IncorrectHabitLogDateException.class, InvalidTimezoneException.class, EmptyFieldException.class, NullFieldException.class, FieldTooLongException.class, InvalidNameException.class, InvalidDescriptionException.class})
    public ProblemDetail handle400(RuntimeException exception){
        return ProblemDetail.forStatusAndDetail(HttpStatusCode.valueOf(400), exception.getMessage());
    }

    @ExceptionHandler({NonExistingUserException.class, NonExistingHabitException.class})
    public ProblemDetail handle404(RuntimeException exception){
        return ProblemDetail.forStatusAndDetail(HttpStatusCode.valueOf(404), exception.getMessage());
    }

    @ExceptionHandler({ExistingEmailException.class, ExistingHabitForThatUserException.class, InactiveHabitException.class})
    public ProblemDetail handle409(RuntimeException exception){
        return ProblemDetail.forStatusAndDetail(HttpStatusCode.valueOf(409), exception.getMessage());
    }

    @ExceptionHandler({IncorrectZoneIdException.class, Exception.class})
    public ProblemDetail handle500(Exception exception){
        log.error(exception.getMessage(), exception);
        return ProblemDetail.forStatusAndDetail(HttpStatusCode.valueOf(500), "Not recognised system exception thrown.");
    }
}
