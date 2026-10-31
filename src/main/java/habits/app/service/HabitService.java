package habits.app.service;

import habits.app.domain.Habit;
import habits.app.domain.User;
import habits.app.exception.*;
import habits.app.repository.HabitRepository;
import habits.app.repository.UserRepository;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;



@Service
public class HabitService {

    final int MAX_NAME_LENGTH = 100;

    final int MAX_DESCRIPTION_LENGTH = 300;

    private final HabitRepository habitRepo;

    private final UserRepository userRepo;

    public HabitService(HabitRepository habitRepo, UserRepository userRepo){
        this.habitRepo = habitRepo;
        this.userRepo = userRepo;
    }

    @Transactional
    public Habit registerHabit(long userId, String name, String description){

        if (name == null) {
            throw new EmptyFieldException("Field name is null");
        }
        if (name.isEmpty()) {
            throw new EmptyFieldException("Field name is empty");
        }
        if (name.length() > MAX_NAME_LENGTH) {
            throw new FieldTooLongException("Field name is too long");
        }
        if(name.startsWith(" ") ||  name.endsWith(" ")){
            throw new InvalidNameException("Field name is invalid");
        }
        if(description != null && description.length() > MAX_DESCRIPTION_LENGTH){
            throw new FieldTooLongException("Field description is too long");
        }

        Optional<User> foundUser = userRepo.findById(userId);
        if(foundUser.isEmpty()){
            throw new NonExistingUserException("User with id: " + userId + " not found");
        }

        User user = foundUser.get();

        Habit habit = new Habit(user, name, description);

        Habit savedHabit;

        try{
            savedHabit = habitRepo.saveAndFlush(habit);
        }catch(DataIntegrityViolationException d){
            if(d.getMessage().contains("uk_habits_user_name")){
                throw new ExistingHabitForThatUserException("Habit's name already existing.", d.getCause());
            }

            else throw d;

        }

        return savedHabit;

    }
}