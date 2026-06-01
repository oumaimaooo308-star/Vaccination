package com.example.demo.dto;

import java.time.LocalDate;

import lombok.Data;

@Data
public class DoseDTO {
    private Long id;
    private Integer numeroDose;
    private LocalDate dateDose;
    private Long visiteId;
}