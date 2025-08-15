package iama.sipet.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.Base64;
import java.util.Date;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

@Entity
@Table(name = "peticiones")
@Data
public class PeticionesEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne
    @JoinColumn(name = "usuarios")
    @NotNull(message = "El usuario es obligatorio")
    private UserEntity user;

    @NotNull(message = "El id de entidad es obligatorio")
    private Integer id_entidad;

    @NotBlank(message = "El tipo de petición es obligatorio")
    private String tipo_peticion;

    @NotBlank(message = "La categoría es obligatoria")
    private String categoria;

    @Lob
    @Column(name = "pdf", columnDefinition = "LONGBLOB")
    private byte[] pdf;

    @Column(name = "nombre_pdf")
    private String nombrePDF;

    @NotNull(message = "La fecha de registro es obligatoria")
    private Date fecha_registro;

    private String comentario;

    @NotBlank(message = "El estado es obligatorio")
    private String estado;

    // Métodos personalizados

    @Override
    public String toString() {
        return "PeticionesEntity{id=" + id + ", tipo_peticion='" + tipo_peticion + "', userId=" + (user != null ? user.getId() : null) + "}";
    }

    @JsonProperty("operadorId")
    public Integer getOperadorId() {
        return user != null ? user.getId() : null;
    }

    @JsonProperty("operadorName")
    public String getOperadorName() {
        return user != null ? user.getNombre() + " " + user.getApellido_p() + " " + user.getApellido_m() : null;
    }

    @JsonIgnore
    public UserEntity getUser() {
        return user;
    }

    @JsonProperty("pdfBase64")
    public String getPdfBase64() {
        if (this.pdf != null && this.pdf.length > 0) {
            return Base64.getEncoder().encodeToString(this.pdf);
        } else {
            return null;
        }
    }

}
