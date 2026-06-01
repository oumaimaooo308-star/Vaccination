package com.example.demo.controller;

import com.example.demo.entity.Consultant;
import com.example.demo.entity.Gestionnaire;
import com.example.demo.entity.Utilisateur;
import com.example.demo.repository.UtilisateurRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

@ControllerAdvice
public class GlobalControllerAdvice {

    @Autowired
    private UtilisateurRepository utilisateurRepo;


    private Utilisateur getUtilisateurConnecte() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        //Vérification : pas d'auth, anonyme, ou pas encore connecté
        if (auth == null
                || !auth.isAuthenticated()
                || "anonymousUser".equals(auth.getPrincipal())) {
            return null;
        }

        return utilisateurRepo.findByUsername(auth.getName()).orElse(null);
    }

    @ModelAttribute("centreNom")
    public String centreNom() {
        Utilisateur u = getUtilisateurConnecte();
        if (u instanceof Gestionnaire g && g.getCentre() != null) {
            return g.getCentre().getVille();
        }
        return "—";
    }

    @ModelAttribute("centreId")
    public Long centreId() {
        Utilisateur u = getUtilisateurConnecte();
        if (u instanceof Gestionnaire g && g.getCentre() != null) {
            return g.getCentre().getId();
        }
        return null;
    }

    @ModelAttribute("userName")
    public String userName() {
        Utilisateur u = getUtilisateurConnecte();
        return u != null ? u.getUsername() : "—";
    }

    @ModelAttribute("userInitiales")
    public String userInitiales() {
        Utilisateur u = getUtilisateurConnecte();
        if (u == null || u.getUsername() == null) return "?";
        String username = u.getUsername().toUpperCase();
        return username.length() >= 2 ? username.substring(0, 2) : username;
    }

    @ModelAttribute("userRole")
    public String userRole() {
        Utilisateur u = getUtilisateurConnecte();
        if (u == null) return "—";
        return switch (u.getRole()) {
            case "ROLE_GESTIONNAIRE" -> "Médecin / Infirmier";
            case "ROLE_ADMIN"        -> "Administrateur";
            case "ROLE_CONSULTANT"   -> "Consultant RH";
            default                  -> u.getRole();
        };
    }

    //  pour sec:authorize dans les vues
    @ModelAttribute("isGestionnaire")
    public boolean isGestionnaire() {
        Utilisateur u = getUtilisateurConnecte();
        return u instanceof Gestionnaire;
    }

    @ModelAttribute("isConsultant")
    public boolean isConsultant() {
        Utilisateur u = getUtilisateurConnecte();
        return u instanceof Consultant;
    }
}