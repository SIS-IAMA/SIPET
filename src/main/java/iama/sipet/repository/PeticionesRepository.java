package iama.sipet.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import iama.sipet.entity.PeticionesEntity;

@Repository
public interface PeticionesRepository extends JpaRepository<PeticionesEntity, Integer> {
    @Query(value = "SELECT * FROM peticiones ORDER BY fecha_registro DESC", nativeQuery = true)
    List<PeticionesEntity> findAll();

    @Query(value = "SELECT * FROM peticiones WHERE usuarios = ?1 ORDER BY fecha_registro DESC", nativeQuery = true)
    List<PeticionesEntity> findByUser(Integer id);

    @Query("SELECT p FROM PeticionesEntity p WHERE p.id = ?1")
    Optional<PeticionesEntity> findById(Integer id);

    @Query("SELECT p FROM PeticionesEntity p WHERE p.user.id = ?1 and p.tipo_peticion = ?2")
    Optional<PeticionesEntity> findByUserPassword(Integer id, String tipo_peticion);

    @Query("SELECT p FROM PeticionesEntity p WHERE p.id_entidad = ?1 and p.categoria = ?2 and p.tipo_peticion = ?3")
    Optional<PeticionesEntity> findByEntidadCategoriaTipo(Integer idEntidad, String categoria, String tipo);

    @Query("SELECT p FROM PeticionesEntity p WHERE p.estado = 'ACEPTADA' AND p.fecha_registro <= ?1")
    List<PeticionesEntity> findPeticionesAceptadasAntiguas(LocalDateTime fechaLimite1Semana);
}
