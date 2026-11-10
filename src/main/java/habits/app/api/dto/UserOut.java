package habits.app.api.dto;

import jakarta.validation.constraints.*;

import java.time.Instant;
import java.util.TimeZone;

public record UserOut (   long id,

                          String email,

                          String timezone,

                          Instant createdAt){ }
