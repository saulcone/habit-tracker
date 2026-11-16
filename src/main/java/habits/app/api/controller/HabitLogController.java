package habits.app.api.controller;

import habits.app.api.dto.HabitLogOut;
import habits.app.domain.HabitLog;
import habits.app.service.HabitLogReturnType;
import habits.app.service.HabitLogService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

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

    @GetMapping
    public List<HabitLogOut> getHabitLogs(@PathVariable long habitId, @RequestParam("begin") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate begin, @RequestParam("end") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate end){

        List<HabitLog> obtainedHabitLogs = habitLogService.getHabitLogsBetweenDates(begin, end, habitId);

        List<HabitLogOut> returnLogs = new ArrayList<>();

        for(HabitLog habitLog : obtainedHabitLogs){
            returnLogs.add(new HabitLogOut(habitLog.getId(), habitId, habitLog.getStatus(), habitLog.getCompletedAt(), habitLog.getLocalDate()));
        }

        return returnLogs;
    }
}
