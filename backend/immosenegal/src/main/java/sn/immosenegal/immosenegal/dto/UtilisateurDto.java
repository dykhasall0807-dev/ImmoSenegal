package sn.immosenegal.immosenegal.dto;

import sn.immosenegal.immosenegal.entity.Role;
import sn.immosenegal.immosenegal.entity.Utilisateur;

/** Ce qu'on expose de l'utilisateur : jamais le mot de passe. */
public record UtilisateurDto(Long id, String nom, String email, String telephone, Role role, boolean actif) {

    public static UtilisateurDto de(Utilisateur u) {
        return new UtilisateurDto(u.getId(), u.getNom(), u.getEmail(), u.getTelephone(), u.getRole(), u.isActif());
    }
}
