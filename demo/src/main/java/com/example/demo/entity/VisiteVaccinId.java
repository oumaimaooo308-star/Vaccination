package com.example.demo.entity;

import java.io.Serializable;
import java.util.Objects;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.Getter;
import lombok.Setter;

@Embeddable
@Getter
@Setter
public class VisiteVaccinId implements Serializable {

    @Column(name = "employe_id")
    private Long employeId;

    @Column(name = "campagne_id")
    private Long campagneId;

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (!(o instanceof VisiteVaccinId))
            return false;
        VisiteVaccinId that = (VisiteVaccinId) o;
        return Objects.equals(employeId, that.employeId) &&
                Objects.equals(campagneId, that.campagneId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(employeId, campagneId);
    }

}