package iama.sipet.repository;

import iama.sipet.entity.AsignacionEntity;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface AsignacionRespository extends JpaRepository<AsignacionEntity, Integer> {
    @Query(value = "SELECT * FROM asignacion ORDER BY fecha_registro DESC", nativeQuery = true)
    List<AsignacionEntity> findAll();

    @Query ("select a from AsignacionEntity a where a.empleado.id = ?1")
    Optional<AsignacionEntity> findByEmpleado(Integer id);

    @Query("select a from AsignacionEntity a where a.listaEquipos.id = ?1")
    Optional<AsignacionEntity> findByList(Integer id);
}
