package iama.sipet.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import iama.sipet.entity.ListaEquiposEntity;

@Repository
public interface ListaEquiposRepository extends JpaRepository<ListaEquiposEntity, Integer> {
    // Buscar por ID
    @Query("SELECT l FROM ListaEquiposEntity l WHERE l.id = ?1")
    Optional<ListaEquiposEntity> findById(Integer id);

    @Query("SELECT l FROM ListaEquiposEntity l WHERE l.pdf = ?1 ")
    Optional<ListaEquiposEntity> findByPDF(String encode);

    @Query("SELECT l FROM ListaEquiposEntity l WHERE l.fecha_registro <= ?1")
    List<ListaEquiposEntity> findListasInactivas(LocalDateTime fechaLimite6Mes);
}