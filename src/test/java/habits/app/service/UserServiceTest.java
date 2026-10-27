package habits.app.service;

import habits.app.domain.User;
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

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

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
}
