package habits.app.service;

import habits.app.domain.Habit;
import habits.app.domain.HabitLog;
import habits.app.domain.User;
import habits.app.exception.InactiveHabitException;
import habits.app.exception.IncorrectHabitLogDateException;
import habits.app.exception.InvalidTimezoneException;
import habits.app.exception.NonExistingHabitException;
import habits.app.repository.HabitLogRepository;
import habits.app.repository.HabitRepository;
import habits.app.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class HabitLogServiceTest {

    HabitLogService habitLogService;

    @Mock
    HabitLogRepository habitLogRepo;

    @Mock
    HabitRepository habitRepo;

    @Mock
    UserRepository userRepo;

    Clock clock;

    User user1;

    Habit habit1;

    Habit habit2;

    HabitLog habitLog1;

    HabitLog habitLog2;

    private static final Instant NOW = Instant.parse("2026-11-05T23:30:00Z");

    LocalDate localDateTest = LocalDate.of(2026, 11, 6);

    @Captor
    ArgumentCaptor<HabitLog> habitLogArgumentCaptor;

    final long id = 1L;

    @BeforeEach
    void setUp(){
        user1 = new User("mail@test.com", "Europe/Madrid");

        habit1 = new Habit(user1, "Habit 1", "Do push ups");

        habit2 = new Habit(user1, "Habit 2", "Study programming");

        clock = Clock.fixed(NOW, ZoneOffset.UTC);

        habitLog1 = new HabitLog(habit1, HabitLog.Status.CHECKED, NOW, localDateTest);

        habitLog2 = new HabitLog(habit2, HabitLog.Status.CHECKED, NOW, localDateTest);

        habitLogService = new HabitLogService(habitLogRepo, habitRepo, clock);

    }

    @Test
    void C1_when_registering_habit_log_return_correctly_saved_habit_log(){

        Optional<Habit> habitReturn = Optional.of(habit1);

        when(habitRepo.findById(id)).thenReturn(habitReturn);

        when(habitLogRepo.findByLocalDateEqualsAndHabitEquals(localDateTest, habit1)).thenReturn(Optional.empty());

        when(habitLogRepo.saveAndFlush((any(HabitLog.class)))).thenReturn(habitLog1);

        HabitLog savedHabitLog = habitLogService.registerHabitLog(id);

        verify(habitLogRepo).saveAndFlush(habitLogArgumentCaptor.capture());

        HabitLog habitLogSaved = habitLogArgumentCaptor.getValue();

        assertAll(
                () -> assertEquals(localDateTest, habitLogSaved.getLocalDate()),
                () -> assertEquals(HabitLog.Status.CHECKED, habitLogSaved.getStatus()),
                () -> assertEquals(NOW, habitLogSaved.getCompletedAt()),
                () -> assertSame(habit1, habitLogSaved.getHabit()),
                () -> assertSame(habitLog1, savedHabitLog)
        );
    }

    @Test
    void C2_when_register_two_habits_with_different_timezones_and_same_instant_return_both_habit_log_correctly_saved(){
        Optional<Habit> habitReturn = Optional.of(habit1);

        User user2 = new User("mail1@test.com", "America/Los_Angeles");

        habit2.setUser(user2);

        Optional<Habit> habitReturn2 = Optional.of(habit2);

        LocalDate localDateTest2 = LocalDate.of(2026, 11, 5);

        long id2 = 2L;

        when(habitRepo.findById(id)).thenReturn(habitReturn);

        when(habitRepo.findById(id2)).thenReturn(habitReturn2);

        when(habitLogRepo.findByLocalDateEqualsAndHabitEquals(localDateTest, habit1)).thenReturn(Optional.empty());

        when(habitLogRepo.findByLocalDateEqualsAndHabitEquals(localDateTest2, habit2)).thenReturn(Optional.empty());

        when(habitLogRepo.saveAndFlush((any(HabitLog.class)))).thenReturn(habitLog1, habitLog2);

        HabitLog savedHabitLog1 = habitLogService.registerHabitLog(id);


        HabitLog savedHabitLog2 = habitLogService.registerHabitLog(id2);

        verify(habitLogRepo, times(2)).saveAndFlush(habitLogArgumentCaptor.capture());

        List<HabitLog> habitLogs = habitLogArgumentCaptor.getAllValues();

        assertAll(
                () -> assertEquals(localDateTest, habitLogs.getFirst().getLocalDate()),
                () -> assertEquals(HabitLog.Status.CHECKED, habitLogs.getFirst().getStatus()),
                () -> assertEquals(NOW, habitLogs.getFirst().getCompletedAt()),
                () -> assertSame(habit1, habitLogs.getFirst().getHabit()),
                () -> assertSame(habitLog1, savedHabitLog1),

                () -> assertEquals(localDateTest2, habitLogs.get(1).getLocalDate()),
                () -> assertEquals(HabitLog.Status.CHECKED, habitLogs.get(1).getStatus()),
                () -> assertEquals(NOW, habitLogs.get(1).getCompletedAt()),
                () -> assertSame(habit2, habitLogs.get(1).getHabit()),
                () -> assertSame(habitLog2, savedHabitLog2)
        );
    }

    @Test
    void C3_when_register_habit_log_with_inexistent_habit_throw_non_existing_habit_exception(){

        when(habitRepo.findById(id)).thenReturn(Optional.empty());

        assertThrows(NonExistingHabitException.class, () -> habitLogService.registerHabitLog(id));

        verifyNoInteractions(habitLogRepo);
    }

    @Test
    void C4_when_register_habit_log_with_inactive_status_habit_throw_inactive_habit_log_exception(){

        habit1.toggleActive();

        Optional<Habit> habitReturn = Optional.of(habit1);

        when(habitRepo.findById(id)).thenReturn(habitReturn);

        assertThrows(InactiveHabitException.class, () -> habitLogService.registerHabitLog(id));

        verifyNoInteractions(habitLogRepo);
    }

    @Test
    void C5_when_register_habit_log_with_incorrect_timezone_throw_incorrect_timezone_exception(){

        user1.setTimezone("Invalid/Timezone");

        Optional<Habit> habitReturn = Optional.of(habit1);

        when(habitRepo.findById(id)).thenReturn(habitReturn);

        assertThrows(InvalidTimezoneException.class, () -> habitLogService.registerHabitLog(id));

        verifyNoInteractions(habitLogRepo);
    }

    @Test
    void C6_when_registering_saved_before_habit_log_return_correctly_saved_habit_log(){

        Optional<Habit> habitReturn = Optional.of(habit1);

        Optional<HabitLog> habitLogReturn = Optional.of(habitLog1);

        when(habitRepo.findById(id)).thenReturn(habitReturn);

        when(habitLogRepo.findByLocalDateEqualsAndHabitEquals(localDateTest, habit1)).thenReturn(habitLogReturn);

        HabitLog savedHabitLog = habitLogService.registerHabitLog(id);

        verify(habitLogRepo, never()).saveAndFlush(any(HabitLog.class));

        assertAll(
                () -> assertEquals(localDateTest, savedHabitLog.getLocalDate()),
                () -> assertEquals(HabitLog.Status.CHECKED, savedHabitLog.getStatus()),
                () -> assertEquals(NOW, savedHabitLog.getCompletedAt()),
                () -> assertSame(habit1, savedHabitLog.getHabit()),
                () -> assertSame(habitLog1, savedHabitLog)
        );
    }

    @Test
    void C7_when_registering_same_habit_with_same_date_throw_incorrect_habit_log_date_exception(){

        Optional<Habit> habitReturn = Optional.of(habit1);

        when(habitRepo.findById(id)).thenReturn(habitReturn);

        when(habitLogRepo.findByLocalDateEqualsAndHabitEquals(localDateTest, habit1)).thenReturn(Optional.empty());

        DataIntegrityViolationException exceptionThrown = new DataIntegrityViolationException("uk_habit_logs_habit_date", new Throwable("uk_habit_logs_habit_date"));

        when(habitLogRepo.saveAndFlush((any(HabitLog.class)))).thenThrow(exceptionThrown);

        IncorrectHabitLogDateException exceptionReceived = assertThrows(IncorrectHabitLogDateException.class, () -> habitLogService.registerHabitLog(id));

        assertEquals("uk_habit_logs_habit_date", exceptionReceived.getMessage());
    }

    @Test
    void C8_when_registering_same_habit_with_same_date_throw_the_same_exception(){

        Optional<Habit> habitReturn = Optional.of(habit1);

        when(habitRepo.findById(id)).thenReturn(habitReturn);

        when(habitLogRepo.findByLocalDateEqualsAndHabitEquals(localDateTest, habit1)).thenReturn(Optional.empty());

        String exceptionMessage = "different cause";

        DataIntegrityViolationException exceptionThrown = new DataIntegrityViolationException(exceptionMessage, new Throwable("different cause"));

        when(habitLogRepo.saveAndFlush((any(HabitLog.class)))).thenThrow(exceptionThrown);

        DataIntegrityViolationException exceptionReceived = assertThrows(DataIntegrityViolationException.class, () -> habitLogService.registerHabitLog(id));

        assertAll(
                () -> assertEquals(exceptionMessage, exceptionReceived.getMessage()),
                () -> assertSame(exceptionThrown, exceptionReceived)
        );
    }
}
