package sn.immosenegal.immosenegal.controller;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import sn.immosenegal.immosenegal.dto.AuthResponse;
import sn.immosenegal.immosenegal.dto.LoginRequest;
import sn.immosenegal.immosenegal.dto.RegisterRequest;
import sn.immosenegal.immosenegal.dto.UtilisateurDto;
import sn.immosenegal.immosenegal.service.UtilisateurService;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final UtilisateurService service;

    public AuthController(UtilisateurService service) {
        this.service = service;
    }

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    public AuthResponse register(@Valid @RequestBody RegisterRequest demande) {
        return service.inscrire(demande);
    }

    @PostMapping("/login")
    public AuthResponse login(@Valid @RequestBody LoginRequest demande) {
        return service.connecter(demande);
    }

    @GetMapping("/me")
    public UtilisateurDto me(Authentication authentification) {
        return service.moi(authentification.getName());
    }
}
