package com.example.demo.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.demo.entity.CompagneVaccin;

@Repository
public interface CompagneVaccinRepository extends JpaRepository<CompagneVaccin,Long> {
    List<CompagneVaccin> findByCentreId(Long centreId);// toutesles campagnes d'un centre
}
