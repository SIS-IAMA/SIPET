package iama.sipet.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.Date;
import java.util.List;

@Entity
@Table(name = "empleado")
@Data
public class EmpleadoEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @OneToOne
    @JoinColumn(name= "id_asignacion_empleado")
    private AsignacionEntity asignacion;

    @OneToMany(mappedBy = "empleado", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ResponsivaEntity> responsiva;

    @NotBlank(message = "El nombre es obligatorio")
    private String nombre;

    @NotBlank(message = "El apellido paterno es obligatorio")
    private String apellido_p;

    @NotBlank(message = "El apellido materno es obligatorio")
    private String apellido_m;

    @NotBlank(message = "El puesto es obligatorio")
    private String puesto;

    @NotBlank(message = "El departamento es obligatorio")
    private String departamento;

    @NotNull(message = "El teléfono es obligatorio")
    private Long telefono;

    @NotNull(message = "La fecha de registro es obligatoria")
    private Date fecha_registro;

    private String foto;

    private boolean estado;

    @JsonProperty("asignacionID")
    public Integer getAsignacionID() {
        return asignacion != null ? asignacion.getId() : null;
    }

    @JsonIgnore
    public AsignacionEntity getAsignacion() {
        return asignacion;
    }
}
