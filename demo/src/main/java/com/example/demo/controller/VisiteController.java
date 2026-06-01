package com.example.demo.controller;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.example.demo.dto.DoseDTO;
import com.example.demo.dto.VisiteDTO;
import com.example.demo.entity.Dose;
import com.example.demo.entity.VisiteVaccin;
import com.example.demo.service.VisiteService;

@Controller
@RequestMapping("/campagnes/{campagneId}/visites")
public class VisiteController {
    @Autowired
    private VisiteService visiteService;

    @GetMapping
    public String liste(@PathVariable Long campagneId,@RequestParam(defaultValue="0") int page,@RequestParam(defaultValue="7") int size, Model model) {
       Pageable pageable = PageRequest.of(page,size);
       Page<VisiteVaccin> visitesPage = visiteService.listerParCampagne(campagneId, pageable);

        model.addAttribute("visites", visitesPage.getContent());
        model.addAttribute("campagne", visiteService.getCampagne(campagneId));
        model.addAttribute("campagneId", campagneId);// utile pour construire les liens
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", visitesPage.getTotalPages());
        return "campagnes/visites/liste";
    }

    // formilaire pour enregistrer le consentement de collab
    @GetMapping("/{id}/consentement")
    public String formulaireConsentement(@PathVariable Long campagneId, @PathVariable Long id, Model model) {
        model.addAttribute("visite", visiteService.findById(id));
        model.addAttribute("visiteDTO", new VisiteDTO()); // formulaire vide
        model.addAttribute("campagneId", campagneId);
        return "campagnes/visites/consentement";
    }

    // recevoir et enregistrer le consentement soumis via formulaire
    @PostMapping("/{id}/consentement")
    public String enregisterConsentemet(@PathVariable Long campagneId, @PathVariable long id,
            @ModelAttribute VisiteDTO dto) {
        visiteService.enregistrerConsentement(id, dto);
        return "redirect:/campagnes/" + campagneId + "/visites";
    }

    // med confirme vaccination
    /*
     * @PostMapping("/{id}/vacciner")
     * public String vacciner(@PathVariable Long campagneId, @PathVariable Long id,
     * RedirectAttributes ra) {
     * try {
     * visiteService.vacciner(id);
     * ra.addFlashAttribute("msgSucces", "Employé marqué comme vacciné");
     * } catch (IllegalStateException exception) {
     * ra.addFlashAttribute("msgErreur", exception.getMessage());
     * }
     * return "redirect:/campagnes/" + campagneId + "/visites";
     * }
     */

    @PostMapping("/{id}/non-vacciner")
    public String nonVacciner(@PathVariable Long campagneId, @PathVariable Long id, RedirectAttributes ra) {
        visiteService.nonVacciner(id);
        ra.addFlashAttribute("msgSucces", "Employé marqué comme non vacciné");
        return "redirect:/campagnes/" + campagneId + "/visites";
    }

    // formulaire pour ajouter une dose à une visite
    @GetMapping("/{id}/doses")
    public String formulaireDose(@PathVariable Long campagneId, @PathVariable Long id, Model model) {
        VisiteVaccin visite = visiteService.findById(id);
        List<Dose> doses = visite.getDoses() != null ? visite.getDoses() : new ArrayList<>();

        model.addAttribute("visite", visite);
        model.addAttribute("campagne", visite.getCampagne());
        model.addAttribute("doses", doses);
        model.addAttribute("doseDTO", new DoseDTO());
        model.addAttribute("campagneId", campagneId);
        model.addAttribute("nbDosesMax", visite.getCampagne().getNbDose());
        model.addAttribute("employeId", visite.getEmploye().getId());
        return "campagnes/visites/doses";
    }

    // enregistrer dose via formulaire
    @PostMapping("/{id}/doses")
    public String ajouterDose(@PathVariable Long campagneId, @PathVariable long id, @ModelAttribute DoseDTO doseDTO,
            RedirectAttributes ra) {
        try {
            visiteService.ajouterDose(id, doseDTO);
            ra.addFlashAttribute("msgSucces", "Dose enregistrée");
        } catch (IllegalStateException exception) {
            ra.addFlashAttribute("msgErreur", exception.getMessage());
        }
        return "redirect:/campagnes/" + campagneId + "/visites/" + id + "/doses";
    }

    @PostMapping("/{id}/doses/{doseId}/supprimer")
    public String supprimerDose(@PathVariable Long campagneId, @PathVariable Long id, @PathVariable Long doseId,
            RedirectAttributes ra) {
        visiteService.supprimerDose(doseId);
        ra.addFlashAttribute("msgSucces", "Dose supprimée.");
        return "redirect:/campagnes/" + campagneId + "/visites/" + id + "/doses";
    }

    @PostMapping("/{id}/supprimer")
    public String supprimerEmployeDeCampagne(@PathVariable Long campagneId,
            @PathVariable Long id,
            RedirectAttributes ra) {
        try {
            visiteService.supprimerDesCampagne(id);
            ra.addFlashAttribute("msgSucces", "Employé retiré de la campagne.");
        } catch (Exception e) {
            ra.addFlashAttribute("msgErreur", e.getMessage());
        }
        return "redirect:/campagnes/" + campagneId + "/visites";
    }

}
