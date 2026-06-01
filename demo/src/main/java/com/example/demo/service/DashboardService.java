package com.example.demo.service;

import com.example.demo.entity.CompagneVaccin;
import com.example.demo.entity.Employe;
import com.example.demo.entity.Gestionnaire;
import com.example.demo.entity.Utilisateur;
import com.example.demo.entity.VisiteVaccin;
import com.example.demo.repository.CompagneVaccinRepository;
import com.example.demo.repository.EmployeRepository;
import com.example.demo.repository.UtilisateurRepository;
import com.example.demo.repository.VisiteVaccinRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class DashboardService {

    @Autowired
    private UtilisateurRepository utilisateurRepo;

    @Autowired
    private EmployeRepository employeRepo;

    @Autowired
    private CompagneVaccinRepository campagneRepo;

    @Autowired
    private VisiteVaccinRepository visiteRepo;

    private Long getCentreIdConnecte() {
        // if gestionnaire connecté
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || "anonymousUser".equals(auth.getPrincipal())) {
            return null;
        }
       
        return utilisateurRepo.findByUsername(auth.getName())
                .map(u -> u instanceof Gestionnaire g ? g.getCentre().getId() : null)
                .orElse(null);
    }

    // Nombre total d'employés dans le centre
    public int totalEmployes() {
        Long centreId = getCentreIdConnecte();
        if (centreId == null) {
            return (int) employeRepo.count();
        }
        return employeRepo.findByCentreId(centreId).size();
    }

    public long nbVaccines() {
        Long centreId = getCentreIdConnecte();
        List<Employe> employes = centreId != null ? employeRepo.findByCentreId(centreId) : employeRepo.findAll();
        return employes.stream()
                .flatMap(e -> visiteRepo.findByEmployeId(e.getId()).stream())
                .filter(v -> "VACCINE".equals(v.getEtatVaccination()))
                .count();
    }

    // Nombre d'employés encore EN_INSTANCE (à convoquer)
    public long nbEnInstance() {
        Long centreId = getCentreIdConnecte();
        List<Employe> employes = centreId != null ? employeRepo.findByCentreId(centreId) : employeRepo.findAll();
        return employes.stream()
                .flatMap(e -> visiteRepo.findByEmployeId(e.getId()).stream())
                .filter(v -> "EN_INSTANCE".equals(v.getEtatVaccination()))
                .count();
    }

    // Nombre d'employés non vaccinés (refus ou décision méd.)
    public long nbNonVaccines() {
        Long centreId = getCentreIdConnecte();
        List<Employe> employes = centreId != null
                ? employeRepo.findByCentreId(centreId)
                : employeRepo.findAll();
        return employes.stream()
                .flatMap(e -> visiteRepo.findByEmployeId(e.getId()).stream())
                .filter(v -> "NON_VACCINE".equals(v.getEtatVaccination()))
                .count();
    }

    // Nombre de campagnes actives dans le centre
    public int nbCampagnes() {
        Long centreId = getCentreIdConnecte();
        if (centreId == null)
            return (int) campagneRepo.count();
        return campagneRepo.findByCentreId(centreId).size();
    }

    // Les 5 dernières visites EN_INSTANCE du centre
    public List<VisiteVaccin> dernieresVisitesEnInstance() {
        Long centreId = getCentreIdConnecte();
        List<Employe> employes = centreId != null
                ? employeRepo.findByCentreId(centreId)
                : employeRepo.findAll();

        return employes.stream()
                .flatMap(e -> visiteRepo.findByEmployeId(e.getId()).stream())
                .filter(v -> "EN_INSTANCE".equals(v.getEtatVaccination()))
                // On limite à 5 pour le dashboard
                .limit(5)
                .collect(java.util.stream.Collectors.toList());
    }

    // pour le select
    public List<CompagneVaccin> listerCampagnes() {
        Long centreId = getCentreIdConnecte();
        if (centreId != null) {
            return campagneRepo.findByCentreId(centreId);
        }
        return campagneRepo.findAll();
    }

    public long totalEmployesParCampagne(Long campagneId) {
        return visiteRepo.countByCampagneId(campagneId);
    }

    public long nbVaccinesParCampagne(Long campagneId) {
        return visiteRepo.countByCampagneIdAndEtatVaccination(campagneId, "VACCINE");
    }

    public long nbEnInstanceParCampagne(Long campagneId) {
        return visiteRepo.countByCampagneIdAndEtatVaccination(campagneId, "EN_INSTANCE");
    }

    public long nbNonVaccinesParCampagne(Long campagneId) {
        return visiteRepo.countByCampagneIdAndEtatVaccination(campagneId, "NON_VACCINE");
    }

    public List<VisiteVaccin> dernieresVisitesEnInstanceParCampagne(Long campagneId) {
        return visiteRepo.findTop5ByCampagneIdAndEtatVaccination(campagneId, "EN_INSTANCE");
    }

    public List<VisiteVaccin> dernieresDoses() {
        Long centreId = getCentreIdConnecte();
        List<Employe> employes = centreId != null
                ? employeRepo.findByCentreId(centreId)
                : employeRepo.findAll();
        return employes.stream()
                .flatMap(e -> visiteRepo.findByEmployeId(e.getId()).stream())
                .filter(v -> v.getDoses() != null && !v.getDoses().isEmpty())
                .sorted((a, b) -> {
                    java.time.LocalDate da = a.getDoses().get(a.getDoses().size() - 1).getDateDose();
                    java.time.LocalDate db = b.getDoses().get(b.getDoses().size() - 1).getDateDose();
                    return db.compareTo(da);
                })
                .limit(5)
                .collect(java.util.stream.Collectors.toList());
    }

    public List<VisiteVaccin> dernieresDosesParCampagne(Long campagneId) {
        return visiteRepo.findTop5ByCampagneIdAndDosesNotEmptyOrderByDosesDateDoseDesc(campagneId);
    }

    // ── Stats par centre (pour Gestionnaire) ──

    public long totalEmployesParCentre(Long centreId) {
        return employeRepo.findByCentreId(centreId).size();
    }

    public long nbVaccinesParCentre(Long centreId) {
        return employeRepo.findByCentreId(centreId).stream()
                .flatMap(e -> visiteRepo.findByEmployeId(e.getId()).stream())
                .filter(v -> "VACCINE".equals(v.getEtatVaccination()))
                .count();
    }

    public long nbEnInstanceParCentre(Long centreId) {
        return employeRepo.findByCentreId(centreId).stream()
                .flatMap(e -> visiteRepo.findByEmployeId(e.getId()).stream())
                .filter(v -> "EN_INSTANCE".equals(v.getEtatVaccination()))
                .count();
    }

    public long nbNonVaccinesParCentre(Long centreId) {
        return employeRepo.findByCentreId(centreId).stream()
                .flatMap(e -> visiteRepo.findByEmployeId(e.getId()).stream())
                .filter(v -> "NON_VACCINE".equals(v.getEtatVaccination()))
                .count();
    }

    public List<VisiteVaccin> dernieresVisitesEnInstanceParCentre(Long centreId) {
        return employeRepo.findByCentreId(centreId).stream()
                .flatMap(e -> visiteRepo.findByEmployeId(e.getId()).stream())
                .filter(v -> "EN_INSTANCE".equals(v.getEtatVaccination()))
                .limit(5)
                .collect(java.util.stream.Collectors.toList());
    }

    public List<VisiteVaccin> dernieresDosesParCentre(Long centreId) {
        return employeRepo.findByCentreId(centreId).stream()
                .flatMap(e -> visiteRepo.findByEmployeId(e.getId()).stream())
                .filter(v -> v.getDoses() != null && !v.getDoses().isEmpty())
                .sorted((a, b) -> {
                    java.time.LocalDate da = a.getDoses()
                            .get(a.getDoses().size() - 1).getDateDose();
                    java.time.LocalDate db = b.getDoses()
                            .get(b.getDoses().size() - 1).getDateDose();
                    return db.compareTo(da); // tri décroissant
                })
                .limit(5)
                .collect(java.util.stream.Collectors.toList());
    }

    // ── Refus (consentement REFUSE) ──
    public long nbRefus() {
        Long centreId = getCentreIdConnecte();
        List<Employe> employes = centreId != null
                ? employeRepo.findByCentreId(centreId)
                : employeRepo.findAll();
        return employes.stream()
                .flatMap(e -> visiteRepo.findByEmployeId(e.getId()).stream())
                .filter(v -> "REFUSE".equalsIgnoreCase(v.getEtatConsentement()))
                .count();
    }

    public long nbRefusParCentre(Long centreId) {
        return employeRepo.findByCentreId(centreId).stream()
                .flatMap(e -> visiteRepo.findByEmployeId(e.getId()).stream())
                .filter(v -> "REFUSE".equalsIgnoreCase(v.getEtatConsentement()))
                .count();
    }

    public long nbRefusParCampagne(Long campagneId) {
        return visiteRepo.findByCampagneId(campagneId).stream()
                .filter(v -> "REFUSE".equalsIgnoreCase(v.getEtatConsentement()))
                .count();
    }

    // ── En cours (ACCEPTE + pas encore VACCINE) ──
    public long nbEnCours() {
        Long centreId = getCentreIdConnecte();
        List<Employe> employes = centreId != null
                ? employeRepo.findByCentreId(centreId)
                : employeRepo.findAll();
        return employes.stream()
                .flatMap(e -> visiteRepo.findByEmployeId(e.getId()).stream())
                .filter(v -> "ACCEPTE".equalsIgnoreCase(v.getEtatConsentement())
                        && !"VACCINE".equals(v.getEtatVaccination()))
                .count();
    }

    public long nbEnCoursParCentre(Long centreId) {
        return employeRepo.findByCentreId(centreId).stream()
                .flatMap(e -> visiteRepo.findByEmployeId(e.getId()).stream())
                .filter(v -> "ACCEPTE".equalsIgnoreCase(v.getEtatConsentement())
                        && !"VACCINE".equals(v.getEtatVaccination()))
                .count();
    }

    public long nbEnCoursParCampagne(Long campagneId) {
        return visiteRepo.findByCampagneId(campagneId).stream()
                .filter(v -> "ACCEPTE".equalsIgnoreCase(v.getEtatConsentement())
                        && !"VACCINE".equals(v.getEtatVaccination()))
                .count();
    }

}