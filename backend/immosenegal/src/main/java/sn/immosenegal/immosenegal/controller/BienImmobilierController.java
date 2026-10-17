
package sn.immosenegal.immosenegal.controller;

import java.util.List;
import java.util.Optional;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import sn.immosenegal.immosenegal.entity.BienImmobilier;
import sn.immosenegal.immosenegal.repository.BienImmobilierRepository;

@RestController
@RequestMapping("/api/biens")
public class BienImmobilierController {

    private final BienImmobilierRepository repository;

    public BienImmobilierController(BienImmobilierRepository repository) {
        this.repository = repository;
    }

    // Afficher tous les biens
    @GetMapping
    public List<BienImmobilier> obtenirTousLesBiens() {
        return repository.findAll();
    }

    // Afficher un bien par son identifiant
    @GetMapping("/{id}")
    public ResponseEntity<BienImmobilier> obtenirUnBien(
            @PathVariable Long id) {

        return repository.findById(id)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    // Ajouter un bien
    @PostMapping
    public ResponseEntity<BienImmobilier> ajouterUnBien(
            @RequestBody BienImmobilier bien) {

        BienImmobilier nouveauBien = repository.save(bien);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(nouveauBien);
    }

    // Modifier un bien
    @PutMapping("/{id}")
    public ResponseEntity<BienImmobilier> modifierUnBien(
            @PathVariable Long id,
            @RequestBody BienImmobilier donnees) {

        Optional<BienImmobilier> resultat = repository.findById(id);

        if (resultat.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        BienImmobilier bien = resultat.get();
        bien.setTitre(donnees.getTitre());
        bien.setDescription(donnees.getDescription());
        bien.setType(donnees.getType());
        bien.setPrix(donnees.getPrix());
        bien.setSurface(donnees.getSurface());
        bien.setAdresse(donnees.getAdresse());
        bien.setVille(donnees.getVille());

        return ResponseEntity.ok(repository.save(bien));
    }

    // Supprimer un bien
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> supprimerUnBien(@PathVariable Long id) {

        if (!repository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }

        repository.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}
