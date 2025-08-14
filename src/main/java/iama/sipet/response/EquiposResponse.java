package iama.sipet.response;

import iama.sipet.entity.EquipoTecnologicoEntity;

import java.util.List;

public class EquiposResponse {
    private List<EquipoTecnologicoEntity> equipo;

    public List<EquipoTecnologicoEntity> getEquipo() {
        return equipo;
    }

    public void setEquipo(List<EquipoTecnologicoEntity> equipo) {
        this.equipo = equipo;
    }
}
