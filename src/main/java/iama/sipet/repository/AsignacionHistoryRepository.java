package iama.sipet.repository;

import iama.sipet.entity.AsignacionEntity;
import iama.sipet.entity.AsignacionHistoryEntity;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface AsignacionHistoryRepository extends JpaRepository<AsignacionHistoryEntity, Integer> {
    @Query ("select a from AsignacionHistoryEntity a where a.asignacion.id = ?1")
    Optional<AsignacionHistoryEntity> findByAsignacion(Integer id);
}
