package sn.immosenegal.immosenegal.security;

import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;
import sn.immosenegal.immosenegal.repository.UtilisateurRepository;

import java.io.IOException;
import java.util.List;

/** Lit l'en-tête « Authorization: Bearer <jeton> » et identifie l'utilisateur pour chaque requête. */
public class JwtAuthFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final UtilisateurRepository utilisateurs;

    public JwtAuthFilter(JwtService jwtService, UtilisateurRepository utilisateurs) {
        this.jwtService = jwtService;
        this.utilisateurs = utilisateurs;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest requete, HttpServletResponse reponse, FilterChain chaine)
            throws ServletException, IOException {
        String entete = requete.getHeader("Authorization");
        if (entete != null && entete.startsWith("Bearer ")) {
            try {
                String email = jwtService.extraireEmail(entete.substring(7));
                utilisateurs.findByEmail(email)
                        .filter(u -> u.isActif())
                        .ifPresent(u -> {
                            var authentification = new UsernamePasswordAuthenticationToken(
                                    u.getEmail(), null,
                                    List.of(new SimpleGrantedAuthority("ROLE_" + u.getRole().name())));
                            SecurityContextHolder.getContext().setAuthentication(authentification);
                        });
            } catch (JwtException | IllegalArgumentException e) {
                // Jeton invalide ou expiré : la requête reste anonyme et sera refusée si besoin
            }
        }
        chaine.doFilter(requete, reponse);
    }
}
