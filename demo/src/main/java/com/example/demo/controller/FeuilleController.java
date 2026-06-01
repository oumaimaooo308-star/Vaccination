package com.example.demo.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import com.example.demo.dto.VisiteDTO;
import com.example.demo.entity.VisiteVaccin;
import com.example.demo.repository.UtilisateurRepository;
import com.example.demo.repository.VisiteVaccinRepository;
import com.example.demo.service.VisiteService;

@Controller
@RequestMapping("/feuilles")
public class FeuilleController {
    @Autowired
    private VisiteVaccinRepository visiteRepo;

    @Autowired
    private UtilisateurRepository utilisateurRepo;

    @Autowired
    private  VisiteService visiteService;

    @GetMapping("/{visiteId}/imprimer")
    public String imprimer(@PathVariable Long visiteId,Model model){
        
        VisiteVaccin visite=visiteRepo.findById(visiteId)
            .orElseThrow(() -> new RuntimeException("Visite introuvable : "+visiteId));
          
        model.addAttribute("visite", visite);
        Authentication auth=SecurityContextHolder.getContext().getAuthentication();
        String nomMedecin = utilisateurRepo.findByUsername(auth.getName())
            .map(u -> u.getUsername())
            .orElse("Dr. —");  
        model.addAttribute("medecin", nomMedecin);

        return "feuilles/imprimer"; 
    }

    @GetMapping("/{visiteId}/consentement-form")
    public String afficherFormulaireConsentement(@PathVariable Long visiteId, Model model) {
        VisiteVaccin visite = visiteService.findById(visiteId);
        model.addAttribute("visite",      visite);
        model.addAttribute("employe",     visite.getEmploye());
        model.addAttribute("campagne",    visite.getCampagne());
        
        model.addAttribute("visiteDTO",   new VisiteDTO());
        return "feuilles/consentement-form";
    }

    @PostMapping("/{visiteId}/consentement")
    public String enregistrerConsentement(@PathVariable Long visiteId,@ModelAttribute VisiteDTO dto) {
        visiteService.enregistrerConsentement(visiteId, dto);
        return "redirect:/feuilles/" + visiteId + "/imprimer";
    }


}
