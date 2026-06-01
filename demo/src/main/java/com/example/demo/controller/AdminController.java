package com.example.demo.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.example.demo.service.AdminService;

@Controller
@RequestMapping("/admin")
public class AdminController {

    @Autowired
    private AdminService adminService;

    @GetMapping("/utilisateurs")
    public String liste(Model model) {
        model.addAttribute("utilisateurs", adminService.listerUtilisateurs());
        model.addAttribute("centres", adminService.listerCentres());
        return "admin/utilisateurs";
    }

    @PostMapping("/utilisateurs/gestionnaire")
    private String creerGestionnaire(@RequestParam String username, @RequestParam String password,
            @RequestParam Long centreId, RedirectAttributes ra) {
        try {
            adminService.creerGestionnaire(username, password, centreId);
            ra.addFlashAttribute("msgSucces", "Gestionnaire « " + username + " » créé avec succès.");
        } catch (Exception e) {
            ra.addFlashAttribute("msgErreur", e.getMessage());
        }
        return "redirect:/admin/utilisateurs";
    }

    @PostMapping("/utilisateurs/consultant")
    public String creerConsultant(
            @RequestParam String username,
            @RequestParam String password,
            RedirectAttributes ra) {
        try {
            adminService.creerConsultant(username, password);
            ra.addFlashAttribute("msgSucces",
                    "Consultant « " + username + " » créé avec succès.");
        } catch (Exception e) {
            ra.addFlashAttribute("msgErreur", e.getMessage());
        }
        return "redirect:/admin/utilisateurs";
    }

    @PostMapping("/utilisateurs/{id}/reinitialiser")
    public String reinitialiser(
            @PathVariable Long id,
            @RequestParam String nouveauMdp,
            RedirectAttributes ra) {
        try {
            adminService.reinitialiserMotDePasse(id, nouveauMdp);
            ra.addFlashAttribute("msgSucces", "Mot de passe réinitialisé.");
        } catch (Exception e) {
            ra.addFlashAttribute("msgErreur", e.getMessage());
        }
        return "redirect:/admin/utilisateurs";
    }

    @PostMapping("/utilisateurs/{id}/supprimer")
    public String supprimer(
            @PathVariable Long id,
            RedirectAttributes ra) {
        try {
            adminService.supprimer(id);
            ra.addFlashAttribute("msgSucces", "Utilisateur supprimé.");
        } catch (Exception e) {
            ra.addFlashAttribute("msgErreur", e.getMessage());
        }
        return "redirect:/admin/utilisateurs";
    }

    @PostMapping("/utilisateurs/{id}/changer-centre")
    public String changerCentre(@PathVariable Long id,
            @RequestParam Long centreId,
            RedirectAttributes ra) {
        try {
            adminService.changerCentre(id, centreId);
            ra.addFlashAttribute("msgSucces", "Centre mis à jour avec succès.");
        } catch (Exception e) {
            ra.addFlashAttribute("msgErreur", e.getMessage());
        }
        return "redirect:/admin/utilisateurs";
    }

}
