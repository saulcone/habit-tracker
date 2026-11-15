package habits.app.api.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;

public record UserIn(@NotNull
                    @Email
                    String email,

                    @NotNull
                    String timezone) { }
