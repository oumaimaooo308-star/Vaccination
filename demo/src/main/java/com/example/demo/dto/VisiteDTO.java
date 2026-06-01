package com.example.demo.dto;

import java.time.LocalDate;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class VisiteDTO {
    private Long id;
    private Long employeId;
    private Long campagneId;
    private String etatConsentement;  // "ACCEPTE" ou "REFUSE"
    private LocalDate dateDecision;
    private String etatVaccination; //'en instance ' , 'Vaccine' ,'Non vaccine'
}
