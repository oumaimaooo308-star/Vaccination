package com.example.demo.entity;

import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "consultant")
@DiscriminatorValue("Consultant")
@Getter
@Setter
public class Consultant extends Utilisateur {
    
}
