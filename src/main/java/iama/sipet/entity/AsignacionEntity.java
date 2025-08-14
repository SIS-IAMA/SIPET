package iama.sipet.entity;

import java.util.Date;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Entity
@Table(name = "asignacion")
@Data
public class AsignacionEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @OneToOne(mappedBy = "asignacion", cascade = CascadeType.ALL)
    @NotNull(message = "La lista de equipos es obligatoria")
    private ListaEquiposEntity listaEquipos;

    @OneToOne(mappedBy = "asignacion", cascade = CascadeType.ALL)
    @NotNull(message = "El empleado es obligatorio")
    private EmpleadoEntity empleado;

    @OneToOne(mappedBy = "asignacionActiva", cascade = CascadeType.ALL)
    @NotNull(message = "La responsiva activa es obligatoria")
    private ResponsivaEntity responsivaActiva;

    @OneToOne(mappedBy = "asignacion", cascade = CascadeType.ALL)
    private AsignacionHistoryEntity asignacionHistory;

    @NotBlank(message = "La ubicación es obligatoria")
    private String ubicacion;

    @NotNull(message = "La fecha de registro es obligatoria")
    private Date fecha_registro;

    private Date fecha_modificacion;

    @NotBlank(message = "El estado es obligatorio")
    private String estado;
}
