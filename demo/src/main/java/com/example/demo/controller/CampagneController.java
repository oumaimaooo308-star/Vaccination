package com.example.demo.controller;

import com.example.demo.service.EmployeService;

import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import com.example.demo.dto.CompagneDTO;
import com.example.demo.entity.Centre;
import com.example.demo.entity.CompagneVaccin;
import com.example.demo.entity.Employe;
import com.example.demo.entity.Gestionnaire;
import com.example.demo.entity.Utilisateur;
import com.example.demo.entity.VisiteVaccin;
import com.example.demo.repository.CentreRepository;
import com.example.demo.repository.EmployeRepository;
import com.example.demo.repository.UtilisateurRepository;
import com.example.demo.repository.VisiteVaccinRepository;
import com.example.demo.service.CampagneService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

@Controller
@RequestMapping("/campagnes")
public class CampagneController {
    private final EmployeService employeService;

    @Autowired
    private CampagneService campagneService;

    @Autowired
    private CentreRepository centreRepo;

    @Autowired
    private EmployeRepository employeRepo;

    @Autowired
    private VisiteVaccinRepository visiteRepo;

    @Autowired
    private UtilisateurRepository utilisateurRepo;

    CampagneController(EmployeService employeService) {
        this.employeService = employeService;
    }

    // recuperer le centre de l'utilisateur conecté
    private Long getCentreIdConnecte() {
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

    @GetMapping
    public String liste(Model model) {
        Long centreId = getCentreIdConnecte();

        // Consultant (centreId == null) : voit toutes les campagnes
        // Gestionnaire : voit seulement les campagnes de son centre
        List<CompagneVaccin> campagnes = (centreId != null)
                ? campagneService.listerParCentre(centreId)
                : campagneService.listerToutes();

        model.addAttribute("campagnes", campagnes);
        model.addAttribute("directions",
                centreId != null ? employeService.listerDirections(centreId) : List.of());
        model.addAttribute("centreId", centreId);

        if (centreId != null) {
            Centre centre = centreRepo.findById(centreId).orElseThrow();
            model.addAttribute("centreNom", centre.getVille());
        } else {
            model.addAttribute("centreNom", "Tous les centres");
        }

        return "campagnes/liste";
    }

    @GetMapping("/nouveau")
    public String formulaireCreation(Model model) {
        Long centreId = getCentreIdConnecte();
        model.addAttribute("campagneDTO", new CompagneDTO());
        model.addAttribute("centreId", centreId);
        // directions pour les checkboxes
        model.addAttribute("directions",
                centreId != null ? employeService.listerDirections(centreId) : List.of());
        return "campagnes/formulaire";
    }

    @PostMapping("/nouveau") // soumettre le formulaire
    public String creer(@ModelAttribute CompagneDTO compagneDTO,
            @RequestParam(value = "directionsSelectionnees", required = false) List<String> directionsSelectionnees,
            @RequestParam(value = "fichierImport", required = false) MultipartFile fichierImport) {
        // @ModelAttribute = Spring remplit automatiquement le DTO avec les données du formulaire
        campagneService.creer(compagneDTO, directionsSelectionnees, fichierImport);
        return "redirect:/campagnes?success"; // permet d'afficher le msg flash
    }

    @GetMapping("/{id}")
    public String detail(@PathVariable Long id,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "7") int size,
            Model model) {

        CompagneVaccin campagne = campagneService.findById(id);

        // Paginer les visites manuellement
        List<VisiteVaccin> toutesVisites = campagne.getVisites();
        int total = toutesVisites.size();
        int debut = page * size;
        int fin = Math.min(debut + size, total);
        List<VisiteVaccin> visitesPage = (debut < total)
                ? toutesVisites.subList(debut, fin)
                : List.of();

        model.addAttribute("campagne", campagne);
        model.addAttribute("visitesPage", visitesPage);
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", (int) Math.ceil((double) total / size));

        return "campagnes/detail";
    }

    @PostMapping("/{id}/supprimer")
    public String supprimer(@PathVariable Long id) {
        campagneService.supprimer(id);
        return "redirect:/campagnes?deleted";
    }

    @GetMapping("/{id}/ajouter-employes")
    public String ajouterEmployes(@PathVariable Long id, Model model) {
        Long centreId = getCentreIdConnecte();

        if (centreId == null) {
            return "redirect:/campagnes?erreur=accès refusé";
        }
        CompagneVaccin campagne = campagneService.findById(id);

        List<Employe> employes = employeRepo.findByCentreId(centreId);

        List<Long> dejaDansCampagne = visiteRepo.findByCampagneId(id)
                .stream().map(v -> v.getEmploye().getId())
                .collect(Collectors.toList());

        model.addAttribute("campagne", campagne);
        model.addAttribute("employes", employes);
        model.addAttribute("dejaDansCampagne", dejaDansCampagne);
        model.addAttribute("directions", employeService.listerDirections(centreId));

        return "campagnes/ajouter-employes";
    }

    @PostMapping("/{id}/ajouter-employes")
    public String ajouterEmployes(@PathVariable Long id,
            @RequestParam(required = false) List<Long> employeIds,
            @RequestParam(required = false) List<String> directionsSelectionnees,
            @RequestParam(value = "fichierImport", required = false) MultipartFile fichierImport) {
        // Délègue au service qui crée une VisiteVaccin EN_INSTANCE
        // pour chaque employé sélectionné
        campagneService.ajouterEmployes(id, employeIds, directionsSelectionnees, fichierImport);

        return "redirect:/campagnes/" + id + "/visites";
    }

    @GetMapping("/modele-import")
    public void telechargerModele(HttpServletResponse response) throws IOException {
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setHeader("Content-Disposition", "attachment; filename=modele-import-matricules.xlsx");

        try (Workbook wb = new XSSFWorkbook()) {
            Sheet sheet = wb.createSheet("Matricules");

            // En-tête
            Row header = sheet.createRow(0);
            header.createCell(0).setCellValue("matricule");

            // Exemples de matricules réels depuis la BD
            Long centreId = getCentreIdConnecte();
            List<Employe> employes = employeRepo.findByCentreId(centreId);
            int rowNum = 1;
            for (Employe e : employes) {
                Row row = sheet.createRow(rowNum++);
                row.createCell(0).setCellValue(e.getMatricule());
            }

            sheet.autoSizeColumn(0);
            wb.write(response.getOutputStream());
        }
    }

}
