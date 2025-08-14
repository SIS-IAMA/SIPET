package iama.sipet.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import jakarta.validation.constraints.NotEmpty;

import java.util.Date;
import java.util.List;

@Entity
@Table(name = "user")
@Data
public class UserEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @NotEmpty(message = "El nombre de usuario es obligatorio")
    private String username;

    @NotEmpty(message = "La contraseña es obligatoria")
    private String password;

    @NotEmpty(message = "El nombre es obligatorio")
    private String nombre;

    @NotEmpty(message = "El apellido paterno es obligatorio")
    private String apellido_p;

    @NotEmpty(message = "El apellido materno es obligatorio")
    private String apellido_m;

    // Aquí usamos tipos Boolean para validarlos con @NotNull
    @NotNull(message = "El rol es obligatorio")
    private Boolean rol;

    @NotNull(message = "La fecha de registro es obligatoria")
    private Date fecha_registro;

    private String foto;

    @NotNull(message = "El estado es obligatorio")
    private Boolean estado;

    private String token;
    private Date fecha_token;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL)
    private List<PeticionesEntity> peticiones;

    public Boolean getRol() {
        return rol;
    }

    public Boolean getEstado() {
        return estado;
    }

    @JsonIgnore
    public String getPassword() {
        return password;
    }
}
