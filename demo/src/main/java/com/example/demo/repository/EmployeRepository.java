package com.example.demo.repository;

import java.util.List;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Page;
import org.springframework.data.jpa.repository.JpaRepository;

import com.example.demo.entity.Employe;

public interface EmployeRepository extends JpaRepository<Employe, Long> {
    List<Employe> findByCentreId(Long centreId);

    boolean existsByMatricule(String matricule); // verifier si le matricule existe deja

    Page<Employe> findByCentreId(Long centreId,Pageable pageable);

    Page<Employe> findAll(Pageable pageable);
}
