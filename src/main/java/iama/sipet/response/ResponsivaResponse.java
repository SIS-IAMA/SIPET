package iama.sipet.response;

import iama.sipet.entity.ResponsivaEntity;

import java.util.List;

public class ResponsivaResponse {
    private List<ResponsivaEntity> responsiva;

    public List<ResponsivaEntity> getResponsiva() {
        return responsiva;
    }

    public void setResponsiva(List<ResponsivaEntity> responsiva) {
        this.responsiva = responsiva;
    }
}