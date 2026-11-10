package habits.app.api.dto;

import habits.app.domain.HabitLog.Status;

import java.time.Instant;
import java.time.LocalDate;

public record HabitLogOut(

        long id,

        long habitId,

        Status status,

        Instant completedAt,

        LocalDate localDate) { }
