package com.example.demo.entity;

import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "centre")
@Getter
@Setter
public class Centre {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String ville;

    @OneToMany(mappedBy = "centre")
    private List<Employe> employes=new ArrayList<>();
    
    @OneToMany(mappedBy = "centre",cascade = CascadeType.ALL,orphanRemoval = true)
    private List<CompagneVaccin> compagnes=new ArrayList<>();


}
