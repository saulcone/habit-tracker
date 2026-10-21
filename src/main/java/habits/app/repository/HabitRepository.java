package habits.app.repository;
import habits.app.domain.Habit;
import habits.app.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface HabitRepository extends JpaRepository<Habit, Long>{
    List<Habit> findByIsActiveTrueAndUserEquals(User user);
}
