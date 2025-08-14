package iama.sipet.event;

import iama.sipet.entity.PeticionesEntity;
import org.springframework.context.ApplicationEvent;

public class PeticionCreadaEvent extends ApplicationEvent {
    private final PeticionesEntity peticion;

    public PeticionCreadaEvent(Object source, PeticionesEntity peticion) {
        super(source);
        this.peticion = peticion;
    }

    public PeticionesEntity getPeticion() {
        return peticion;
    }
}
