package com.example.demo.dto;

import java.time.LocalDate;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class EmployeDTO {
    private Long id;
    private String matricule;
    private String nom;
    private String prenom;
    private LocalDate dateNaissance;
    private LocalDate dateRecrutement;
    private String categorieVF;
    private String sexe;
    private String activite;
    private String posteTravaille;
    private String telephone;
    private String direction;
    private Long centreId; //j'en ai besoin qu'a l'id pour faire la liaison dans une formulaire thymeleaf pour le selectionner via une liste deroulante

    
}