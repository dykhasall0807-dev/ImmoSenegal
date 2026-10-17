package sn.immosenegal.immosenegal.controller;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import sn.immosenegal.immosenegal.dto.RegisterRequest;
import sn.immosenegal.immosenegal.dto.UtilisateurDto;
import sn.immosenegal.immosenegal.service.UtilisateurService;

import java.util.List;

/** Gestion des comptes : réservée au SUPER_ADMIN (règle dans SecurityConfig). */
@RestController
@RequestMapping("/api/admin/utilisateurs")
public class AdminUtilisateurController {

    private final UtilisateurService service;

    public AdminUtilisateurController(UtilisateurService service) {
        this.service = service;
    }

    @GetMapping
    public List<UtilisateurDto> lister() {
        return service.lister();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public UtilisateurDto creer(@Valid @RequestBody RegisterRequest demande) {
        return service.creerParAdmin(demande);
    }

    @PatchMapping("/{id}/actif")
    public UtilisateurDto changerActif(@PathVariable Long id, @RequestParam boolean valeur) {
        return service.changerActif(id, valeur);
    }
}
