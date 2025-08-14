package iama.sipet.entity;

import java.util.Date;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Entity
@Table(name = "asignacionHistory")
@Data
public class AsignacionHistoryEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @OneToOne
    @JoinColumn(name = "id_asignacion_history")
    @NotNull(message = "Una asignacionHistory debe tener una asignacion activa como referencia")
    @JsonIgnore
    private AsignacionEntity asignacion;

    // Ids de equipos separados por comas Ejemplo: "1,3,5,8"
    private String idEquipos;

    // Id de la responsiva
    private Integer idResponsivaActiva;

    // Ubicacion
    private String ubicacion;

    // Ultima fecha
    private Date fecha_registro;

    public List<Integer> getIdEquiposList() {
        if (idEquipos == null || idEquipos.isEmpty()){
            return List.of();
        }else {
            String[] parts = idEquipos.split(",");
            return java.util.Arrays.stream(parts)
                    .map(String::trim)
                    .map(Integer::parseInt)
                    .toList();
        }
    }

    @Transient
    public void setIdEquiposList(List<Integer> ids) {
        this.idEquipos = ids.stream().map(String::valueOf).reduce((a, b) -> a + "," + b).orElse("");
    }

    @JsonProperty("asignacionID")
    public Integer getAsignacionID() {
        return asignacion != null ? asignacion.getId() : null;
    }

}
