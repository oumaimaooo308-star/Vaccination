package com.example.demo.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.example.demo.entity.Centre;
import com.example.demo.entity.Consultant;
import com.example.demo.entity.Gestionnaire;
import com.example.demo.entity.Utilisateur;
import com.example.demo.repository.CentreRepository;
import com.example.demo.repository.UtilisateurRepository;

@Service
public class AdminService {

    @Autowired
    private UtilisateurRepository utilisateurRepo;
    @Autowired
    private CentreRepository centreRepo;
    @Autowired
    private PasswordEncoder passwordEncoder;

    // lister tous les utilisateurs sauf admin
    public List<Utilisateur> listerUtilisateurs() {
        return utilisateurRepo.findAll();
    }

    public List<Centre> listerCentres() {
        return centreRepo.findAll();
    }

    public Utilisateur findById(Long id) {
        return utilisateurRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Utilisateur introuvable"));
    }

    public void creerGestionnaire(String username, String password, Long centreId) {
        if (utilisateurRepo.findByUsername(username).isPresent())
            throw new RuntimeException("Ce nom d'utilisateur existe déja");
        Centre centre = centreRepo.findById(centreId)
                .orElseThrow(() -> new RuntimeException("Centre introuvable"));

        Gestionnaire g = new Gestionnaire();
        g.setUsername(username);
        g.setPassword(passwordEncoder.encode(password));
        g.setRole("ROLE_GESTIONNAIRE");
        g.setCentre(centre);
        utilisateurRepo.save(g);
    }

    public void creerConsultant(String username, String password) {
        if (utilisateurRepo.findByUsername(username).isPresent())
            throw new RuntimeException("Ce nom d'utilisateur existe déjà");
        Consultant c = new Consultant();
        c.setUsername(username);
        c.setPassword(passwordEncoder.encode(password));
        c.setRole("ROLE_CONSULTANT");
        utilisateurRepo.save(c);
    }

    public void reinitialiserMotDePasse(Long id, String nouveauMdp) {
        Utilisateur u = findById(id);
        u.setPassword(passwordEncoder.encode(nouveauMdp));
        utilisateurRepo.save(u);
    }

    public void supprimer(Long id) {
        utilisateurRepo.deleteById(id);
    }

    public void changerCentre(Long gestionnaireId, Long nouveauCentreId) {
        Utilisateur u = findById(gestionnaireId);
        if (!(u instanceof Gestionnaire g))
            throw new RuntimeException("Cet utilisateur n'est pas un gestionnaire");

        Centre centre = centreRepo.findById(nouveauCentreId)
                .orElseThrow(() -> new RuntimeException("Centre introuvable"));
        g.setCentre(centre);
        utilisateurRepo.save(g);
    }

}
