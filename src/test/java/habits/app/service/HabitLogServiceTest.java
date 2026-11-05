package habits.app.service;

import habits.app.domain.Habit;
import habits.app.domain.HabitLog;
import habits.app.domain.User;
import habits.app.repository.HabitLogRepository;
import habits.app.repository.HabitRepository;
import habits.app.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
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
    void when_registering_habit_log_return_correctly_saved_habit_log(){

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
}
