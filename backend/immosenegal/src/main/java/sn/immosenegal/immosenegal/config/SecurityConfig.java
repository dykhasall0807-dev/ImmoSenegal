package sn.immosenegal.immosenegal.config;

import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import sn.immosenegal.immosenegal.repository.UtilisateurRepository;
import sn.immosenegal.immosenegal.security.JwtAuthFilter;
import sn.immosenegal.immosenegal.security.JwtService;

import java.io.IOException;
import java.util.List;

@Configuration
public class SecurityConfig {

    private final JwtService jwtService;
    private final UtilisateurRepository utilisateurs;

    public SecurityConfig(JwtService jwtService, UtilisateurRepository utilisateurs) {
        this.jwtService = jwtService;
        this.utilisateurs = utilisateurs;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable())
            .cors(Customizer.withDefaults())
            // Pas de session : chaque requête porte son jeton JWT
            .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                // Public : connexion, inscription, santé du serveur
                .requestMatchers("/api/auth/register", "/api/auth/login", "/actuator/health", "/error").permitAll()
                // Visiteurs : consultation du catalogue
                .requestMatchers(HttpMethod.GET, "/api/biens/**").permitAll()
                // Administration : super-admin uniquement
                .requestMatchers("/api/admin/**").hasRole("SUPER_ADMIN")
                // Publier / modifier / supprimer un bien : propriétaire, agent, admin
                .requestMatchers(HttpMethod.POST, "/api/biens/**").hasAnyRole("PROPRIETAIRE", "AGENT", "SUPER_ADMIN")
                .requestMatchers(HttpMethod.PUT, "/api/biens/**").hasAnyRole("PROPRIETAIRE", "AGENT", "SUPER_ADMIN")
                .requestMatchers(HttpMethod.DELETE, "/api/biens/**").hasAnyRole("PROPRIETAIRE", "AGENT", "SUPER_ADMIN")
                // Tout le reste : être connecté
                .anyRequest().authenticated())
            .exceptionHandling(e -> e
                .authenticationEntryPoint((req, res, ex) ->
                        ecrireErreur(res, HttpServletResponse.SC_UNAUTHORIZED, "Authentification requise"))
                .accessDeniedHandler((req, res, ex) ->
                        ecrireErreur(res, HttpServletResponse.SC_FORBIDDEN, "Accès refusé pour votre rôle")))
            .addFilterBefore(new JwtAuthFilter(jwtService, utilisateurs), UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource(
            @Value("${app.frontend-url:http://localhost:5173}") String urlFrontend) {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOriginPatterns(List.of(urlFrontend));
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        config.setAllowedHeaders(List.of("Authorization", "Content-Type"));
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }

    private static void ecrireErreur(HttpServletResponse reponse, int statut, String message) throws IOException {
        reponse.setStatus(statut);
        reponse.setContentType("application/json;charset=UTF-8");
        reponse.getWriter().write("{\"status\":" + statut + ",\"message\":\"" + message + "\"}");
    }
}
