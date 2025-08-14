package iama.sipet.repository;

import iama.sipet.entity.EmpleadoEntity;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface EmpleadoRepository extends JpaRepository<EmpleadoEntity, Integer> {
    @Query (value = "SELECT * FROM empleado WHERE estado = true ORDER BY fecha_registro DESC", nativeQuery = true)
    List<EmpleadoEntity> findActive();

    @Query ("SELECT e FROM EmpleadoEntity e WHERE e.telefono = ?1 ")
    Optional<EmpleadoEntity> findByPhone(Long telefono);

    @Query("SELECT e FROM EmpleadoEntity e WHERE e.nombre = ?1 AND e.apellido_p = ?2 AND e.apellido_m = ?3 AND e.puesto = ?4 AND e.departamento = ?5 AND e.telefono = ?6")
    Optional<EmpleadoEntity> findByFullData(String nombre, String apellido_p, String apellido_m, String puesto, String departamento, String telefono);

    @Query("SELECT e FROM EmpleadoEntity e WHERE e.estado = false")
    Optional<EmpleadoEntity> findByFoto(String originalFilename);

    @Query("SELECT e FROM EmpleadoEntity e WHERE e.estado = false")
    List<EmpleadoEntity> findAllByEstadoFalse();

    @Query("SELECT e FROM EmpleadoEntity e WHERE e.estado = false AND e.fecha_registro <= ?1")
    List<EmpleadoEntity> findDeshabilitadosAntiguos(java.time.LocalDateTime fechaLimite);
}
