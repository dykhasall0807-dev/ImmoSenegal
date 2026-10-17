package sn.immosenegal.immosenegal.config;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.server.ResponseStatusException;

import java.util.LinkedHashMap;
import java.util.Map;

/** Réponses d'erreur uniformes : {status, message, erreurs?}. */
@RestControllerAdvice
public class GestionnaireErreurs {

    public record ErreurApi(int status, String message, Map<String, String> erreurs) {
    }

    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<ErreurApi> statut(ResponseStatusException ex) {
        String message = ex.getReason() != null ? ex.getReason() : "Erreur";
        return ResponseEntity.status(ex.getStatusCode())
                .body(new ErreurApi(ex.getStatusCode().value(), message, null));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErreurApi> validation(MethodArgumentNotValidException ex) {
        Map<String, String> erreurs = new LinkedHashMap<>();
        ex.getBindingResult().getFieldErrors()
                .forEach(e -> erreurs.putIfAbsent(e.getField(), e.getDefaultMessage()));
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new ErreurApi(400, "Données invalides", erreurs));
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErreurApi> corpsIllisible(HttpMessageNotReadableException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new ErreurApi(400, "Corps de requête invalide", null));
    }
}
