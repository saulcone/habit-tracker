package habits.app.service;

import habits.app.domain.User;
import habits.app.exception.ExistingEmailException;
import habits.app.exception.InvalidTimezoneException;
import habits.app.repository.UserRepository;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Locale;

import static java.time.ZoneId.getAvailableZoneIds;

@Service
public class UserService {

    private final UserRepository userRepo;

    public UserService(UserRepository userRepo){
        this.userRepo = userRepo;
    }

    @Transactional
    public User registerUser(String email, String timezone){

        String normalizedEmail = email.toLowerCase(Locale.ROOT);

        if(!getAvailableZoneIds().contains(timezone)) throw new InvalidTimezoneException("This timezone is not correct.");

        if(userRepo.existsByEmail(normalizedEmail)) throw new ExistingEmailException("This email is already registered.");

        User user = new User(normalizedEmail, timezone);

        User savedUser;

        try{
            savedUser = userRepo.saveAndFlush(user);
        }catch(DataIntegrityViolationException e){
            if(e.getMostSpecificCause().toString().contains("uk_users_email")){
                throw new ExistingEmailException("duplicate key value violates unique constraint 'uk_users_email'", e.getMostSpecificCause());
            }

            else throw e;
        }

        return savedUser;
    }
}
