package habits.app.api.dto;

import habits.app.service.HabitService;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.Instant;

public record HabitOut(   long id,

                          long userId,

                          String name,

                          String description,

                          Instant createdAt) { }
