package sn.immosenegal.immosenegal.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import sn.immosenegal.immosenegal.entity.BienImmobilier;

public interface BienImmobilierRepository
        extends JpaRepository<BienImmobilier, Long> {
}