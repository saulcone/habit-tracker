package habits.app;

import habits.app.domain.Habit;
import habits.app.domain.HabitLog;
import habits.app.domain.User;
import habits.app.repository.*;
import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
public class HabitLogTest {

    @Autowired
    HabitLogRepository habitLogRepo;

    @Autowired
    UserRepository userRepo;

    @Autowired
    HabitRepository habitRepo;

    @Autowired
    TestEntityManager entityManager;

    User user1;

    Habit habit1;

    Habit habit2;

    @BeforeEach
    void setUp(){
        user1 = new User("test@mail.com", "Europe/Madrid");

        habit1 = new Habit(user1, "Push ups", "Do 20 push ups");

        habit2 = new Habit(user1, "Eat fruit", "Eat 1 full fruit");

        userRepo.saveAndFlush(user1);

        habitRepo.saveAndFlush(habit1);

        habitRepo.saveAndFlush(habit2);

        entityManager.clear();
    }

    @Test
    void C1_when_saving_log_check_date_and_status_returns_no_error() {

        HabitLog habitLog1 = new HabitLog(habit1, HabitLog.Status.UNCHECKED);
        HabitLog habitLog2 = new HabitLog(habit2, HabitLog.Status.UNCHECKED);

        habitLogRepo.saveAndFlush(habitLog1);
        habitLogRepo.saveAndFlush(habitLog2);

        entityManager.clear();

        Optional<HabitLog> foundOptionalLogs1;
        Optional<HabitLog> foundOptionalLogs2;


        foundOptionalLogs1 = assertDoesNotThrow(() -> habitLogRepo.findById(habitLog1.getId()));
        foundOptionalLogs2 = assertDoesNotThrow(() -> habitLogRepo.findById(habitLog2.getId()));;

        assertAll(
                () -> assertTrue(foundOptionalLogs1.isPresent()),
                () -> assertTrue(foundOptionalLogs2.isPresent())
        );

        HabitLog foundLog1 = foundOptionalLogs1.get();
        HabitLog foundLog2 = foundOptionalLogs2.get();

        List<HabitLog> allFoundLogs = new ArrayList<HabitLog>();
        allFoundLogs.add(foundLog1);
        allFoundLogs.add(foundLog2);

        assertAll(
                () -> assertEquals(habitLog1.getCompletedAt().truncatedTo(ChronoUnit.SECONDS), allFoundLogs.get(0).getCompletedAt().truncatedTo(ChronoUnit.SECONDS)),
                () -> assertEquals(habitLog1.getLocalDate(), allFoundLogs.get(0).getLocalDate()),
                () -> assertEquals(habitLog1.getStatus(), allFoundLogs.get(0).getStatus()),
                () -> assertEquals(habitLog1.getHabit().getName(), allFoundLogs.get(0).getHabit().getName()),
                () -> assertEquals(habitLog1.getHabit().getId(), allFoundLogs.get(0).getHabit().getId()),
                () -> assertEquals(habitLog1.getHabit().getDescription(), allFoundLogs.get(0).getHabit().getDescription()),

                () -> assertEquals(habitLog2.getCompletedAt().truncatedTo(ChronoUnit.SECONDS), allFoundLogs.get(1).getCompletedAt().truncatedTo(ChronoUnit.SECONDS)),
                () -> assertEquals(habitLog2.getLocalDate(), allFoundLogs.get(1).getLocalDate()),
                () -> assertEquals(habitLog2.getStatus(), allFoundLogs.get(1).getStatus()),
                () -> assertEquals(habitLog2.getHabit().getName(), allFoundLogs.get(1).getHabit().getName()),
                () -> assertEquals(habitLog2.getHabit().getId(), allFoundLogs.get(1).getHabit().getId()),
                () -> assertEquals(habitLog2.getHabit().getDescription(), allFoundLogs.get(1).getHabit().getDescription())


        );
    }
}
