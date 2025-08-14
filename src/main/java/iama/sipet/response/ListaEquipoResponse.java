package iama.sipet.response;

import iama.sipet.entity.ListaEquiposEntity;

import java.util.List;

public class ListaEquipoResponse {
    private List<ListaEquiposEntity> listaEquipos;

    public List<ListaEquiposEntity> getListaEquipos() {
        return listaEquipos;
    }

    public void setListaEquipos(List<ListaEquiposEntity> listaEquipos) {
        this.listaEquipos = listaEquipos;
    }
}
