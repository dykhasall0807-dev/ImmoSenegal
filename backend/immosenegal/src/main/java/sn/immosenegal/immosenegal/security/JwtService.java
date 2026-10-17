package sn.immosenegal.immosenegal.security;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import sn.immosenegal.immosenegal.entity.Utilisateur;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

/** Création et lecture des jetons JWT (signés avec un secret d'au moins 32 caractères). */
@Service
public class JwtService {

    private final SecretKey cle;
    private final long dureeMs;

    public JwtService(@Value("${app.jwt.secret}") String secret,
                      @Value("${app.jwt.expiration-heures:24}") long heures) {
        byte[] octets = secret.getBytes(StandardCharsets.UTF_8);
        if (octets.length < 32) {
            throw new IllegalStateException("JWT_SECRET doit contenir au moins 32 caractères");
        }
        this.cle = Keys.hmacShaKeyFor(octets);
        this.dureeMs = heures * 3_600_000L;
    }

    public String generer(Utilisateur utilisateur) {
        Date maintenant = new Date();
        return Jwts.builder()
                .subject(utilisateur.getEmail())
                .claim("role", utilisateur.getRole().name())
                .issuedAt(maintenant)
                .expiration(new Date(maintenant.getTime() + dureeMs))
                .signWith(cle)
                .compact();
    }

    /** Renvoie l'email du jeton, ou lève une exception si le jeton est invalide ou expiré. */
    public String extraireEmail(String jeton) {
        return Jwts.parser()
                .verifyWith(cle)
                .build()
                .parseSignedClaims(jeton)
                .getPayload()
                .getSubject();
    }
}
