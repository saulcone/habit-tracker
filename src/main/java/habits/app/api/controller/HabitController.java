package habits.app.api.controller;

import habits.app.api.dto.HabitIn;
import habits.app.api.dto.HabitOut;
import habits.app.domain.Habit;
import habits.app.service.HabitService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/users/{userId}/habits")
public class HabitController {

    private final HabitService habitService;

    public HabitController(HabitService habitService){
        this.habitService = habitService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public HabitOut registerHabit(@Valid @RequestBody HabitIn habitIn, @PathVariable long userId){

        Habit habitRegistered = habitService.registerHabit(userId, habitIn.name(), habitIn.description());

        return new HabitOut(habitRegistered.getId(), userId, habitRegistered.getName(), habitRegistered.getDescription(), habitRegistered.getCreatedAt());

    }
}
