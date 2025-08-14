package iama.sipet.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.util.Date;
import java.util.List;

@Entity
@EqualsAndHashCode(exclude = "asignacion")
@ToString(exclude = "asignacion")
@Table(name = "lista_equipos")
@Data
public class ListaEquiposEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @OneToMany(mappedBy = "listaEquipos", cascade = CascadeType.ALL)
    @Size(min = 1, message = "Debe incluir al menos un equipo tecnológico")
    private List<EquipoTecnologicoEntity> equipoTecnologico;

    @OneToOne
    @JoinColumn(name = "id_asignacion_lista")
    private AsignacionEntity asignacion;

    private String pdf;

    @NotBlank(message = "El tipo de lista es obligatorio")
    private String tipo;

    @NotNull(message = "La fecha de registro es obligatoria")
    private Date fecha_registro;

    private boolean estado; // true = valido || false = invalido

    // Métodos personalizados

    @JsonProperty("asignacionID")
    public Integer getAsignacionID() {
        return asignacion != null ? asignacion.getId() : null;
    }

    @JsonIgnore
    public AsignacionEntity getAsignacion() {
        return asignacion;
    }
}
