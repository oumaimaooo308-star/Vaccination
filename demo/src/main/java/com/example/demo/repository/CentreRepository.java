package com.example.demo.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.demo.entity.Centre;

@Repository
public interface CentreRepository extends JpaRepository<Centre,Long> {

    
}
