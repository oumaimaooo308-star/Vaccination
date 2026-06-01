package com.example.demo.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.example.demo.entity.VisiteVaccin;
import java.util.List;
import java.util.Optional;

@Repository
public interface VisiteVaccinRepository extends JpaRepository<VisiteVaccin, Long> {
    List<VisiteVaccin> findByCampagneId(Long campagneId); // les visites d'une compagne pour les collaborateurs

    List<VisiteVaccin> findByEmployeId(Long employeId);// toutes les visites d'un emp avec les campagnes confondues

    Optional<VisiteVaccin> findByEmployeIdAndCampagneId(Long employeId, Long CampagneId); // la visite d'un employe dans
                                                                                          // une campagne precise

    long countByCampagneIdAndEtatVaccination(Long campagneId, String etatVaccination); // combien d'employe sont encore
                                                                                       // en instance dans une campagne

    long countByCampagneId(Long campagneId);

    List<VisiteVaccin> findTop5ByCampagneIdAndEtatVaccination(Long campagneId, String etat);

    @Query("""
            SELECT DISTINCT v FROM VisiteVaccin v
            JOIN v.doses d
            WHERE SIZE(v.doses) > 0
            ORDER BY d.dateDose DESC
            LIMIT 5
            """)
    List<VisiteVaccin> findTop5ByDosesNotEmptyOrderByDosesDateDoseDesc();

    @Query("""
            SELECT DISTINCT v FROM VisiteVaccin v
            JOIN v.doses d
            WHERE v.campagne.id = :campagneId
            AND SIZE(v.doses) > 0
            ORDER BY d.dateDose DESC
            LIMIT 5
            """)
    List<VisiteVaccin> findTop5ByCampagneIdAndDosesNotEmptyOrderByDosesDateDoseDesc(
            @Param("campagneId") Long campagneId);

     Page<VisiteVaccin> findByCampagneId(Long campagneId, Pageable pageable);
}
