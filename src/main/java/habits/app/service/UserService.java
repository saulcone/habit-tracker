package habits.app.service;

import habits.app.domain.User;
import habits.app.exception.ExistingEmailException;
import habits.app.exception.InvalidTimezoneException;
import habits.app.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

import java.util.Locale;

import static java.time.ZoneId.getAvailableZoneIds;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepo;

    public User registerUser(String email, String timezone) throws ExistingEmailException, InvalidTimezoneException {

        String normalizedEmail = email.toLowerCase(Locale.ROOT);

        if(!getAvailableZoneIds().contains(timezone)) throw new InvalidTimezoneException("This timezone is not correct.");

        if(userRepo.existsByEmail(normalizedEmail)) throw new ExistingEmailException("This email is already registered.");

        User user = new User(normalizedEmail, timezone);

        User savedUser;

        try{
            savedUser = userRepo.saveAndFlush(user);
        }catch(DataIntegrityViolationException e){
            throw new ExistingEmailException(e.getMessage());
        }

        return savedUser;
    }
}
