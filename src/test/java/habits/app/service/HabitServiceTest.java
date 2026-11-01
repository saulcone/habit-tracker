package habits.app.service;

import habits.app.domain.Habit;
import habits.app.domain.User;
import habits.app.exception.*;
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

import java.util.Optional;

import static habits.app.service.HabitService.MAX_DESCRIPTION_LENGTH;
import static habits.app.service.HabitService.MAX_NAME_LENGTH;
import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class HabitServiceTest {

    @Mock
    HabitRepository habitRepo;

    @Mock
    UserRepository userRepo;

    @InjectMocks
    HabitService habitService;



    @Captor
    ArgumentCaptor<Habit> habitCaptor;

    User user1;

    Habit habit1;

    final long id = 1L;

    @BeforeEach
    void setUp() {
        user1 = new User("mail@test.com", "Madrid/Europe");

        habit1 = new Habit(user1, "Habit 1", "Study Chinese");

    }

    @Test
    void C1_when_name_is_empty_throw_empty_field_exception(){

        habit1.setName("");

        EmptyFieldException e = assertThrows(EmptyFieldException.class, () -> habitService.registerHabit(id, habit1.getName(), habit1.getDescription()));

        assertTrue(e.getMessage().contains("name"));
    }

    @Test
    void C2_when_name_is_more_than_100_characters_throw_field_too_long_exception(){

        habit1.setName("a".repeat(MAX_NAME_LENGTH + 1));

        FieldTooLongException e = assertThrows(FieldTooLongException.class, () -> habitService.registerHabit(id, habit1.getName(), habit1.getDescription()));

        assertTrue(e.getMessage().contains("name"));
    }

    @Test
    void C3_when_name_is_null_throw_null_field_exception(){

        habit1.setName(null);

        NullFieldException e = assertThrows(NullFieldException.class, () -> habitService.registerHabit(id, habit1.getName(), habit1.getDescription()));

        assertTrue(e.getMessage().contains("name"));
    }

    @Test
    void C4_when_description_lenght_is_more_than_300_characters_throw_field_too_long_exception(){

        habit1.setDescription("a".repeat(MAX_DESCRIPTION_LENGTH + 1));

        FieldTooLongException e = assertThrows(FieldTooLongException.class, () -> habitService.registerHabit(id, habit1.getName(), habit1.getDescription()));

        assertTrue(e.getMessage().contains("description"));
    }

    @Test
    void C5_when_description_is_null_throw_null_field_exception(){

        habit1.setDescription(null);

        NullFieldException e = assertThrows(NullFieldException.class, () -> habitService.registerHabit(id, habit1.getName(), habit1.getDescription()));

        assertTrue(e.getMessage().contains("description"));
    }

    @Test
    void C6_when_name_begins_or_ends_with_space_throw_invalid_field_exception(){

        habit1.setName(" Habit 1");

        InvalidNameException e = assertThrows(InvalidNameException.class, () -> habitService.registerHabit(id, habit1.getName(), habit1.getDescription()));

        assertTrue(e.getMessage().contains("name"));
    }

    @Test
    void C7_when_description_begins_or_ends_with_space_throw_invalid_field_exception(){

        habit1.setDescription("Description ");

        InvalidDescriptionException e = assertThrows(InvalidDescriptionException.class, () -> habitService.registerHabit(id, habit1.getName(), habit1.getDescription()));

        assertTrue(e.getMessage().contains("description"));
    }

    @Test
    void C8_when_checking_non_existent_user_throw_new_non_existing_user_exception(){

        NonExistingUserException thrownException = new NonExistingUserException("User with id: " + id + " not found");

        when(userRepo.findById(id)).thenThrow(thrownException);

        assertThrows(NonExistingUserException.class, () -> habitService.registerHabit(id, habit1.getName(), habit1.getDescription()));
    }

    @Test
    void C9_when_register_correct_habit_return_habit_correctly_saved(){

        String habitName = "Habit test";

        String habitDesc = "Habit desc";

        Habit habitToSave = new Habit(user1,  habitName, habitDesc);

        when(userRepo.findById(id)).thenReturn(Optional.of(user1));
        when(habitRepo.saveAndFlush(any(Habit.class))).thenReturn(habitToSave);

        Habit savedHabit = habitService.registerHabit(id, habitName, habitDesc);

        verify(habitRepo).saveAndFlush(habitCaptor.capture());

        Habit value = habitCaptor.getValue();

        assertAll(
                () -> assertSame(habitToSave, savedHabit),
                () -> assertEquals(habitName,  value.getName()),
                () -> assertEquals(habitDesc, value.getDescription())
        );

    }
}
