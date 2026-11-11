package habits.app.api.controller;

import habits.app.domain.User;
import habits.app.exception.ExistingEmailException;
import habits.app.exception.InvalidTimezoneException;
import habits.app.service.UserService;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;

import org.springframework.http.MediaType;

import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;


import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(UserController.class)
public class UserControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    UserService userService;

    static final long id = 1L;


    @Test
    void C1_when_registering_correct_user_receive_a_201_code_and_correctly_saved_user() throws Exception{

        String jsonIn = """
                {
                    "email" : "mail@test.com",
                    "timezone" : "Europe/Madrid"
                }
                """;

        String email = "mail@test.com";
        String timezone = "Europe/Madrid";

        User userReturn = new User(email, timezone);

        ReflectionTestUtils.setField(userReturn, "id", id);

        when(userService.registerUser(email, timezone)).thenReturn(userReturn);

         mockMvc.perform(post("/api/v1/users").contentType(MediaType.APPLICATION_JSON).content(jsonIn))
                 .andExpect(status().isCreated())
                 .andExpect(jsonPath("$.email").value(email))
                 .andExpect(jsonPath("$.timezone").value(timezone))
                 .andExpect(jsonPath("$.id").value(id));

         verify(userService).registerUser(email, timezone);
     }

    @Test
    void C2_when_registering_user_with_incorrect_email_receive_a_400_code() throws Exception{

        String jsonIn = """
                {
                    "email" : "mailtest.com",
                    "timezone" : "Europe/Madrid"
                }
                """;

        mockMvc.perform(post("/api/v1/users").contentType(MediaType.APPLICATION_JSON).content(jsonIn))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(userService);
    }

    @Test
    void C3_when_registering_existing_email_user_receive_a_409_code() throws Exception{

        String jsonIn = """
                {
                    "email" : "mail@test.com",
                    "timezone" : "Europe/Madrid"
                }
                """;

        String email = "mail@test.com";
        String timezone = "Europe/Madrid";


        ExistingEmailException existingEmailException = new ExistingEmailException("The email already exists in the system");

        when(userService.registerUser(email, timezone)).thenThrow(existingEmailException);

        mockMvc.perform(post("/api/v1/users").contentType(MediaType.APPLICATION_JSON).content(jsonIn))
                .andExpect(status().isConflict());

        verify(userService).registerUser(email, timezone);
    }

    @Test
    void C4_when_registering_user_with_incorrect_json_format_receive_a_400_code() throws Exception{

        String jsonIn = """
                NOT CORRECT JSON
                """;


        mockMvc.perform(post("/api/v1/users").contentType(MediaType.APPLICATION_JSON).content(jsonIn))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(userService);
    }

    @Test
    void C5_when_receiving_runtime_exception_when_registering_user_receive_a_500_code_and_generic_message() throws Exception{

        String jsonIn = """
                {
                    "email" : "mail@test.com",
                    "timezone" : "Europe/Madrid"
                }
                """;

        String email = "mail@test.com";
        String timezone = "Europe/Madrid";

        RuntimeException unexpectedException = new RuntimeException("SQL: Table Users");

        when(userService.registerUser(email, timezone)).thenThrow(unexpectedException);

        String expectedMessage = "Not recognised system exception thrown.";

        mockMvc.perform(post("/api/v1/users").contentType(MediaType.APPLICATION_JSON).content(jsonIn))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.detail").value(expectedMessage));

        verify(userService).registerUser(email, timezone);
    }

    @Test
    void C6_when_receiving_invalid_timezone_exception_when_registering_user_receive_a_400_code_and_custom_message() throws Exception{

        String jsonIn = """
                {
                    "email" : "mail@test.com",
                    "timezone" : "Europe/Invalid"
                }
                """;

        String email = "mail@test.com";
        String timezone = "Europe/Invalid";

        String expectedMessage = "This timezone is not correct.";

        InvalidTimezoneException unexpectedException = new InvalidTimezoneException(expectedMessage);

        when(userService.registerUser(email, timezone)).thenThrow(unexpectedException);

        mockMvc.perform(post("/api/v1/users").contentType(MediaType.APPLICATION_JSON).content(jsonIn))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.detail").value(expectedMessage));

        verify(userService).registerUser(email, timezone);
    }

    @Test
    void C7_when_receiving_json_with_no_timezone_when_registering_user_receive_a_400_code_and_custom_message() throws Exception{

        String jsonIn = """
                {
                    "email" : "mail@test.com"
                }
                """;


        mockMvc.perform(post("/api/v1/users").contentType(MediaType.APPLICATION_JSON).content(jsonIn))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(userService);
    }
}
