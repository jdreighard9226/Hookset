package edu.carroll.cs341.hookset.importers.jpa.entity;

import jakarta.persistence.*;

@Entity
public class Fish {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "Fish_ID")
    private int fishId;

    @Column(name = "FWP_Species_ID")
    private int fwpSpeciesId;

    @Column(name = "Fish_Family")
    private String fishFamily;

    @Column(name = "Fish_Species")
    private String fishSpecies;

    


}
