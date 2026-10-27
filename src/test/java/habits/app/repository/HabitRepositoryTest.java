package habits.app.repository;

import habits.app.domain.Habit;
import habits.app.domain.User;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;

import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
public class HabitRepositoryTest {

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

    @Test
    void C2_when_active_habits_and_inactive_and_retrieve_active_habits_only_active_received(){

        User user2 = new User("mail1@test.com", "Europe/Madrid");

        userRepo.saveAndFlush(user2);


        Habit habitUser2 = new Habit(user2, "Habit From User 2", "Eat oranges");

        Habit habit1 = new Habit(user1, "Habit 1", "Do push ups");

        Habit habit2 = new Habit(user1, "Habit 2", "Study Chinese");
        habit2.toggleActive();

        Habit habit3 = new Habit(user1, "Habit 3", "Learn yoga");

        Habit habit4 = new Habit(user1, "Habit 4", "Read novel");
        habit4.toggleActive();

        habitRepo.save(habitUser2);
        habitRepo.save(habit1);
        habitRepo.save(habit2);
        habitRepo.save(habit3);
        habitRepo.save(habit4);

        assertDoesNotThrow(() -> habitRepo.flush());

        entityManager.clear();

        List<Habit> habits = habitRepo.findByIsActiveTrueAndUserEqualsOrderByName(user1);
        assertEquals(2, habits.size());

        assertAll(
                () -> assertEquals(habit1.getName(), habits.getFirst().getName()),
                () -> assertEquals(habit1.getDescription(), habits.getFirst().getDescription()),
                () -> assertEquals(habit1.getId(), habits.getFirst().getId()),
                () -> assertTrue(habits.getFirst().getIsActive()),

                () -> assertEquals(habit3.getName(), habits.get(1).getName()),
                () -> assertEquals(habit3.getDescription(), habits.get(1).getDescription()),
                () -> assertEquals(habit3.getId(), habits.get(1).getId()),
                () -> assertTrue(habits.get(1).getIsActive())
        );
    }
}
