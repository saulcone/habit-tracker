package habits.app.api.controller;

import habits.app.domain.Habit;
import habits.app.domain.User;
import habits.app.exception.*;
import habits.app.service.HabitService;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;

import java.util.ArrayList;
import java.util.List;

import static habits.app.service.HabitService.MAX_DESCRIPTION_LENGTH;
import static habits.app.service.HabitService.MAX_NAME_LENGTH;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.nullValue;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(HabitController.class)
public class HabitControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    HabitService habitService;

    static final long habitId = 1L;
    static final long userId = 2L;

    @Test
    void C1_when_registering_correct_habit_receive_a_201_code_and_correctly_saved_habit() throws Exception{


        String name = "Habit 1";
        String description = "Habit's description.";

        String jsonIn = """
                {
                    "name" : "%s",
                    "description" : "%s"
                }
                """.formatted(name, description);

        String email = "mail@test.com";
        String timezone = "Europe/Madrid";

        User user = new User(email, timezone);

        ReflectionTestUtils.setField(user, "id", userId);

        Habit habitReturn = new Habit(user, name, description);

        ReflectionTestUtils.setField(habitReturn, "id", habitId);

        when(habitService.registerHabit(userId, name, description)).thenReturn(habitReturn);

        mockMvc.perform(post("/api/v1/users/{userId}/habits", userId).contentType(MediaType.APPLICATION_JSON).content(jsonIn))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value(name))
                .andExpect(jsonPath("$.description").value(description))
                .andExpect(jsonPath("$.id").value(habitId))
                .andExpect(jsonPath("$.userId").value(userId));

        verify(habitService).registerHabit(userId, name, description);
    }

    @Test
    void C2_when_registering_with_non_name_field_in_json_receive_400_code() throws Exception{
        String description = "Habit's description.";

        String jsonIn = """
                {
                    "description" : "%s"
                }
                """.formatted(description);

        mockMvc.perform(post("/api/v1/users/{userId}/habits", userId).contentType(MediaType.APPLICATION_JSON).content(jsonIn))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(habitService);
    }

    @Test
    void C3_when_registering_with_longer_than_accepted_name_field_in_json_receive_400_code() throws Exception{

        String name = "a".repeat(MAX_NAME_LENGTH + 1);
        String description = "Habit's description.";

        String jsonIn = """
                {
                    "name" : "%s",
                    "description" : "%s"
                }
                """.formatted(name, description);

        mockMvc.perform(post("/api/v1/users/{userId}/habits", userId).contentType(MediaType.APPLICATION_JSON).content(jsonIn))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(habitService);
    }

    @Test
    void C4_when_registering_with_longer_than_accepted_description_field_in_json_receive_400_code() throws Exception{

        String name = "Habit 1";
        String description = "a".repeat(MAX_DESCRIPTION_LENGTH + 1);

        String jsonIn = """
                {
                    "name" : "%s",
                    "description" : "%s"
                }
                """.formatted(name, description);

        mockMvc.perform(post("/api/v1/users/{userId}/habits", userId).contentType(MediaType.APPLICATION_JSON).content(jsonIn))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(habitService);
    }

    @Test
    void C5_when_registering_habit_with_empty_name_receive_a_400_code() throws Exception{


        String name = "";
        String description = "Habit's description.";

        String jsonIn = """
                {
                    "name" : "%s",
                    "description" : "%s"
                }
                """.formatted(name, description);

        String expectedMessage = "Field name is empty";

        EmptyFieldException thrownException = new EmptyFieldException(expectedMessage);

        when(habitService.registerHabit(userId, name, description)).thenThrow(thrownException);

        mockMvc.perform(post("/api/v1/users/{userId}/habits", userId).contentType(MediaType.APPLICATION_JSON).content(jsonIn))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.detail").value(expectedMessage));

        verify(habitService).registerHabit(userId, name, description);
    }

    @Test
    void C6_when_registering_habit_with_spaced_in_first_characters_of_name_receive_a_400_code() throws Exception{


        String name = " Habit 1";
        String description = "Habit's description.";

        String jsonIn = """
                {
                    "name" : "%s",
                    "description" : "%s"
                }
                """.formatted(name, description);


        String expectedMessage = "Field name begins or ends with empty space";

        InvalidNameException thrownException = new InvalidNameException(expectedMessage);

        when(habitService.registerHabit(userId, name, description)).thenThrow(thrownException);

        mockMvc.perform(post("/api/v1/users/{userId}/habits", userId).contentType(MediaType.APPLICATION_JSON).content(jsonIn))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.detail").value(expectedMessage));

        verify(habitService).registerHabit(userId, name, description);
    }

    @Test
    void C7_when_registering_habit_with_spaced_in_first_characters_of_description_receive_a_400_code() throws Exception{


        String name = "Habit 1";
        String description = " Habit's description.";

        String jsonIn = """
                {
                    "name" : "%s",
                    "description" : "%s"
                }
                """.formatted(name, description);

        String expectedMessage = "Field description begins or ends with empty space";

        InvalidDescriptionException thrownException = new InvalidDescriptionException(expectedMessage);

        when(habitService.registerHabit(userId, name, description)).thenThrow(thrownException);

        mockMvc.perform(post("/api/v1/users/{userId}/habits", userId).contentType(MediaType.APPLICATION_JSON).content(jsonIn))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.detail").value(expectedMessage));

        verify(habitService).registerHabit(userId, name, description);
    }

    @Test
    void C8_when_registering_habit_non_existing_user_receive_a_404_code() throws Exception{


        String name = "Habit 1";
        String description = "Habit's description.";

        String jsonIn = """
                {
                    "name" : "%s",
                    "description" : "%s"
                }
                """.formatted(name, description);

        String expectedMessage = "User with id: " + userId + " not found";

        NonExistingUserException thrownException = new NonExistingUserException(expectedMessage);

        when(habitService.registerHabit(userId, name, description)).thenThrow(thrownException);

        mockMvc.perform(post("/api/v1/users/{userId}/habits", userId).contentType(MediaType.APPLICATION_JSON).content(jsonIn))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.detail").value(expectedMessage));

        verify(habitService).registerHabit(userId, name, description);
    }

    @Test
    void C9_when_registering_duplicated_habit_receive_a_409_code() throws Exception{


        String name = "Habit 1";
        String description = "Habit's description.";

        String jsonIn = """
                {
                    "name" : "%s",
                    "description" : "%s"
                }
                """.formatted(name, description);

        String expectedMessage = "Habit's name already existing.";

        ExistingHabitForThatUserException thrownException = new ExistingHabitForThatUserException(expectedMessage);

        when(habitService.registerHabit(userId, name, description)).thenThrow(thrownException);

        mockMvc.perform(post("/api/v1/users/{userId}/habits", userId).contentType(MediaType.APPLICATION_JSON).content(jsonIn))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.detail").value(expectedMessage));

        verify(habitService).registerHabit(userId, name, description);
    }

    @Test
    void C10_when_registering_habit_and_unknown_error_is_thrown_receive_a_500_code_with_generic_message() throws Exception{


        String name = "Habit 1";
        String description = "Habit's description.";

        String jsonIn = """
                {
                    "name" : "%s",
                    "description" : "%s"
                }
                """.formatted(name, description);


        String thrownMessage = "SQL: Table Users";

        String expectedMessage = "Not recognised system exception thrown.";

        RuntimeException thrownException = new RuntimeException(thrownMessage);

        when(habitService.registerHabit(userId, name, description)).thenThrow(thrownException);

        mockMvc.perform(post("/api/v1/users/{userId}/habits", userId).contentType(MediaType.APPLICATION_JSON).content(jsonIn))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.detail").value(expectedMessage));

        verify(habitService).registerHabit(userId, name, description);
    }

    @Test
    void C11_when_registering_habit_and_user_id_is_not_number_receive_a_400_code() throws Exception{
        String name = "Habit 1";
        String description = "Habit's description.";


        String jsonIn = """
                {
                    "name" : "%s",
                    "description" : "%s"
                }
                """.formatted(name, description);

        String notCorrectUserId = "NOT_CORRECT";

        mockMvc.perform(post("/api/v1/users/{userId}/habits", notCorrectUserId).contentType(MediaType.APPLICATION_JSON).content(jsonIn))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(habitService);
    }

    @Test
    void C12_when_registering_correct_habit_with_no_description_receive_a_201_code_and_correctly_saved_habit() throws Exception{


        String name = "Habit 1";


        String jsonIn = """
                {
                    "name" : "%s"
                }
                """.formatted(name);

        String email = "mail@test.com";
        String timezone = "Europe/Madrid";

        User user = new User(email, timezone);

        ReflectionTestUtils.setField(user, "id", userId);

        Habit habitReturn = new Habit(user, name, null);

        ReflectionTestUtils.setField(habitReturn, "id", habitId);

        when(habitService.registerHabit(userId, name, null)).thenReturn(habitReturn);

        mockMvc.perform(post("/api/v1/users/{userId}/habits", userId).contentType(MediaType.APPLICATION_JSON).content(jsonIn))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value(name))
                .andExpect(jsonPath("$.description").value(nullValue()))
                .andExpect(jsonPath("$.id").value(habitId))
                .andExpect(jsonPath("$.userId").value(userId));

        verify(habitService).registerHabit(userId, name, null);
    }

    @Test
    void C13_when_getting_habits_from_user_with_two_habits_obtain_habits_and_200_code() throws Exception{

        String email = "mail@test.com";
        String timezone = "Europe/Madrid";

        User user = new User(email, timezone);

        ReflectionTestUtils.setField(user, "id", userId);

        Habit habitReturn1 = new Habit(user, "Habit 1", "Description 1.");

        ReflectionTestUtils.setField(habitReturn1, "id", 7L);

        Habit habitReturn2 = new Habit(user, "Habit 2", "Description 2.");

        ReflectionTestUtils.setField(habitReturn2, "id", 8L);

        List<Habit> habitsReturn = new ArrayList<>();

        habitsReturn.add(habitReturn1);
        habitsReturn.add(habitReturn2);

        when(habitService.getUserActiveHabits(userId)).thenReturn(habitsReturn);

        mockMvc.perform(get("/api/v1/users/{userId}/habits", userId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].name").value(habitReturn1.getName()))
                .andExpect(jsonPath("$[1].name").value(habitReturn2.getName()))
                .andExpect(jsonPath("$[0].id").value(habitReturn1.getId()))
                .andExpect(jsonPath("$[1].id").value(habitReturn2.getId()))
                .andExpect(jsonPath("$[0].userId").value(userId))
                .andExpect(jsonPath("$[1].userId").value(userId))
                .andExpect(jsonPath("$[0].description").value(habitReturn1.getDescription()))
                .andExpect(jsonPath("$[1].description").value(habitReturn2.getDescription()));

        verify(habitService).getUserActiveHabits(userId);
    }

    @Test
    void C14_when_getting_habits_from_user_with_no_habits_obtain_empty_list_and_200_code() throws Exception{

        when(habitService.getUserActiveHabits(userId)).thenReturn(new ArrayList<>());

        mockMvc.perform(get("/api/v1/users/{userId}/habits", userId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));

        verify(habitService).getUserActiveHabits(userId);
    }

    @Test
    void C15_when_getting_habits_from_non_existing_user_404_code() throws Exception{

        String expectedMessage = "User with id: " + userId + " not found";

        NonExistingUserException returnException = new NonExistingUserException(expectedMessage);

        when(habitService.getUserActiveHabits(userId)).thenThrow(returnException);

        mockMvc.perform(get("/api/v1/users/{userId}/habits", userId))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.detail").value(expectedMessage));

        verify(habitService).getUserActiveHabits(userId);
    }

    @Test
    void C16_when_getting_habits_with_non_long_format_user_id_400_code() throws Exception{

        String incorrectId = "abcd";

        mockMvc.perform(get("/api/v1/users/{userId}/habits", incorrectId))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(habitService);
    }

}
