package habits.app.service;

import habits.app.domain.User;
import habits.app.exception.ExistingEmailException;
import habits.app.exception.InvalidTimezoneException;
import habits.app.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;+
import habits.app.exception.NonExistingUserException;
import habits.app.exception.ExistingHabitForThatUserException;

import java.util.Locale;

import static java.time.ZoneId.getAvailableZoneIds;

@Service
public class HabitService {

    private final HabitRepository habitRepo;

    private final UserRepository userRepo;

    public HabitService(HabitRepository habitRepo, UserRepository userRepo){
        this.habitRepo = habitRepo;
    }

    @Transactional
    public Habit registerHabit(long userId, String name, String description){

        Optional<User> foundUser = userRepo.findById(userId);

        if(!foundUser.isPresent()){
            throw new NonExistingUserException("user not found");
        }

        User user = foundUser.get();

        Habit habit = new Habit(user, name, description);

        try{
            Habit savedHabit = habitRepo.saveAndFlush(habit);
        }catch(DataIntegrityViolationException d){
            if(d.getMessage().contains("uk_habits_user_name")){
                throw new ExistingHabitForThatUserException("Habit's name already existing.", d.getCause());
            }

        }

        return savedHabit;

    }



}