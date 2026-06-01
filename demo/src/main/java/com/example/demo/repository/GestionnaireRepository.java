package com.example.demo.repository;

import com.example.demo.entity.Gestionnaire;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface GestionnaireRepository extends JpaRepository<Gestionnaire, Long> {
    
    Optional<Gestionnaire> findByUsername(String username);
}