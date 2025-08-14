package iama.sipet.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.Date;

@Entity
@Table(name = "responsiva")
@Data
public class ResponsivaEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne
    @JoinColumn(name = "id_empleado_responsiva")
    @NotNull(message = "El empleado es obligatorio")
    private EmpleadoEntity empleado;

    @OneToOne
    @JoinColumn(name = "id_asignacion_responsiva_activa")
    private AsignacionEntity asignacionActiva;

    @NotBlank(message = "La responsiva en formato pdf es obligatoria")
    private String pdf;

    @NotNull(message = "La fecha de registro es obligatoria")
    private Date fecha_registro;

    private boolean estado; // Reemplazamos boolean por Boolean para permitir validación

    // Métodos de ayuda para mostrar solo IDs

    @JsonProperty("empleadoID")
    public Integer getEmpleadoID() {
        return empleado != null ? empleado.getId() : null;
    }

    @JsonIgnore
    public EmpleadoEntity getEmpleado() {
        return empleado;
    }

    @JsonProperty("asignacionActivaID")
    public Integer getAsignacionActivaID() {
        return asignacionActiva != null ? asignacionActiva.getId() : null;
    }

    @JsonIgnore
    public AsignacionEntity getAsignacionActiva() {
        return asignacionActiva;
    }
}
