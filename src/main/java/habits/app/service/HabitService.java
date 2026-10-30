package habits.app.service;

import habits.app.domain.User;
import habits.app.exception.ExistingEmailException;
import habits.app.exception.InvalidTimezoneException;
import habits.app.repository.UserRepository;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class HabitService {
}
