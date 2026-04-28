package com.example.demo.entity;

import java.time.LocalDate;
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
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "campagne_vaccin")
@Getter
@Setter
public class CompagneVaccin {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String nomVaccin;

    private String description;

    @Column(nullable=false)
    private LocalDate dateCompagne;

    @ManyToOne
    @JoinColumn(name = "centre_id",nullable = false)
    private Centre centre;
    
    @OneToMany(mappedBy="campagne",cascade=CascadeType.ALL)
    private List<VisiteVaccin> visites=new ArrayList<>();
}