package iama.sipet.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Entity
@Table(name = "equipo_tecnologico")
@Data
public class EquipoTecnologicoEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne
    @JoinColumn(name = "equipoTecnologico") // FK hacia lista_equipos
    @NotNull(message = "La lista de equipos es obligatoria")
    private ListaEquiposEntity listaEquipos;

    @NotBlank(message = "El tipo es obligatorio")
    private String tipo;

    @NotBlank(message = "El número de serie es obligatorio")
    private String numero_serie;

    @NotBlank(message = "La marca es obligatoria")
    private String marca;

    @NotBlank(message = "El modelo es obligatorio")
    private String modelo;

    private String sistema_operativo;

    @Min(value = 0, message = "La RAM no puede ser negativa")
    private int ram;

    @Min(value = 0, message = "El almacenamiento no puede ser negativo")
    private int almacenamiento;

    private String procesador;
    private String comentario;
    private String foto;

    @NotBlank(message = "El estado es obligatorio")
    private String estado;

    // Métodos personalizados

    @JsonProperty("listaEquiposId")
    public Integer getListaEquiposId() {
        return listaEquipos != null ? listaEquipos.getId() : null;
    }

    @JsonIgnore
    public ListaEquiposEntity getListaEquipos() {
        return listaEquipos;
    }
}
