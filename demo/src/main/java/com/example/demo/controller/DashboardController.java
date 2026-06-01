package com.example.demo.controller;

import com.example.demo.entity.Gestionnaire;
import com.example.demo.entity.Utilisateur;
import com.example.demo.repository.UtilisateurRepository;
import com.example.demo.service.DashboardService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class DashboardController {

    @Autowired
    private DashboardService dashboardService;
    @Autowired
    private UtilisateurRepository utilisateurRepo;

    
    @GetMapping("/home")
    public String dashboard(@RequestParam(required = false) Long campagneId, Model model) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        Utilisateur u = utilisateurRepo.findByUsername(auth.getName()).orElseThrow();

        //  Consultant voit les stats globales, pas filtrées par centre
        model.addAttribute("toutesLesCampagnes", dashboardService.listerCampagnes());
        model.addAttribute("campagneSelectionnee", campagneId);

        if (campagneId != null) {
            model.addAttribute("totalEmployes", dashboardService.totalEmployesParCampagne(campagneId));
            model.addAttribute("nbVaccines", dashboardService.nbVaccinesParCampagne(campagneId));
            model.addAttribute("nbEnInstance", dashboardService.nbEnInstanceParCampagne(campagneId));
            model.addAttribute("nbNonVaccines", dashboardService.nbNonVaccinesParCampagne(campagneId));
            model.addAttribute("visitesPendantes", dashboardService.dernieresVisitesEnInstanceParCampagne(campagneId));
            model.addAttribute("dernieresDoses", dashboardService.dernieresDosesParCampagne(campagneId));
            model.addAttribute("nbRefus", dashboardService.nbRefusParCampagne(campagneId));
            model.addAttribute("nbEnCours", dashboardService.nbEnCoursParCampagne(campagneId));
        } else if (u instanceof Gestionnaire g) {
            // Gestionnaire → stats de son centre uniquement
            Long centreId = g.getCentre().getId();
            model.addAttribute("totalEmployes", dashboardService.totalEmployesParCentre(centreId));
            model.addAttribute("nbVaccines", dashboardService.nbVaccinesParCentre(centreId));
            model.addAttribute("nbEnInstance", dashboardService.nbEnInstanceParCentre(centreId));
            model.addAttribute("nbNonVaccines", dashboardService.nbNonVaccinesParCentre(centreId));
            model.addAttribute("visitesPendantes", dashboardService.dernieresVisitesEnInstanceParCentre(centreId));
            model.addAttribute("dernieresDoses", dashboardService.dernieresDosesParCentre(centreId));
            model.addAttribute("nbRefus", dashboardService.nbRefusParCentre(centreId));
            model.addAttribute("nbEnCours", dashboardService.nbEnCoursParCentre(centreId));
        } else {
            // Consultant → stats globales toutes campagnes / tous centres
            model.addAttribute("totalEmployes", dashboardService.totalEmployes());
            model.addAttribute("nbVaccines", dashboardService.nbVaccines());
            model.addAttribute("nbEnInstance", dashboardService.nbEnInstance());
            model.addAttribute("nbNonVaccines", dashboardService.nbNonVaccines());
            model.addAttribute("visitesPendantes", dashboardService.dernieresVisitesEnInstance());
            model.addAttribute("dernieresDoses", dashboardService.dernieresDoses());
            model.addAttribute("nbRefus", dashboardService.nbRefus());
            model.addAttribute("nbEnCours", dashboardService.nbEnCours());
        }

        return "home";
    }

    
    @GetMapping("/login")
    public String loginPage() {
        return "auth/login"; 
    }

}
