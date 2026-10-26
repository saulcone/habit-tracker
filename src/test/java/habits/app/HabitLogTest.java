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
import org.springframework.dao.DataIntegrityViolationException;

import java.time.Instant;
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

    @Test
    void C2_when_find_by_between_two_dates_return_only_logs_on_habit1(){

        LocalDate date1 = LocalDate.of(2026, 10, 19);
        LocalDate date2 = LocalDate.of(2026, 10, 22);
        LocalDate date3 = LocalDate.of(2026, 10, 26);
        LocalDate date4 = LocalDate.of(2026, 10, 27);
        LocalDate date5 = LocalDate.of(2026, 10, 24);


        HabitLog habitLog1 = new HabitLog(habit1, HabitLog.Status.UNCHECKED);
        habitLog1.setLocalDate(date2);

        HabitLog habitLog2 = new HabitLog(habit1, HabitLog.Status.UNCHECKED);
        habitLog2.setLocalDate(date5);

        HabitLog habitLog3 = new HabitLog(habit1, HabitLog.Status.UNCHECKED);
        habitLog3.setLocalDate(date4);

        HabitLog habitLog4 = new HabitLog(habit2, HabitLog.Status.UNCHECKED);
        habitLog4.setLocalDate(date2);

        HabitLog habitLog5 = new HabitLog(habit1, HabitLog.Status.UNCHECKED);
        habitLog5.setLocalDate(date1);

        HabitLog habitLog6 = new HabitLog(habit1, HabitLog.Status.UNCHECKED);
        habitLog6.setLocalDate(date3);

        habitLogRepo.saveAndFlush(habitLog1);
        habitLogRepo.saveAndFlush(habitLog2);
        habitLogRepo.saveAndFlush(habitLog3);
        habitLogRepo.saveAndFlush(habitLog4);
        habitLogRepo.saveAndFlush(habitLog5);
        habitLogRepo.saveAndFlush(habitLog6);

        entityManager.clear();

        //between day 19 and 26
        List<HabitLog> logsObtained = habitLogRepo.findByLocalDateBetweenAndHabitEqualsOrderByLocalDate(date1, date3, habit1);

        assertEquals(4, logsObtained.size());

        HabitLog obtainedLog1 = logsObtained.get(0);
        HabitLog obtainedLog2 = logsObtained.get(1);
        HabitLog obtainedLog3 = logsObtained.get(2);
        HabitLog obtainedLog4 = logsObtained.get(3);




        assertAll(
                () -> assertEquals(habitLog5.getId(), obtainedLog1.getId()),

                () -> assertEquals(habitLog1.getId(), obtainedLog2.getId()),

                () -> assertEquals(habitLog2.getId(), obtainedLog3.getId()),

                () -> assertEquals(habitLog6.getId(), obtainedLog4.getId())
        );
    }

    @Test
    void C3_when_saving_logs_return_true_when_date_is_x_and_habit_is_1_and_false_if_one_clause_is_different(){

        LocalDate date1 = LocalDate.of(2026, 10, 19);
        LocalDate date2 = LocalDate.of(2026, 10, 22);

        HabitLog habitLog1 = new HabitLog(habit1, HabitLog.Status.UNCHECKED);
        habitLog1.setLocalDate(date1);


        habitLogRepo.saveAndFlush(habitLog1);

        entityManager.clear();

        assertAll(
                () -> assertTrue(habitLogRepo.existsByHabitAndLocalDateEquals(habit1, date1)),
                () -> assertFalse(habitLogRepo.existsByHabitAndLocalDateEquals(habit1, date2)),
                () -> assertFalse(habitLogRepo.existsByHabitAndLocalDateEquals(habit2, date1))
        );
    }

    // Due to PostgreSQL implementation, to use auto rollback and try this test, it is needed to put the save and flush that throws exception on the last assertion in order to not break the transaction
    // The test creates a transaction for each function (test) so if the transaction is broken before other not failing transactions, they won't be correctly tested
    @Test
    void C4_when_saving_log_from_same_date_different_habit_and_same_habit_and_different_date_return_not_exception_but_when_saving_from_existing_log_and_same_date_return_data_integrity_violation_exception(){

        LocalDate date1 = LocalDate.of(2026, 10, 19);
        LocalDate date2 = LocalDate.of(2026, 10, 20);

        HabitLog habitLogToSave = new HabitLog(habit1, HabitLog.Status.UNCHECKED);
        habitLogToSave.setLocalDate(date1);

        HabitLog habitLogToException = new HabitLog(habit1, HabitLog.Status.UNCHECKED);
        habitLogToException.setLocalDate(date1);

        HabitLog habitLog1 = new HabitLog(habit1, HabitLog.Status.UNCHECKED);
        habitLog1.setLocalDate(date2);

        HabitLog habitLog2 = new HabitLog(habit2, HabitLog.Status.UNCHECKED);
        habitLog2.setLocalDate(date2);



        habitLogRepo.saveAndFlush(habitLogToSave);

        entityManager.clear();

        assertAll(
                () -> assertDoesNotThrow(() -> habitLogRepo.saveAndFlush(habitLog1)),
                () -> assertDoesNotThrow(() -> habitLogRepo.saveAndFlush(habitLog2)),
                () -> assertThrows(DataIntegrityViolationException.class, () -> habitLogRepo.saveAndFlush(habitLogToException))

        );



    }
}
