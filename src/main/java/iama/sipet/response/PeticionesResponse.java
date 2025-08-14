package iama.sipet.response;

import java.util.List;

import iama.sipet.entity.PeticionesEntity;

public class PeticionesResponse {
    private List<PeticionesEntity> peticion;

    public List<PeticionesEntity> getPeticion() {
        return peticion;
    }

    public void setPeticion(List<PeticionesEntity> peticion) {
        this.peticion = peticion;
    }
}
