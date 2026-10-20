package habits.app.domain;
import java.time.Instant;
import java.time.LocalDate;
import jakarta.persistence.*;


@Entity()
@Table(
        name = "habit_logs",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_habit_logs_habit_date",
                columnNames = {"habit_id", "local_date"}
        )
)
public class HabitLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "habit_id", nullable = false)
    private Habit habit;

    private Instant completedAt;

    private LocalDate localDate;

    private String status;

    protected HabitLog(){
    }

    public HabitLog(Habit habit, String status){
        this.habit = habit;
        this.completedAt = Instant.now();
        this.localDate = LocalDate.now();
        this.status = status;
    }

    public Long getId(){
        return this.id;
    }

    public Habit getHabit(){
        return this.habit;
    }

    public void setHabit(Habit habit){
        this.habit = habit;
    }

    public Instant getCompletedAt(){
        return this.completedAt;
    }

    public void setCompletedAt(Instant completedAt){
        this.completedAt = completedAt;
    }

    public LocalDate getLocalDate(){
        return this.localDate;
    }

    public void setLocalDate(LocalDate localDate){
        this.localDate = localDate;
    }

    public String getStatus(){
        return this.status;
    }

    public void setStatus(String status){
        this.status = status;
    }
}
