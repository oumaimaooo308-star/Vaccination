package com.example.demo.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.example.demo.dto.DoseDTO;
import com.example.demo.dto.VisiteDTO;
import com.example.demo.entity.CompagneVaccin;
import com.example.demo.entity.Dose;
import com.example.demo.entity.VisiteVaccin;
import com.example.demo.repository.CompagneVaccinRepository;
import com.example.demo.repository.DoseRepository;
import com.example.demo.repository.EmployeRepository;
import com.example.demo.repository.VisiteVaccinRepository;

@Service
public class VisiteService {
    @Autowired
    private VisiteVaccinRepository visiteRepo;


    @Autowired
    private CompagneVaccinRepository campagneRepo;

    @Autowired
    private DoseRepository doseRepo;

    // lister les visites d'une compagne
    public List<VisiteVaccin> listerParCampagne(Long campagneId) {
        return visiteRepo.findByCampagneId(campagneId);
    }

    // Recuperer un visite par id
    public VisiteVaccin findById(Long id) {
        return visiteRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Visite introuvable"));
    }

    public CompagneVaccin getCampagne(Long campagneId) {
        return campagneRepo.findById(campagneId)
                .orElseThrow(() -> new RuntimeException("Campagne introuvable : " + campagneId));
    }

    // enregistrement des consentement
    // refuse-> etatV:non vaccine
    // accepte-> reste en instance
    public VisiteVaccin enregistrerConsentement(Long visiteId, VisiteDTO dto) {
        // recuperer la visite de la base
        VisiteVaccin visite = visiteRepo.findById(visiteId)
                .orElseThrow(() -> new RuntimeException("Visite introuvable"));

        visite.setEtatConsentement(dto.getEtatConsentement());
        visite.setDateDecision(dto.getDateDecision());

        if ("REFUSE".equalsIgnoreCase(dto.getEtatConsentement())) {
            visite.setEtatVaccination("NON_VACCINE");
        } else if ("ACCEPTE".equalsIgnoreCase(dto.getEtatConsentement())) {
            visite.setEtatVaccination("NON_VACCINE");
        }
        return visiteRepo.save(visite);
    }

   

    // marquer le collaborateur non vaccine dans le cas de consentement accepte mais
    // le med decide de ne pas vaccine
    // Contre-indication medical etc ....
    public VisiteVaccin nonVacciner(Long visiteId) {
        VisiteVaccin visite = visiteRepo.findById(visiteId)
                .orElseThrow(() -> new RuntimeException("Visite introuvable"));
        visite.setEtatVaccination("NON_VACCINE");

        if (visite.getDateDecision() == null) {
            visite.setDateDecision(java.time.LocalDate.now());
        }
        return visiteRepo.save(visite);
    }

    // Ajouter dose à une visite
    // max 3 doses -- doit etre vacciné pour jouter une dose
    //
    // REMPLACER la méthode vacciner() par ceci :
    public void ajouterDose(Long visiteId, DoseDTO dto) {
        VisiteVaccin visite = findById(visiteId);

        // Vérifier consentement
        String consentement = visite.getEtatConsentement();
        if (consentement == null || !consentement.strip().equalsIgnoreCase("ACCEPTE"))
            throw new IllegalStateException("Consentement non accepté");

        long nbExistantes = doseRepo.countByVisiteVaccinId(visiteId);
        int nbMax = visite.getCampagne().getNbDose();

        if (nbExistantes >= nbMax)
            throw new IllegalStateException("Nombre maximum de doses atteint (" + nbMax + ")");

        // Créer la dose
        Dose dose = new Dose();
        dose.setVisiteVaccin(visite);
        dose.setNumeroDose((int) nbExistantes + 1);
        dose.setDateDose(dto.getDateDose());
        doseRepo.save(dose);

        // Si toutes les doses sont enregistrées → VACCINE automatiquement
        if (nbExistantes + 1 >= nbMax) {
            visite.setEtatVaccination("VACCINE");
            visiteRepo.save(visite);
        }
        // Sinon on reste EN_INSTANCE jusqu'à la dernière dose
    }

    // Supprimer un dose par id
    public void supprimerDose(Long doseId) {
        if (!doseRepo.existsById(doseId)) {
            throw new RuntimeException("Dose introuvable : " + doseId);
        }
        doseRepo.deleteById(doseId);
    }

    public void supprimerDesCampagne(Long visiteId) {
        VisiteVaccin visite = findById(visiteId);
        doseRepo.deleteAll(visite.getDoses());
        visiteRepo.delete(visite);
    }

    public Page<VisiteVaccin> listerParCampagne(Long campagneId, Pageable pageable){
        return visiteRepo.findByCampagneId(campagneId, pageable);
    }

}
