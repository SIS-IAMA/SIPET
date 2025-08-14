package iama.sipet.service;

import iama.sipet.entity.AsignacionEntity;
import iama.sipet.entity.ListaEquiposEntity;
import iama.sipet.response.AsignacionResponseRest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

public interface IAsignacionService {
    public ResponseEntity<AsignacionResponseRest> create(AsignacionEntity asignacionEntity,
            MultipartFile file) throws IOException;
    public ResponseEntity<AsignacionResponseRest> update(Integer id, Integer idPeticion,
            AsignacionEntity asignacionEntity, MultipartFile file) throws IOException;
    public ResponseEntity<AsignacionResponseRest> updateData(Integer id, AsignacionEntity asignacionEntity,
            ListaEquiposEntity listaEquiposEntity, MultipartFile file) throws IOException;
    public ResponseEntity<AsignacionResponseRest> updateResponsiva(Integer id, Integer idPeticion,
            MultipartFile file) throws IOException;
    public ResponseEntity<AsignacionResponseRest> delete(Integer id);
    public ResponseEntity<AsignacionResponseRest> getAll();
    public ResponseEntity<AsignacionResponseRest> getById(Integer id);
}





