package com.example.demo.service;

import com.example.demo.repository.GestionnaireRepository;
import com.example.demo.repository.UtilisateurRepository;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import com.example.demo.controller.CampagneController;
import com.example.demo.dto.CompagneDTO;
import com.example.demo.entity.Centre;
import com.example.demo.entity.CompagneVaccin;
import com.example.demo.entity.Employe;
import com.example.demo.entity.Gestionnaire;
import com.example.demo.entity.Utilisateur;
import com.example.demo.entity.VisiteVaccin;
import com.example.demo.repository.CentreRepository;
import com.example.demo.repository.CompagneVaccinRepository;
import com.example.demo.repository.EmployeRepository;
import com.example.demo.repository.VisiteVaccinRepository;

@Service
public class CampagneService {
    private final GestionnaireRepository gestionnaireRepository;

    @Autowired
    private CompagneVaccinRepository campagneRepo;

    @Autowired
    private CentreRepository centreRepo;

    @Autowired
    private EmployeRepository employeRepo;

    @Autowired
    private VisiteVaccinRepository visiteRepo;

    @Autowired
    private UtilisateurRepository utilisateurRepo;

    CampagneService(GestionnaireRepository gestionnaireRepository) {
        this.gestionnaireRepository = gestionnaireRepository;
    }

