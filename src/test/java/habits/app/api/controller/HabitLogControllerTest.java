package habits.app.api.controller;

import habits.app.domain.Habit;
import habits.app.domain.HabitLog;
import habits.app.domain.User;
import habits.app.exception.*;
import habits.app.service.HabitLogReturnType;
import habits.app.service.HabitLogService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;

import java.time.*;
import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(HabitLogController.class)
public class HabitLogControllerTest {
    @Autowired
    MockMvc mockMvc;

    @MockitoBean
    HabitLogService habitLogService;

    Habit habit1;

    User user1;

    Instant completedAt;

    LocalDate localDate;

    private static final Instant NOW = Instant.parse("2026-11-05T23:30:00Z");

    static final long userId = 3L;

    static final long habitId = 2L;

    static final long habitLogId = 1L;

    HabitLog habitLog1;

    @BeforeEach
    void setUp(){

        user1 = new User("mail@test.com", "Europe/Madrid");
        ReflectionTestUtils.setField(user1, "id", userId);

        habit1 = new Habit(user1, "Habit 1", "Do push ups");
        ReflectionTestUtils.setField(habit1, "id", habitId);

        completedAt = NOW;
        localDate = LocalDate.of(2026, 11, 6);

        habitLog1 = new HabitLog(habit1, HabitLog.Status.CHECKED, completedAt, localDate);
        ReflectionTestUtils.setField(habitLog1, "id", habitLogId);
    }

    @Test
    void C1_when_creating_correct_log_and_not_existing_return_correctly_saved_habit_and_201_code() throws Exception{

        HabitLogReturnType returnType = new HabitLogReturnType(habitLog1, true);

        when(habitLogService.registerHabitLog(habitId)).thenReturn(returnType);

        mockMvc.perform(post("/api/v1/habits/{habitId}/logs", habitId))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(habitLogId))
                .andExpect(jsonPath("$.habitId").value(habitId))
                .andExpect(jsonPath("$.status").value("CHECKED"))
                .andExpect(jsonPath("$.completedAt").value("2026-11-05T23:30:00Z"))
                .andExpect(jsonPath("$.localDate").value("2026-11-06"));

