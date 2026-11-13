package habits.app.api.controller;

import habits.app.api.dto.HabitLogOut;
import habits.app.domain.HabitLog;
import habits.app.service.HabitLogReturnType;
import habits.app.service.HabitLogService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/habits/{habitId}/logs")
public class HabitLogController {

    private final HabitLogService habitLogService;

    public HabitLogController(HabitLogService habitLogService){
        this.habitLogService = habitLogService;
    }

    @PostMapping
    public ResponseEntity<HabitLogOut> registerHabitLog(@PathVariable long habitId){

        HabitLogReturnType habitLogReturnTypeRegistered = habitLogService.registerHabitLog(habitId);

        HabitLog habitLogRegistered = habitLogReturnTypeRegistered.habitLog();

        HabitLogOut returnHabitLog = new HabitLogOut(habitLogRegistered.getId(), habitId, habitLogRegistered.getStatus(), habitLogRegistered.getCompletedAt(), habitLogRegistered.getLocalDate());

        if (habitLogReturnTypeRegistered.created()){
            return ResponseEntity.status(HttpStatus.CREATED).body(returnHabitLog);
        }

        else return ResponseEntity.ok(returnHabitLog);
    }
}
