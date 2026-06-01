package com.example.demo.controller;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.example.demo.entity.CompagneVaccin;
import com.example.demo.entity.Employe;
import com.example.demo.entity.Gestionnaire;
import com.example.demo.entity.Utilisateur;
import com.example.demo.repository.UtilisateurRepository;
import com.example.demo.service.CampagneService;
import com.example.demo.service.EmployeService;

@Controller
@RequestMapping("/employes")
public class EmployeController {

    @Autowired
    private EmployeService employeService;

    @Autowired
    private UtilisateurRepository utilisateurRepo;

    @Autowired
    private CampagneService campagneService;

   
    @GetMapping
public String liste(@RequestParam(required = false) Long campagneId,
                    @RequestParam(defaultValue = "0") int page,
                    @RequestParam(defaultValue = "7") int size,
                    Model model) {

    Authentication auth = SecurityContextHolder.getContext().getAuthentication();
    Utilisateur u = utilisateurRepo.findByUsername(auth.getName()).orElseThrow();

    Pageable pageable = PageRequest.of(page, size);
    Page<Employe> employesPage;
    List<CompagneVaccin> campagnesDuFiltre;

    if (u instanceof Gestionnaire g) {
        Long centreId = g.getCentre().getId();
        employesPage = employeService.listerParCentre(centreId, pageable);
        campagnesDuFiltre = campagneService.listerParCentre(centreId);
    } else {
        employesPage = employeService.listerTous(pageable);
        campagnesDuFiltre = campagneService.listerToutes();
    }

    List<Employe> employes = employesPage.getContent();

    Map<Long, String> statutParEmploye;
    if (campagneId != null) {
        statutParEmploye = employeService.calculerStatutsParCampagne(employes, campagneId);
    } else {
        statutParEmploye = employeService.calculerStatusParEmploye(employes);
    }

    int nbVaccines   = employeService.compterParStatut(statutParEmploye, "VACCINE");
    int nbEnInstance = employeService.compterParStatut(statutParEmploye, "EN_INSTANCE");
    int nbNonVaccine = employeService.compterParStatut(statutParEmploye, "NON_VACCINE");

    model.addAttribute("employes",            employes);
    model.addAttribute("statutParEmploye",    statutParEmploye);
    model.addAttribute("totalEmployes",       employesPage.getTotalElements());
    model.addAttribute("nbVaccines",          nbVaccines);
    model.addAttribute("nbEnInstance",        nbEnInstance);
    model.addAttribute("nbNonVaccines",       nbNonVaccine);
    model.addAttribute("campagnes",           campagnesDuFiltre);
    model.addAttribute("campagneSelectionnee", campagneId);
    model.addAttribute("currentPage",         page);
    model.addAttribute("totalPages",          employesPage.getTotalPages());

    return "employes/liste";
}

   
    @GetMapping("/{id}")
    public String detail(@PathVariable Long id,
            @RequestParam(required = false) Long campagneId,
            Model model) {

        Employe employe = employeService.findById(id);
        model.addAttribute("employe", employe);

        if (campagneId != null) {
            employeService.getVisiteParCampagne(id, campagneId).ifPresent(visite -> {
                model.addAttribute("visite", visite);
                model.addAttribute("doses", visite.getDoses());
            });
        } else {
            employeService.GetderniereVisite(id).ifPresent(visite -> {
                model.addAttribute("visite", visite);
                model.addAttribute("doses", visite.getDoses());
            });
        }

        return "employes/detail";
    }
}