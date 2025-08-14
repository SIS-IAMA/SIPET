package iama.sipet.service;

import iama.sipet.entity.EmpleadoEntity;
import iama.sipet.response.EmpleadoResponseRest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

public interface IEmpleadoService {
    public ResponseEntity<EmpleadoResponseRest> buscarEmpleados();
    public ResponseEntity<EmpleadoResponseRest> buscarPorId(Integer id);
    public ResponseEntity<EmpleadoResponseRest> crear(EmpleadoEntity empleadoEntity, MultipartFile file) throws IOException;
    public ResponseEntity<EmpleadoResponseRest> actualizar( EmpleadoEntity empleadoEntity, Integer id, MultipartFile file) throws IOException;
    public ResponseEntity<EmpleadoResponseRest> eliminar( Integer id);

}
