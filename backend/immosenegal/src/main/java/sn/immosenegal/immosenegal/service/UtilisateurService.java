package sn.immosenegal.immosenegal.service;

import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import sn.immosenegal.immosenegal.dto.AuthResponse;
import sn.immosenegal.immosenegal.dto.LoginRequest;
import sn.immosenegal.immosenegal.dto.RegisterRequest;
import sn.immosenegal.immosenegal.dto.UtilisateurDto;
import sn.immosenegal.immosenegal.entity.Role;
import sn.immosenegal.immosenegal.entity.Utilisateur;
import sn.immosenegal.immosenegal.repository.UtilisateurRepository;
import sn.immosenegal.immosenegal.security.JwtService;

import java.util.List;

@Service
public class UtilisateurService {

    private static final String MESSAGE_CONNEXION = "Email ou mot de passe incorrect";

    private final UtilisateurRepository repository;
    private final PasswordEncoder encodeur;
    private final JwtService jwtService;

    public UtilisateurService(UtilisateurRepository repository, PasswordEncoder encodeur, JwtService jwtService) {
        this.repository = repository;
        this.encodeur = encodeur;
        this.jwtService = jwtService;
    }

    /** Inscription publique : seulement CLIENT ou PROPRIETAIRE. */
    @Transactional
    public AuthResponse inscrire(RegisterRequest demande) {
        if (demande.role() != Role.CLIENT && demande.role() != Role.PROPRIETAIRE) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                    "L'inscription publique est réservée aux rôles CLIENT et PROPRIETAIRE");
        }
        Utilisateur utilisateur = creer(demande);
        return new AuthResponse(jwtService.generer(utilisateur), UtilisateurDto.de(utilisateur));
    }

    /** Création d'un compte (agent, admin...) par un super-admin. */
    @Transactional
    public UtilisateurDto creerParAdmin(RegisterRequest demande) {
        return UtilisateurDto.de(creer(demande));
    }

    public AuthResponse connecter(LoginRequest demande) {
        Utilisateur utilisateur = repository.findByEmail(demande.email().trim().toLowerCase())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, MESSAGE_CONNEXION));
        if (!utilisateur.isActif() || !encodeur.matches(demande.motDePasse(), utilisateur.getMotDePasse())) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, MESSAGE_CONNEXION);
        }
        return new AuthResponse(jwtService.generer(utilisateur), UtilisateurDto.de(utilisateur));
    }

    public UtilisateurDto moi(String email) {
        return repository.findByEmail(email)
                .map(UtilisateurDto::de)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Utilisateur introuvable"));
    }

    public List<UtilisateurDto> lister() {
        return repository.findAll().stream().map(UtilisateurDto::de).toList();
    }

    @Transactional
    public UtilisateurDto changerActif(Long id, boolean actif) {
        Utilisateur utilisateur = repository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Utilisateur introuvable"));
        utilisateur.setActif(actif);
        return UtilisateurDto.de(repository.save(utilisateur));
    }

    private Utilisateur creer(RegisterRequest demande) {
        String email = demande.email().trim().toLowerCase();
        if (repository.existsByEmail(email)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Cet email est déjà utilisé");
        }
        Utilisateur utilisateur = new Utilisateur();
        utilisateur.setNom(demande.nom().trim());
        utilisateur.setEmail(email);
        utilisateur.setTelephone(demande.telephone());
        utilisateur.setMotDePasse(encodeur.encode(demande.motDePasse()));
        utilisateur.setRole(demande.role());
        return repository.save(utilisateur);
    }
}
