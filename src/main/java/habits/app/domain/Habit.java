package habits.app.domain;
import jakarta.persistence.*;
import java.time.Instant;

@Entity(name = "habits")
public class Habit {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    private String name;

    private String description;

    private boolean isActive = true;

    private Instant createdAt = Instant.now();

    protected Habit(){
    }

    public Habit(User user, String name, String description) {
        this.user = user;
        this.name = name;
        this.description = description;
        this.isActive = true;
        this.createdAt = Instant.now();
    }

    public Long getId(){
        return this.id;
    }

    public User getUser(){
        return this.user;
    }

    public void setUser(User user){
        // check user possibility
        this.user = user;
    }

    public String getName(){
        return this.name;
    }

    public void setName(String name){
        this.name = name;
    }

    public String getDescription(){
        return description;
    }

    public void setDescription(String description){
        this.description = description;
    }

    public boolean getIsActive(){
        return this.isActive;
    }

    public void toggleActive(){
        this.isActive = !isActive;
    }

    public Instant getCreatedAt(){
        return this.createdAt;
    }


}
