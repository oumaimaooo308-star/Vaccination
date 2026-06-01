package com.example.demo.dto;

import java.time.LocalDate;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;
@Setter
@Getter
@Data
public class CompagneDTO {
    private Long id;
    private String nomVaccin;
    private String description;
    private LocalDate dateCompagne;
    private Long centreId;
    private Integer nbDoses;

   
}
