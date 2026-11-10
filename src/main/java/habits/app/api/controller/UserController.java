package habits.app.api.controller;

import habits.app.api.dto.UserIn;
import habits.app.api.dto.UserOut;
import habits.app.domain.User;

import habits.app.service.UserService;
import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/api/v1/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService){
        this.userService = userService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public UserOut registerUser(@Valid @RequestBody UserIn user){

        User userRegistered = userService.registerUser(user.email(), user.timezone());

        return new UserOut(userRegistered.getId(), userRegistered.getEmail(), userRegistered.getTimezone(), userRegistered.getCreatedAt());
    }
}
