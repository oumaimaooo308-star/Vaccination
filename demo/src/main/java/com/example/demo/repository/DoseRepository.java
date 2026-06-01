package com.example.demo.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.demo.entity.Dose;

@Repository
public interface DoseRepository extends JpaRepository<Dose,Long> {
    List<Dose> findByVisiteVaccinIdOrderByNumeroDoseAsc(Long visiteId);//tous les doses d'une visite ordonées par numéro
    long countByVisiteVaccinId(Long visiteId); //compter les nbres de doses enregistrées pour une visite
}
