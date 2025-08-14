package iama.sipet.response;

public class PeticionesResponseRest extends ResponseRest {
    private PeticionesResponse peticionesResponse = new PeticionesResponse();

    public PeticionesResponse getPeticionesResponse() {
        return peticionesResponse;
    }

    public void setPeticionesResponse(PeticionesResponse peticionesResponse) {
        this.peticionesResponse = peticionesResponse;
    }
}
