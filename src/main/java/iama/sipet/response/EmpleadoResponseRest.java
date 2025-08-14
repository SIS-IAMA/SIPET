package iama.sipet.response;

public class EmpleadoResponseRest extends  ResponseRest {
    private EmpleadoResponse empleadoResponse = new EmpleadoResponse();

    public EmpleadoResponse getEmpleadoResponse() {
        return empleadoResponse;
    }

    public void setEmpleadoResponse(EmpleadoResponse empleadoResponse) {
        this.empleadoResponse = empleadoResponse;
    }
}
