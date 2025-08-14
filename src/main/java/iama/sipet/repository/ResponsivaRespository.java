package iama.sipet.repository;

import iama.sipet.entity.ResponsivaEntity;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface ResponsivaRespository extends JpaRepository<ResponsivaEntity, Integer> {
    @Query("SELECT r FROM ResponsivaEntity r WHERE r.pdf= ?1")
    Optional<ResponsivaEntity> findByPdf(String pdf);

    @Query("SELECT r FROM ResponsivaEntity r WHERE r.asignacionActiva.id = ?1 and r.estado = true")
    Optional<ResponsivaEntity> getByAsignacionActiva(Integer id);

    @Query(value = "SELECT * FROM responsiva WHERE id_asignacion_responsiva = ?1 ORDER BY fecha_registro DESC LIMIT 1", nativeQuery = true)
    Optional<ResponsivaEntity> findByAsignacionReciente(Integer id);
}
