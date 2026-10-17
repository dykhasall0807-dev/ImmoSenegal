package sn.immosenegal.immosenegal.config;

import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import sn.immosenegal.immosenegal.entity.Role;
import sn.immosenegal.immosenegal.entity.Utilisateur;
import sn.immosenegal.immosenegal.repository.UtilisateurRepository;

/** Crée les 4 comptes de démonstration au premier démarrage (mot de passe commun : Passer123!). */
@Component
public class DonneesDemo implements CommandLineRunner {

    private static final String MOT_DE_PASSE_DEMO = "Passer123!";

    private final UtilisateurRepository repository;
    private final PasswordEncoder encodeur;

    public DonneesDemo(UtilisateurRepository repository, PasswordEncoder encodeur) {
        this.repository = repository;
        this.encodeur = encodeur;
    }

    @Override
    public void run(String... args) {
        creerSiAbsent("Administrateur", "admin@immosenegal.sn", Role.SUPER_ADMIN);
        creerSiAbsent("Agent Démo", "agent@immosenegal.sn", Role.AGENT);
        creerSiAbsent("Propriétaire Démo", "proprio@immosenegal.sn", Role.PROPRIETAIRE);
        creerSiAbsent("Client Démo", "client@immosenegal.sn", Role.CLIENT);
    }

    private void creerSiAbsent(String nom, String email, Role role) {
        if (repository.existsByEmail(email)) {
            return;
        }
        Utilisateur utilisateur = new Utilisateur();
        utilisateur.setNom(nom);
        utilisateur.setEmail(email);
        utilisateur.setMotDePasse(encodeur.encode(MOT_DE_PASSE_DEMO));
        utilisateur.setRole(role);
        repository.save(utilisateur);
    }
}