    public Long getCentreIdConnecte() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || "anonymousUser".equals(auth.getPrincipal())) {
            throw new RuntimeException("Aucun utilisateur connecté");
        }
        Utilisateur u = utilisateurRepo.findByUsername(auth.getName())
                .orElseThrow(() -> new RuntimeException("Utilisateur introuvable"));

        // Gestionnaire → son propre centre
        if (u instanceof Gestionnaire g) {
            return g.getCentre().getId();
        }
        // Consultant → pas de centre fixe, on retourne null
        return null;
    }

    // lister tous les compagnes d'un centre
    public List<CompagneVaccin> listerParCentre(Long centreId) {
        return campagneRepo.findByCentreId(centreId);
    }

    // Recuperer le centre du gestionnaire connecte
    public Long getCentreIdCourant() {
        return gestionnaireRepository.findById(1L)
                .orElseThrow(() -> new RuntimeException("Gestionnaire introuvable"))
                .getCentre()
                .getId();

    }

    // creer la compagne :
    public void creer(CompagneDTO dto, List<String> directionsSelectionnees, MultipartFile fichierImport) {
        Long centreId = getCentreIdConnecte();
        Centre centre = centreRepo.findById(centreId)
                .orElseThrow(() -> new RuntimeException("Centre intouvable"));

        CompagneVaccin campagne = new CompagneVaccin();
        campagne.setNomVaccin(dto.getNomVaccin());
        campagne.setDescription(dto.getDescription());
        campagne.setDateCompagne(dto.getDateCompagne());
        campagne.setNbDose(dto.getNbDoses());
        campagne.setCentre(centre);
        campagneRepo.save(campagne);

        if (directionsSelectionnees != null && !directionsSelectionnees.isEmpty()) {
            affecterParDirections(campagne, directionsSelectionnees);
        }

        if (fichierImport != null && !fichierImport.isEmpty()) {
            affecterParFichier(campagne, fichierImport);
        }
    }

    // ajouter les employes a la compagne
    public void ajouterEmployes(Long campagneId, List<Long> employeIds, List<String> directionsSelectionnees,
            MultipartFile fichierImport) {
        CompagneVaccin campagne = campagneRepo.findById(campagneId)
                .orElseThrow(() -> new RuntimeException("Campagne introuvable"));

        if (employeIds != null) {
            for (Long employeId : employeIds) {
                Employe employe = employeRepo.findById(employeId)
                        .orElseThrow(() -> new RuntimeException("Employe introuvable"));
                creerVisiteSiAbsente(campagne, employe);

                // verifier si l'employe existe deja dans la compagne:
                boolean dejaPresent = visiteRepo
                        .findByEmployeIdAndCampagneId(employeId, campagneId)
                        .isPresent();
                if (!dejaPresent) {
                    VisiteVaccin visite = new VisiteVaccin();
                    visite.setEmploye(employe);
                    visite.setCampagne(campagne);
                    visite.setEtatVaccination("EN_INSTANCE");
                    visiteRepo.save(visite);
                }
            }
        }
        // traitement du ficchier importé
        if (fichierImport != null && !fichierImport.isEmpty()) {
            affecterParFichier(campagne, fichierImport);
        }
        if (directionsSelectionnees != null && !directionsSelectionnees.isEmpty()) {
            affecterParDirections(campagne, directionsSelectionnees);
        }

    }

    // Recuperer la compagne par son id
    public CompagneVaccin findById(Long id) {
        return campagneRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Compagne introuvable"));

    }

    // supprimer une compagne
    public void supprimer(Long id) {
        campagneRepo.deleteById(id);
    }

    // pour pre cocher et désactiver les cases des employés déjà présents
    public Set<Long> getIdsDejaAffectes(Long campagneId) {
        return visiteRepo.findByCampagneId(campagneId)
                .stream()
                .map(v -> v.getEmploye().getId())
                .collect(Collectors.toSet());
    }

    // retourner la liste distincte et triee des valeurs du champs dorection
    public List<String> listerDirections(Long centreId) {
        return employeRepo.findByCentreId(centreId)
                .stream()
                .map(Employe::getDirection)
                .filter(d -> d != null && !d.isBlank())
                .distinct()
                .sorted()
                .collect(Collectors.toList());
    }

    // Trouve tous les employés du centre dont le champ `direction`
    // est dans la liste cochée dans le modal.
    // Délègue à creerVisiteSiAbsente() pour le doublon-check.
    private void affecterParDirections(CompagneVaccin campagne, List<String> directions) {
        Long centreId = campagne.getCentre().getId();
        employeRepo.findByCentreId(centreId).stream()
                .filter(e -> e.getDirection() != null && directions.contains(e.getDirection()))
                .forEach(e -> creerVisiteSiAbsente(campagne, e));
    }

    private void affecterParFichier(CompagneVaccin campagne, MultipartFile fichier) {
        String nomFichier = fichier.getOriginalFilename();
        if (nomFichier == null)
            return;
        try {
            if (nomFichier.toLowerCase().endsWith(".csv")) {
                affecterDepuisCSV(campagne, fichier);
            } else if (nomFichier.toLowerCase().endsWith(".xlsx")
                    || nomFichier.toLowerCase().endsWith(".xls")) {
                affecterDepuisExcel(campagne, fichier);
            }
        } catch (Exception ex) {
            System.err.println("[CampagneService] Erreur parsing fichier import : " + ex.getMessage());
        }
    }

    private void affecterDepuisCSV(CompagneVaccin campagne, MultipartFile fichier) throws Exception {
        BufferedReader reader = new BufferedReader(
                new InputStreamReader(fichier.getInputStream(), "UTF-8"));

        String entete = reader.readLine();
        if (entete == null) {
            reader.close();
            return;
        }

        String sep = entete.contains(";") ? ";" : ",";
        String[] colonnes = entete.split(sep);

        int idxMatricule = -1;
        for (int i = 0; i < colonnes.length; i++) {
            if (colonnes[i].trim().equalsIgnoreCase("matricule")) {
                idxMatricule = i;
                break;
            }
        }
        if (idxMatricule == -1) {
            reader.close();
            return;
        }

        Long centreId = campagne.getCentre().getId();
        final int idx = idxMatricule;
        String ligne;
        while ((ligne = reader.readLine()) != null) {
            String[] champs = ligne.split(sep);
            if (champs.length > idx) {
                String matricule = champs[idx].trim();
                employeRepo.findByCentreId(centreId).stream()
                        .filter(e -> e.getMatricule().equalsIgnoreCase(matricule))
                        .findFirst()
                        .ifPresent(e -> creerVisiteSiAbsente(campagne, e));
            }
        }
        reader.close();
    }

    /**
     * affecterDepuisExcel(campagne, fichier)
     *
     * Parse le .xlsx avec Apache POI (XSSFWorkbook).
     * Même logique que CSV : première ligne = en-tête, colonne "matricule".
    */
    private void affecterDepuisExcel(CompagneVaccin campagne, MultipartFile fichier) throws Exception {
        Workbook workbook = new XSSFWorkbook(fichier.getInputStream());
        Sheet sheet = workbook.getSheetAt(0);

        Row entete = sheet.getRow(0);
        if (entete == null) {
            workbook.close();
            return;
        }

        int idxMatricule = -1;
        for (Cell cell : entete) {
            if (cell.getStringCellValue().trim().equalsIgnoreCase("matricule")) {
                idxMatricule = cell.getColumnIndex();
                break;
            }
        }
        if (idxMatricule == -1) {
            workbook.close();
            return;
        }

        Long centreId = campagne.getCentre().getId();
        final int idx = idxMatricule;
        for (int i = 1; i <= sheet.getLastRowNum(); i++) {
            Row row = sheet.getRow(i);
            if (row == null)
                continue;
            Cell cellMatricule = row.getCell(idx);
            if (cellMatricule == null)
                continue;
            String matricule = cellMatricule.getStringCellValue().trim();
            employeRepo.findByCentreId(centreId).stream()
                    .filter(e -> e.getMatricule().equalsIgnoreCase(matricule))
                    .findFirst()
                    .ifPresent(e -> creerVisiteSiAbsente(campagne, e));
        }
        workbook.close();
    }

    
    private void creerVisiteSiAbsente(CompagneVaccin campagne, Employe employe) {
        boolean dejaPresent = visiteRepo
                .findByEmployeIdAndCampagneId(employe.getId(), campagne.getId())
                .isPresent();
        if (!dejaPresent) {
            VisiteVaccin visite = new VisiteVaccin();
            visite.setEmploye(employe);
            visite.setCampagne(campagne);
            visite.setEtatVaccination("EN_INSTANCE");
            visiteRepo.save(visite);
        }
    }

    public List<CompagneVaccin> listerToutes() {
        return campagneRepo.findAll();
    }

}
