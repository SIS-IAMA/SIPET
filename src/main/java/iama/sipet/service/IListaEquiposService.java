package iama.sipet.service;

import iama.sipet.entity.ListaEquiposEntity;
import iama.sipet.response.ListaEquipoResponseRest;
import java.io.IOException;

import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;

public interface IListaEquiposService {
    public ResponseEntity<ListaEquipoResponseRest> findAll();

    public ResponseEntity<ListaEquipoResponseRest> findById(Integer id);

    public ResponseEntity<ListaEquipoResponseRest> update(Integer id, Integer idPeticion, ListaEquiposEntity listaEquiposEntity, MultipartFile file) throws IOException;

    public ResponseEntity<ListaEquipoResponseRest> deshacer (Integer id);
    
    public void changeEstadoEquipos(Integer id);

    }