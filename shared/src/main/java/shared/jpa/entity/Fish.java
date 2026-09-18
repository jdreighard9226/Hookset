package shared.jpa.entity;

import jakarta.persistence.*;

@Entity
public class Fish {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "fishId")
    private Integer fishId;

    @Column(name = "fwpSpeciesId")
    private String fwpSpeciesId;

    @Column(name = "fishFamily")
    private String fishFamily;

    @Column(name = "fishSpecies")
    private String fishSpecies;

    public Integer getFishId() {
        return fishId;
    }

    public String getFwpSpeciesId() {
        return fwpSpeciesId;
    }

    public String getFishFamily() {
        return fishFamily;
    }

    public String getFishSpecies() {
        return fishSpecies;
    }

    public void setFwpSpeciesId(String fwpSpeciesId) {
        this.fwpSpeciesId = fwpSpeciesId;
    }

    public void setFishFamily(String fishFamily) {
        this.fishFamily = fishFamily;
    }

    public void setFishSpecies(String fishSpecies) {
        this.fishSpecies = fishSpecies;
    }
}
