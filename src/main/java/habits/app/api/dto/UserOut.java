package habits.app.api.dto;

import java.time.Instant;

public record UserOut (   long id,

                          String email,

                          String timezone,

                          Instant createdAt){ }
