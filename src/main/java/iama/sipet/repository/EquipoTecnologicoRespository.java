package iama.sipet.repository;

import iama.sipet.entity.EquipoTecnologicoEntity;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface EquipoTecnologicoRespository extends JpaRepository<EquipoTecnologicoEntity, Integer> {

    @Query("SELECT e FROM EquipoTecnologicoEntity e WHERE e.id= ?1")
    Optional<EquipoTecnologicoEntity> findById(Integer id);

    @Query("SELECT e FROM EquipoTecnologicoEntity e WHERE e.numero_serie = ?1")
    Optional<EquipoTecnologicoEntity> findByNumeroSerie(String numeroSerie);

    @Query(value = "SELECT * FROM equipo_tecnologico WHERE estado = 'ACTIVO' or estado = 'ASIGNADO' or estado = 'PENDIENTE' ORDER BY id DESC", nativeQuery = true)
    List<EquipoTecnologicoEntity> findByEstado();

    @Query("SELECT e FROM EquipoTecnologicoEntity e WHERE e.foto = ?1")
    Optional<EquipoTecnologicoEntity> findByFoto(String originalFilename);

    @Query(value = "SELECT * FROM equipo_tecnologico WHERE estado = 'ACTIVO' ORDER BY id DESC", nativeQuery = true)
    List<EquipoTecnologicoEntity> findByEstadoActivo();
}

