package iama.sipet.response;

import iama.sipet.entity.EmpleadoEntity;

import java.util.List;

public class EmpleadoResponse {
    private List<EmpleadoEntity> empleado;

    public List<EmpleadoEntity> getEmpleado() {
        return empleado;
    }

    public void setEmpleado(List<EmpleadoEntity> empleado) {
        this.empleado = empleado;
    }
}