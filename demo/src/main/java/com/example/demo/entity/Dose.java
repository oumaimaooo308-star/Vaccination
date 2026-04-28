package com.example.demo.entity;

import java.time.LocalDate;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinColumns;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "dose")
@Getter
@Setter
public class Dose {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Integer numeroDose;

    @Column(nullable = false)
    private LocalDate dateDose;

    @ManyToOne // plusieurs dose appartient a une seule visiste
    @JoinColumns({
        @JoinColumn(name = "employe_id", referencedColumnName = "employe_id"),
        @JoinColumn(name = "campagne_id", referencedColumnName = "campagne_id")
    })
    private VisiteVaccin visiteVaccin;

}
