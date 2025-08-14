package iama.sipet.service;

import java.io.IOException;

import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;

import iama.sipet.entity.PeticionesEntity;
import iama.sipet.response.PeticionesResponseRest;

public interface IPeticionService {
    public Boolean create(PeticionesEntity peticionesEntity, MultipartFile file) throws IOException;
    public ResponseEntity<PeticionesResponseRest> gestionar(Integer id, Boolean estado, PeticionesEntity comentarioEntity);
    public Boolean upload(Integer id, String file) throws IOException;
    public ResponseEntity<PeticionesResponseRest> delete(Integer id);
    public ResponseEntity<PeticionesResponseRest> getAll();
    public ResponseEntity<PeticionesResponseRest> getById(Integer id);
    public ResponseEntity<PeticionesResponseRest> getByUser(String user);

}
