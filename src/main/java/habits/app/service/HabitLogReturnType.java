package habits.app.service;

import habits.app.domain.HabitLog;

public record HabitLogReturnType (
        HabitLog habitLog,
        boolean created
){ }