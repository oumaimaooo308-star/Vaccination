package com.example.demo.entity;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "visite_vaccin")
@Setter
@Getter
public class VisiteVaccin {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "employe_id")
    private Employe employe;

    @ManyToOne
    @JoinColumn(name = "campagne_id")
    private CompagneVaccin campagne;

    private String etatConsentement;
    private LocalDate dateDecision;
    private String etatVaccination;

    @OneToMany(mappedBy = "visiteVaccin", cascade = CascadeType.ALL, orphanRemoval = true)
    @Size(max = 3)
    private List<Dose> doses = new ArrayList<>();

}