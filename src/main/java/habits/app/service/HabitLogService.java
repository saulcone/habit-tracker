package habits.app.service;

import habits.app.domain.Habit;
import habits.app.domain.User;
import habits.app.exception.*;
import habits.app.repository.HabitLogRepository;
import habits.app.repository.HabitRepository;
import habits.app.repository.UserRepository;
import org.springframework.stereotype.Service;

import javax.swing.text.html.Option;
import java.time.Instant;
import java.time.LocalDate;
import java.util.Optional;

@Service
public class HabitLogService {

    private final HabitLogRepository habitLogRepo;

    private final HabitRepository habitRepo;

    private final UserRepository userRepo;

    public HabitLogService(HabitLogRepository habitLogRepo, HabitRepository habitRepo, UserRepository userRepo){
        this.habitLogRepo = habitLogRepo;
        this.habitRepo = habitRepo;
        this.userRepo = userRepo;
    }

    public HabitLogService registerHabitLog(long habitId, LocalDate localDate, Instant completedAt){

        Optional<Habit> habitFound = habitRepo.findById(habitId);

        if(habitFound.isEmpty()){
            throw new NonExistingHabitException("The habit with id " + habitId + " doesn't exist.");
        }

        Habit habit = habitFound.get();

        if(!habit.getIsActive()){
            throw new InactiveHabitException("This habit is not active now.");
        }

        User user = habit.getUser();




    }
}
