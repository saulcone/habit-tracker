package habits.app.api.controller;

import habits.app.api.dto.HabitIn;
import habits.app.api.dto.HabitOut;
import habits.app.domain.Habit;
import habits.app.service.HabitService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

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

    @GetMapping
    public List<HabitOut> getActiveUserHabits(@PathVariable long userId){

        List<Habit> habitsObtained = habitService.getUserActiveHabits(userId);

        List<HabitOut> returnHabits = new ArrayList<>();

        for(Habit element : habitsObtained){
            returnHabits.add(new HabitOut(element.getId(), userId, element.getName(), element.getDescription(), element.getCreatedAt()));
        }

        return returnHabits;
    }
}


