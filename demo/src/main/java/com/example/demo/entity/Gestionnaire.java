package com.example.demo.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "gestionnaire")
@DiscriminatorValue("Gestionnaire")  
@Getter
@Setter
public class Gestionnaire extends Utilisateur {

    @ManyToOne
    @JoinColumn(name = "centre_id")
    private Centre centre;
}