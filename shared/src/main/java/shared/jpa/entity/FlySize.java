package shared.jpa.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "FlySizes")
public class FlySize {
    @Id
    @Column(name = "flyId")
    public Long flyId;

    @Id
    @Column(name = "size")
    public Integer size;

    public Long getFlyId() {
        return flyId;
    }

    public Integer getSize() {
        return size;
    }

    public void setFlyId(Long flyId) {
        this.flyId = flyId;
    }

    public void setSize(Integer size) {
        this.size = size;
    }
}
