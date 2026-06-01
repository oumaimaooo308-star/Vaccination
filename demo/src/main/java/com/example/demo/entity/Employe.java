package com.example.demo.entity;

import java.time.LocalDate;
import java.time.Period;
import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "employe")
@Getter
@Setter
public class Employe {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String matricule;

    @Column(nullable = false)
    private String nom;

    @Column(nullable = false)
    private String prenom;

    @Column(nullable = false)
    private LocalDate dateNaissance;

    @Column(nullable = false)
    private LocalDate dateRecrutement;

    @Column(nullable = false)
    private String categorieVF;

    @Column(nullable = false)
    private String sexe;

    @Column(nullable = false)
    private String activite;

    @Column(nullable = false)
    private String posteTravaille;

    @Column(nullable = false)
    private String telephone;

    @Column(nullable = false)
    private String direction;

    @ManyToOne
    @JoinColumn(name = "centre_id", nullable = false)
    private Centre centre;

    @OneToMany(mappedBy = "employe", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<VisiteVaccin> visites = new ArrayList<>();

    @Transient
    public int getAge() {
        if (dateNaissance == null)
            return 0;
        return Period.between(dateNaissance, LocalDate.now()).getYears();
    }

    @Transient
    public LocalDate getDateRetraite() {
        if (dateNaissance == null)
            return null;
        return dateNaissance.plusYears(63);
    }

    @Transient
    public int getAnciennete() {
        if (dateRecrutement == null)
            return 0;
        return Period.between(dateRecrutement, LocalDate.now()).getYears();
    }

    @Transient
    public String getNomComplet() {
        return nom + " " + prenom;
    }

    @Transient
    public String getInitiales() {
        String initNom = (nom != null && !nom.isEmpty()) ? String.valueOf(nom.charAt(0)) : "";
        String initPrenom = (prenom != null && !prenom.isEmpty()) ? String.valueOf(prenom.charAt(0)) : "";
        return (initNom + initPrenom).toUpperCase();
    }

    @Transient
    public String getTypeDC() {
        if (categorieVF == null)
            return "—";
        return categorieVF.startsWith("VF-A") ? "DC" : "DP";
    }

}