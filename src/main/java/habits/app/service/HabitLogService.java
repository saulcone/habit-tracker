package habits.app.service;

import habits.app.domain.Habit;
import habits.app.domain.HabitLog;
import habits.app.domain.User;
import habits.app.exception.*;
import habits.app.repository.HabitLogRepository;
import habits.app.repository.HabitRepository;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.*;
import java.util.Optional;




@Service
public class HabitLogService {

    private final HabitLogRepository habitLogRepo;

    private final HabitRepository habitRepo;

    private final Clock clock;

    public HabitLogService(HabitLogRepository habitLogRepo, HabitRepository habitRepo, Clock clock){
        this.habitLogRepo = habitLogRepo;
        this.habitRepo = habitRepo;
        this.clock = clock;
    }

    @Transactional
    public HabitLogReturnType registerHabitLog(long habitId){

        Optional<Habit> habitFound = habitRepo.findById(habitId);

        if(habitFound.isEmpty()){
            throw new NonExistingHabitException("The habit with id " + habitId + " doesn't exist.");
        }

        Habit habit = habitFound.get();

        if(!habit.getIsActive()){
            throw new InactiveHabitException("This habit is not active now.");
        }

        User user = habit.getUser();

        ZoneId zoneId;

        // we take for granted that the user's timezone is always correct, but even though we catch the exception that could be thrown
        try{
            zoneId = ZoneId.of(user.getTimezone());
        }catch(DateTimeException d){
            throw new IncorrectZoneIdException("The timezone in the user " + user.getId() + " is not correct.", d.getCause());
        }

        LocalDate localDate;
        Instant completedAt;

        completedAt = clock.instant();

        localDate = LocalDate.ofInstant(completedAt, zoneId);

        HabitLog habitLogToSave = new HabitLog(habit, HabitLog.Status.CHECKED, completedAt, localDate);

        Optional<HabitLog> checkSavedHabitLog = habitLogRepo.findByLocalDateEqualsAndHabitEquals(localDate, habit);


        try {
            if(checkSavedHabitLog.isPresent()){
                return new HabitLogReturnType(checkSavedHabitLog.get(), false);
            }

            habitLogToSave = habitLogRepo.saveAndFlush(habitLogToSave);

            return new HabitLogReturnType(habitLogToSave, true);

        } catch (DataIntegrityViolationException d) {
            if (d.getMessage().contains("uk_habit_logs_habit_date")) {
                throw new IncorrectHabitLogDateException("The habit already exists for that day.", new Throwable("The habit already exists for that day."));
            } else{
                throw d;
            }
        }

    }
}
