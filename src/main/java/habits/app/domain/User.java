package habits.app.domain;
import jakarta.persistence.*;
import java.time.Instant;

@Entity()
@Table(name="users")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String email;

    private String timezone;

    private Instant createdAt = Instant.now();

    public User() {
    }

    public User(String email, String timezone){
        this.email = email;
        this.timezone = timezone;
        this.createdAt = Instant.now();
    }

    public Long getId(){
        return this.id;
    }

    public String getEmail(){
        return this.email;
    }

    public void setEmail(String email){
        this.email = email;
    }

    public String getTimezone(){
        return this.timezone;
    }

    public void setTimezone(String timezone){
        this.timezone = timezone;
    }
}
