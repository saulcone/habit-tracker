package habits.app;

import habits.app.domain.User;
import habits.app.repository.UserRepository;
import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;
import org.springframework.dao.DataIntegrityViolationException;

import java.util.Optional;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class UserTest {

    @Autowired
    UserRepository userRepo;

    User user1;

    @Autowired
    TestEntityManager entityManager;

    @BeforeEach
    void setUp(){
        user1 = new User("mail@test.com", "Europe/Madrid");
    }

    @Test
    void C1_when_save_user1_and_check_returns_not_error() {
        String timezoneExpected = "Europe/Madrid";
        String emailExpected = "mail@test.com";

        User userSaved = userRepo.save(user1);
        userRepo.flush();

        Long idExpected = userSaved.getId();

        assertNotNull(idExpected);

        entityManager.clear();

        Optional<User> userCheck = userRepo.findByEmail(emailExpected);

        assertTrue(userCheck.isPresent());

        User userCompare = userCheck.get();

        assertAll(
                () -> assertNotSame(userSaved, userCompare),
                () -> assertEquals(idExpected, userCompare.getId()),
                () -> assertEquals(emailExpected, userCompare.getEmail()),
                () -> assertEquals(timezoneExpected, userCompare.getTimezone())
        );

    }

    @Test
    void C2_when_save_two_users_with_same_email_return_error(){
        User user2 = new User("mail@test.com", "Europe/Madrid");

        userRepo.saveAndFlush(user1);

        assertThrows(DataIntegrityViolationException.class, () -> userRepo.saveAndFlush(user2));
    }

}
