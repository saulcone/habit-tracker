package habits.app.api.dto;

import java.time.Instant;

public record HabitOut(   long id,

                          long userId,

                          String name,

                          String description,

                          Instant createdAt) { }
