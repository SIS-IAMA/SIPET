package iama.sipet.response;

import java.util.List;

import iama.sipet.entity.AsignacionEntity;

public class AsignacionResponse {
    private List<AsignacionEntity> asignacion;

    public List<AsignacionEntity> getAsignacion() {
        return asignacion;
    }

    public void setAsignacion(List<AsignacionEntity> asignacion) {
        this.asignacion = asignacion;
    }
}
