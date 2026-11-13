package habits.app.service;

import habits.app.domain.HabitLog;
import org.springframework.http.HttpStatusCode;

public record HabitLogReturnType (
        HabitLog habitLog,
        boolean created
){ }