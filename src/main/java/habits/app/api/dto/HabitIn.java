package habits.app.api.dto;

import habits.app.service.HabitService;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.Instant;

public record HabitIn (
        @NotNull
        @Size(max = HabitService.MAX_NAME_LENGTH)
        String name,

        @Size(max = HabitService.MAX_DESCRIPTION_LENGTH)
        String description
) { }
