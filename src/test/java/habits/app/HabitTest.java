package habits.app;

import habits.app.domain.Habit;
import habits.app.domain.User;
import habits.app.repository.*;
import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;

import java.time.temporal.ChronoUnit;
import java.util.Optional;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
public class HabitTest {

    @Autowired
    TestEntityManager entityManager;

    @Autowired
    UserRepository userRepo;

    @Autowired
    HabitRepository habitRepo;

    User user1;

    @BeforeEach
    void setUp(){
        user1 = new User("mail@test.com", "Europe/Madrid");

        userRepo.saveAndFlush(user1);
    }

    @Test
    void C1_when_saving_user1_habit_retrieval_returns_habit(){


        Habit habit1 = new Habit(user1, "Habit 1", "Do push ups");

        habit1.toggleActive();

        Habit savedHabit = assertDoesNotThrow(() -> habitRepo.saveAndFlush(habit1));

        entityManager.clear();

        Optional<Habit> foundHabit = assertDoesNotThrow(() -> habitRepo.findById(savedHabit.getId()));

        assertTrue(foundHabit.isPresent());

        Habit retrievedHabit = foundHabit.get();

        assertAll(
                () -> assertNotSame(savedHabit, retrievedHabit),
                () -> assertEquals(savedHabit.getId(), retrievedHabit.getId()),
                () -> assertEquals(savedHabit.getUser().getId(), retrievedHabit.getUser().getId()),
                () -> assertEquals(savedHabit.getName(), retrievedHabit.getName()),
                () -> assertEquals(savedHabit.getDescription(), retrievedHabit.getDescription()),
                () -> assertEquals(savedHabit.getCreatedAt().truncatedTo(ChronoUnit.SECONDS), retrievedHabit.getCreatedAt().truncatedTo(ChronoUnit.SECONDS)),
                () -> assertEquals(savedHabit.getIsActive(), retrievedHabit.getIsActive()),
                () -> assertFalse(retrievedHabit.getIsActive()),
                () -> assertEquals("Do push ups", savedHabit.getDescription())
        );
    }
}
