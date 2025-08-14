package iama.sipet.repository;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import iama.sipet.entity.UserEntity;

@Repository
public interface UserRepository extends JpaRepository<UserEntity, Integer> {
    public Optional<UserEntity> findByUsername(String username);

    @Query("SELECT u FROM UserEntity u WHERE u.rol = false ")
    List<UserEntity> findOperadores();

    @Query("SELECT u FROM UserEntity u WHERE u.id = ?1")
    public Optional<UserEntity> findById(Long id);

    @Query("SELECT u FROM UserEntity u WHERE u.foto = ?1")
    Optional<UserEntity> findByFoto(String encode);

    @Query(value =  "SELECT * FROM user WHERE estado = true AND rol = false ORDER BY fecha_registro DESC", nativeQuery = true)
    List<UserEntity> findOperadoresActivos();

    @Query("SELECT u FROM UserEntity u WHERE u.id = ?1")
    Optional<UserEntity> findByid(Integer id);

    @Query("SELECT u FROM UserEntity u WHERE u.token =?1")
    Optional<UserEntity> findByToken(String token);

    @Query("SELECT u FROM UserEntity u WHERE u.estado = false AND u.fecha_registro <= ?1")
    List<UserEntity> findOperadoresInactivos(LocalDateTime fechaLimite2Meses);
}