        verify(habitLogService).registerHabitLog(habitId);
    }

    @Test
    void C2_when_creating_correct_log_and_exists_in_db_return_correctly_saved_habit_and_200_code() throws Exception{

        HabitLogReturnType returnType = new HabitLogReturnType(habitLog1, false);

        when(habitLogService.registerHabitLog(habitId)).thenReturn(returnType);

        mockMvc.perform(post("/api/v1/habits/{habitId}/logs", habitId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(habitLogId))
                .andExpect(jsonPath("$.habitId").value(habitId))
                .andExpect(jsonPath("$.status").value("CHECKED"))
                .andExpect(jsonPath("$.completedAt").value("2026-11-05T23:30:00Z"))
                .andExpect(jsonPath("$.localDate").value("2026-11-06"));

        verify(habitLogService).registerHabitLog(habitId);
    }

    @Test
    void C3_when_creating_log_and_habit_doesnt_exist_return_404_code() throws Exception{

        String expectedMessage = "The habit with id " + habitId + " doesn't exist.";

        NonExistingHabitException returnException = new NonExistingHabitException(expectedMessage);

        when(habitLogService.registerHabitLog(habitId)).thenThrow(returnException);

        mockMvc.perform(post("/api/v1/habits/{habitId}/logs", habitId))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.detail").value(expectedMessage));

        verify(habitLogService).registerHabitLog(habitId);
    }

    @Test
    void C4_when_creating_log_and_habit_is_inactive_return_409_code() throws Exception{

        String expectedMessage = "This habit is not active now.";

        InactiveHabitException returnException = new InactiveHabitException(expectedMessage);

        when(habitLogService.registerHabitLog(habitId)).thenThrow(returnException);

        mockMvc.perform(post("/api/v1/habits/{habitId}/logs", habitId))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.detail").value(expectedMessage));

        verify(habitLogService).registerHabitLog(habitId);
    }

    @Test
    void C5_when_creating_log_and_race_condition_creating_log_receive_409_code() throws Exception{

        String expectedMessage = "The habit already exists for that day.";

        IncorrectHabitLogDateException returnException = new IncorrectHabitLogDateException(expectedMessage);

        when(habitLogService.registerHabitLog(habitId)).thenThrow(returnException);

        mockMvc.perform(post("/api/v1/habits/{habitId}/logs", habitId))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.detail").value(expectedMessage));

        verify(habitLogService).registerHabitLog(habitId);
    }

    @Test
    void C6_when_creating_log_and_invalid_time_zone_of_user_creating_log_receive_500_code_and_generic_message() throws Exception{

        String thrownMessage = "Different message, simulation conflictive information.";

        String expectedMessage = "Not recognised system exception thrown.";

        IncorrectZoneIdException returnException = new IncorrectZoneIdException(thrownMessage);

        when(habitLogService.registerHabitLog(habitId)).thenThrow(returnException);

        mockMvc.perform(post("/api/v1/habits/{habitId}/logs", habitId))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.detail").value(expectedMessage));

        verify(habitLogService).registerHabitLog(habitId);
    }

    @Test
    void C7_when_creating_log_and_db_constraint_violated_creating_log_receive_500_code_and_generic_message() throws Exception{

        String thrownMessage = "Different message, simulation conflictive information.";

        String expectedMessage = "Not recognised system exception thrown.";

        DataIntegrityViolationException returnException = new DataIntegrityViolationException(thrownMessage);

        when(habitLogService.registerHabitLog(habitId)).thenThrow(returnException);

        mockMvc.perform(post("/api/v1/habits/{habitId}/logs", habitId))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.detail").value(expectedMessage));

        verify(habitLogService).registerHabitLog(habitId);
    }

    @Test
    void C8_when_creating_log_unexpected_error_creating_log_receive_500_code_and_generic_message() throws Exception{

        String thrownMessage = "Different message, simulation conflictive information.";

        String expectedMessage = "Not recognised system exception thrown.";

        RuntimeException returnException = new RuntimeException(thrownMessage);

        when(habitLogService.registerHabitLog(habitId)).thenThrow(returnException);

        mockMvc.perform(post("/api/v1/habits/{habitId}/logs", habitId))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.detail").value(expectedMessage));

        verify(habitLogService).registerHabitLog(habitId);
    }

    @Test
    void C9_when_creating_log_with_incorrect_id_receive_400_code_and_service_not_called() throws Exception{

        String badHabitId = "abcd";

        mockMvc.perform(post("/api/v1/habits/{habitId}/logs", badHabitId))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(habitLogService);
    }

    @Test
    void C10_when_getting_logs_from_between_two_correct_dates_return_two_logs_without_error() throws Exception{

        LocalDate beginTest = LocalDate.of(2000, 11, 1);

        LocalDate endTest = LocalDate.of(2000, 11, 10);

        HabitLog habitLog2 = new HabitLog(habit1, HabitLog.Status.CHECKED, Instant.parse("2026-11-07T10:30:00Z"), LocalDate.of(2026, 11, 7));

        ReflectionTestUtils.setField(habitLog2, "id", 8L);

        List<HabitLog> returnLogs = new ArrayList<>();

        returnLogs.add(habitLog1);
        returnLogs.add(habitLog2);

        when(habitLogService.getHabitLogsBetweenDates(beginTest, endTest, habitId)).thenReturn(returnLogs);

        mockMvc.perform(get("/api/v1/habits/{habitId}/logs", habitId).param("begin", beginTest.toString()).param("end", endTest.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))

                .andExpect(jsonPath("$[0].id").value(habitLog1.getId()))
                .andExpect(jsonPath("$[0].completedAt").value("2026-11-05T23:30:00Z"))
                .andExpect(jsonPath("$[0].status").value("CHECKED"))
                .andExpect(jsonPath("$[0].localDate").value(habitLog1.getLocalDate().toString()))
                .andExpect(jsonPath("$[0].habitId").value(habitId))
                .andExpect(jsonPath("$[1].id").value(habitLog2.getId()))
                .andExpect(jsonPath("$[1].completedAt").value("2026-11-07T10:30:00Z"))
                .andExpect(jsonPath("$[1].status").value("CHECKED"))
                .andExpect(jsonPath("$[1].localDate").value(habitLog2.getLocalDate().toString()))
                .andExpect(jsonPath("$[1].habitId").value(habitId));

        verify(habitLogService).getHabitLogsBetweenDates(beginTest, endTest, habitId);
    }

    @Test
    void C11_when_getting_logs_from_between_two_correct_dates_return_empty_list_without_error() throws Exception{

        LocalDate beginTest = LocalDate.of(2000, 11, 1);

        LocalDate endTest = LocalDate.of(2000, 11, 10);

        List<HabitLog> returnLogs = new ArrayList<>();


        when(habitLogService.getHabitLogsBetweenDates(beginTest, endTest, habitId)).thenReturn(returnLogs);

        mockMvc.perform(get("/api/v1/habits/{habitId}/logs", habitId).param("begin", beginTest.toString()).param("end", endTest.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));

        verify(habitLogService).getHabitLogsBetweenDates(beginTest, endTest, habitId);
    }

    @Test
    void C12_when_getting_logs_from_incorrect_period_dates_then_throw_invalid_date_period_exception() throws Exception{

        LocalDate beginTest = LocalDate.of(2000, 11, 10);

        LocalDate endTest = LocalDate.of(2000, 11, 1);

        String expectedMessage = "The beginning date: " + beginTest + " is after the end period date: " + endTest;

        InvalidDatePeriodException thrownException = new InvalidDatePeriodException(expectedMessage);

        when(habitLogService.getHabitLogsBetweenDates(beginTest, endTest, habitId)).thenThrow(thrownException);

        mockMvc.perform(get("/api/v1/habits/{habitId}/logs", habitId).param("begin", beginTest.toString()).param("end", endTest.toString()))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.detail").value(expectedMessage));

        verify(habitLogService).getHabitLogsBetweenDates(beginTest, endTest, habitId);
    }

    @Test
    void C13_when_getting_logs_from_incorrect_period_dates_then_throw_non_existing_habit_exception_and_404_code() throws Exception{

        LocalDate beginTest = LocalDate.of(2000, 11, 1);

        LocalDate endTest = LocalDate.of(2000, 11, 10);

        String expectedMessage = "The habit with id " + habitId + " doesn't exist.";

        NonExistingHabitException thrownException = new NonExistingHabitException(expectedMessage);

        when(habitLogService.getHabitLogsBetweenDates(beginTest, endTest, habitId)).thenThrow(thrownException);

        mockMvc.perform(get("/api/v1/habits/{habitId}/logs", habitId).param("begin", beginTest.toString()).param("end", endTest.toString()))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.detail").value(expectedMessage));

        verify(habitLogService).getHabitLogsBetweenDates(beginTest, endTest, habitId);
    }

    @Test
    void C14_when_getting_logs_with_incorrect_habitId_format_return_400_code() throws Exception{

        LocalDate beginTest = LocalDate.of(2000, 11, 1);

        LocalDate endTest = LocalDate.of(2000, 11, 10);

        mockMvc.perform(get("/api/v1/habits/{habitId}/logs", "ABDC").param("begin", beginTest.toString()).param("end", endTest.toString()))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(habitLogService);
    }

    @Test
    void C15_when_getting_logs_with_lacking_parameters_format_return_400_code() throws Exception{

        LocalDate beginTest = LocalDate.of(2000, 11, 10);

        mockMvc.perform(get("/api/v1/habits/{habitId}/logs", habitId).param("begin", beginTest.toString()))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(habitLogService);
    }

    @Test
    void C16_when_getting_logs_with_invalid_parameters_format_return_400_code() throws Exception{
        mockMvc.perform(get("/api/v1/habits/{habitId}/logs", habitId).param("begin", "begin date").param("end", "final date"))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(habitLogService);
    }

}
