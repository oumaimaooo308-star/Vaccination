package com.example.demo.service;

import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import com.example.demo.entity.Centre;
import com.example.demo.entity.Employe;
import com.example.demo.entity.Gestionnaire;
import com.example.demo.entity.VisiteVaccin;
import com.example.demo.repository.CentreRepository;
import com.example.demo.repository.EmployeRepository;
import com.example.demo.repository.UtilisateurRepository;
import com.example.demo.repository.VisiteVaccinRepository;

@Service
public class EmployeService {

    @Autowired
    private EmployeRepository employeRepo;

    @Autowired
    private CentreRepository centreRepo;

    @Autowired
    private VisiteVaccinRepository visiteRepo;

    @Autowired
    private UtilisateurRepository utilisateurRepo;

    private Long getCentreUtilisateurConnecte() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || "anonymousUser".equals(auth.getPrincipal())) {
            return null;
        }

        return utilisateurRepo.findByUsername(auth.getName())
                .map(u -> u instanceof Gestionnaire g ? g.getCentre().getId() : null)
                .orElse(null);

    }

    // Liste tous les employés du centre de l'utilisateur connecté
    public List<Employe> listerParCentreConnecte() {
        Long centre = getCentreUtilisateurConnecte();
        return employeRepo.findByCentreId(centre);
    }

    // recuperer un empl par id
    public Employe findById(Long id) {
        return employeRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Employé introuvable : " + id));
    }

   
    public Map<Long, String> calculerStatusParEmploye(List<Employe> employes) {
        Map<Long, String> result = new HashMap<>();
        for (Employe e : employes) {
            List<VisiteVaccin> visites = visiteRepo.findByEmployeId(e.getId());
            if (visites.isEmpty()) {
                result.put(e.getId(), null);
            } else {
                boolean aVaccine = visites.stream().anyMatch(v -> "VACCINE".equals(v.getEtatVaccination()));
                boolean aEnInstance = visites.stream().anyMatch(v -> "EN_INSTANCE".equals(v.getEtatVaccination()));
                if (aVaccine)
                    result.put(e.getId(), "VACCINE");
                else if (aEnInstance)
                    result.put(e.getId(), "EN_INSTANCE");
                else
                    result.put(e.getId(), "NON_VACCINE");
            }
        }
        return result;
    }

    // Recuperer la derniere visite en cours d'un employe
    public Optional<VisiteVaccin> GetderniereVisite(Long employeId) {
        List<VisiteVaccin> visites = visiteRepo.findByEmployeId(employeId);
        if (visites.isEmpty())
            return Optional.empty();
     
        return visites.stream()
                .sorted(Comparator.comparing(v -> prioriteStatut(v.getEtatVaccination()))).findFirst();
    }

    private int prioriteStatut(String statut) {
        if ("EN_INSTANCE".equals(statut))
            return 0;
        if ("VACCINE".equals(statut))
            return 1;
        return 2;
    }

    // compter les employes par statuts
    public int compterParStatut(Map<Long, String> statutMap, String statut) {
        return (int) statutMap.values().stream().filter(s -> statut.equals(s)).count();
    }

    // filtrer les valurs null/blank ,pas de case vise dans la vue
    public List<String> listerDirections(Long centreId) {
        return employeRepo.findByCentreId(centreId)
                .stream()
                .map(Employe::getDirection)
                .filter(d -> d != null && !d.isBlank())
                .distinct()
                .sorted()
                .collect(Collectors.toList());

    }

    public Optional<VisiteVaccin> getVisiteParCampagne(Long employeId, Long campagneId) {
        return visiteRepo.findByEmployeIdAndCampagneId(employeId, campagneId);
    }

    public Map<Long, String> calculerStatusParCampagne(List<Employe> employes, Long campagneId) {
        Map<Long, String> status = new HashMap<>();
        for (Employe e : employes) {
            visiteRepo.findByEmployeIdAndCampagneId(e.getId(), campagneId)
                    .ifPresentOrElse(
                            v -> status.put(e.getId(), v.getEtatVaccination()),
                            () -> status.put(e.getId(), "NON_AFFECTE"));
        }
        return status;
    }

    public List<Employe> listerTous() {
        return employeRepo.findAll();
    }

 

public List<Employe> listerParCentre(Long centreId) {
    return employeRepo.findByCentreId(centreId);
}


public Map<Long, String> calculerStatutsParCampagne(List<Employe> employes, Long campagneId) {
    Map<Long, String> statuts = new HashMap<>();
    for (Employe e : employes) {
        visiteRepo.findByEmployeIdAndCampagneId(e.getId(), campagneId)
                  .ifPresentOrElse(
                      visite -> statuts.put(e.getId(), visite.getEtatVaccination()),
                      ()     -> statuts.put(e.getId(), "AUCUN")
                  );
    }
    return statuts;
}

public Page<Employe> listerParCentre(Long centreId, Pageable pageable) {
    return employeRepo.findByCentreId(centreId, pageable);
}

public Page<Employe> listerTous(Pageable pageable) {
    return employeRepo.findAll(pageable);
}

}
