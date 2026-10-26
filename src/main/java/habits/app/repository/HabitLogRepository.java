package habits.app.repository;
import habits.app.domain.Habit;
import habits.app.domain.HabitLog;
import habits.app.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;

    import java.time.LocalDate;
import java.util.List;

public interface HabitLogRepository extends JpaRepository<HabitLog, Long> {
    List<HabitLog> findByLocalDateBetweenAndHabitEqualsOrderByLocalDate(LocalDate begin, LocalDate end, Habit habit);
    HabitLog findByLocalDateEqualsAndHabitEquals(LocalDate date, Habit habit);
    boolean existsByHabitAndLocalDateEquals(Habit habit, LocalDate date);

    HabitLog habit(Habit habit);
}
