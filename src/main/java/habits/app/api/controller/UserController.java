package habits.app.controller;

import habits.app.api.dto.UserIn;
import habits.app.api.dto.UserOut;
import habits.app.domain.User;
import habits.app.exception.EmptyFieldException;
import habits.app.exception.ExistingEmailException;
import habits.app.exception.InvalidTimezoneException;
import habits.app.service.UserService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/v1/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService){
        this.userService = userService;
    }

    @PostMapping()
    @ResponseStatus(HttpStatus.CREATED)
    public UserOut registerUser(@Valid @RequestBody UserIn user){

        User userRegistered = userService.registerUser(user.email(), user.timezone());

        return new UserOut(userRegistered.getId(), userRegistered.getEmail(), userRegistered.getTimezone(), userRegistered.getCreatedAt());
    }
}
