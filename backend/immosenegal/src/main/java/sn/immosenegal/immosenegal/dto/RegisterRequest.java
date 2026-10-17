package sn.immosenegal.immosenegal.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import sn.immosenegal.immosenegal.entity.Role;

public record RegisterRequest(
        @NotBlank(message = "Le nom est obligatoire") String nom,
        @NotBlank(message = "L'email est obligatoire")
        @Email(message = "Email invalide") String email,
        String telephone,
        @NotBlank(message = "Le mot de passe est obligatoire")
        @Size(min = 8, message = "8 caractères minimum") String motDePasse,
        @NotNull(message = "Le rôle est obligatoire") Role role) {
}
