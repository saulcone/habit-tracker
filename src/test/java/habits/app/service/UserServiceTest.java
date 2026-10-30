package habits.app.service;

import habits.app.domain.User;
import habits.app.exception.ExistingEmailException;
import habits.app.exception.InvalidTimezoneException;
import habits.app.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.junit.jupiter.api.Assertions.*;


import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class UserServiceTest {

    @Mock
    UserRepository userRepo;

    @InjectMocks
    UserService service;

    @Captor
    ArgumentCaptor<User> userCaptor;

    User user;

    String email;

    String timezone;

    @BeforeEach
    void setUp(){
        email = "mail@test.com";
        timezone = "Europe/Madrid";
        user = new User(email, timezone);
    }

    @Test
    void C1_when_register_user_return_user_correctly_saved(){

        when(userRepo.existsByEmail(user.getEmail())).thenReturn(false);
        when(userRepo.saveAndFlush(any(User.class))).thenReturn(user);

        User savedUser = service.registerUser(user.getEmail(), user.getTimezone());

        verify(userRepo).saveAndFlush(userCaptor.capture());

        User value = userCaptor.getValue();

        assertAll(
                () -> assertSame(user, savedUser),
                () -> assertEquals(email,  value.getEmail()),
                () -> assertEquals(timezone, value.getTimezone())
        );

    }

    @Test
    void C2_when_register_existing_mail_return_existing_email_exception(){

        when(userRepo.existsByEmail(user.getEmail())).thenReturn(true);

        assertThrows(ExistingEmailException.class, () -> service.registerUser(user.getEmail(), user.getTimezone()));

        verify(userRepo, never()).saveAndFlush(any(User.class));

    }

    @Test
    void C3_when_register_user_with_not_correct_timezone_return_invalid_timezone_exception(){

        user.setTimezone("not correct timezone");

        assertThrows(InvalidTimezoneException.class, () -> service.registerUser(user.getEmail(), user.getTimezone()));

        verify(userRepo, never()).saveAndFlush(any(User.class));

        verifyNoInteractions(userRepo);
    }

    @Test
    void C4_when_email_with_caps_check_saved_user_email_is_normalized(){

        String cappedEmail = "MaiL@TeSt.CoM";

        when(userRepo.existsByEmail(email)).thenReturn(false);
        when(userRepo.saveAndFlush(any(User.class))).thenReturn(user);

        service.registerUser(cappedEmail, timezone);

        verify(userRepo).saveAndFlush(userCaptor.capture());

        User capturedUser = userCaptor.getValue();

        assertEquals(email, capturedUser.getEmail());

    }

    @Test
    void C5_when_saving_checked_email_and_race_condition_on_db_return_data_integrity_violation_exception(){

        when(userRepo.existsByEmail(email)).thenReturn(false);

        DataIntegrityViolationException exceptionThrown = new DataIntegrityViolationException("uk_users_email");

        when(userRepo.saveAndFlush(any(User.class))).thenThrow(exceptionThrown);


        ExistingEmailException emailException = assertThrows(ExistingEmailException.class, () -> service.registerUser(email, timezone));

        String expectedMessage = "uk_users_email";

        assertTrue(emailException.getMessage().contains(expectedMessage));

    }

    @Test
    void C6_when_saving_email_and_catches_non_related_email_data_integrity_violation_and_with_cause_then_rethrow_exception(){
        when(userRepo.existsByEmail(email)).thenReturn(false);

        DataIntegrityViolationException exceptionThrown = new DataIntegrityViolationException("non email exception", new Throwable("non email cause"));

        when(userRepo.saveAndFlush(any(User.class))).thenThrow(exceptionThrown);


        DataIntegrityViolationException receivedException = assertThrows(DataIntegrityViolationException.class, () -> service.registerUser(email, timezone));


    }

    @Test
    void C7_when_saving_email_and_catches_non_related_email_data_integrity_violation_and_without_cause_then_rethrow_exception(){
        when(userRepo.existsByEmail(email)).thenReturn(false);

        DataIntegrityViolationException exceptionThrown = new DataIntegrityViolationException("exception without cause");

        when(userRepo.saveAndFlush(any(User.class))).thenThrow(exceptionThrown);

        DataIntegrityViolationException receivedException = assertThrows(DataIntegrityViolationException.class, () -> service.registerUser(email, timezone));

        assertSame(exceptionThrown, receivedException);

    }


}
