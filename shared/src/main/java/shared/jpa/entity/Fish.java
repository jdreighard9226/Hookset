package shared.jpa.entity;

import jakarta.persistence.*;

@Entity
public class Fish {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "fishId")
    private Integer fishId;

    @Column(name = "fwpSpeciesId")
    private Integer fwpSpeciesId;

    @Column(name = "fishFamily")
    private String fishFamily;

    @Column(name = "fishSpecies")
    private String fishSpecies;

    public Integer getFishId() {
        return fishId;
    }

    public Integer getFwpSpeciesId() {
        return fwpSpeciesId;
    }

    public String getFishFamily() {
        return fishFamily;
    }

    public String getFishSpecies() {
        return fishSpecies;
    }

    public void setFwpSpeciesId(Integer fwpSpeciesId) {
        this.fwpSpeciesId = fwpSpeciesId;
    }

    public void setFishFamily(String fishFamily) {
        this.fishFamily = fishFamily;
    }

    public void setFishSpecies(String fishSpecies) {
        this.fishSpecies = fishSpecies;
    }
}
