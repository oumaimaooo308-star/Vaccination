package com.example.demo.entity;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;


import jakarta.persistence.CascadeType;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MapsId;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;


@Entity
@Table(name="visite_vaccin")
@Setter 
@Getter
public class VisiteVaccin {

    @EmbeddedId
    private VisiteVaccinId id;

    @ManyToOne
    @MapsId("employeId")
    @JoinColumn(name = "employe_id")
    private Employe employe;

    @ManyToOne
    @MapsId("campagneId")
    @JoinColumn(name = "campagne_id") 
    private CompagneVaccin campagne;

    private String etatConsentement;
    private LocalDate dateDecision;
    private String etatVaccination;

    @OneToMany(mappedBy = "visiteVaccin",cascade = CascadeType.ALL,orphanRemoval = true)
    @Size(max = 3)
    private List<Dose> doses=new ArrayList<>();

    


}
