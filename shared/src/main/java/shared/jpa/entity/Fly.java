package shared.jpa.entity;

import jakarta.persistence.*;

import java.time.Instant;


@Entity
@Table (name = "Flies")
public class Fly {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "flyId")
    private Long flyId;

    @Column(name = "userId")
    private Long userId;

    @Column(name = "flyType")
    private String flyType;

    @Column(name = "color")
    private String color;

    @Column(name = "dateEntered")
    private Instant dateEntered;

    @Column(name = "LastDateModified")
    private Instant lastDateModified;

    public Long getFlyId() {
        return flyId;
    }

    public Long getUserId() {
        return userId;
    }

    public String getFlyType() {
        return flyType;
    }

    public String getColor() {
        return color;
    }

    public Instant getDateEntered() {
        return dateEntered;
    }

    public Instant getLastDateModified() {
        return lastDateModified;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public void setFlyType(String flyType) {
        this.flyType = flyType;
    }

    public void setColor(String color) {
        this.color = color;
    }

    public void setDateEntered(Instant dateEntered) {
        this.dateEntered = dateEntered;
    }

    public void setLastDateModified(Instant dateModified) {
        this.lastDateModified = dateModified;
    }
}
