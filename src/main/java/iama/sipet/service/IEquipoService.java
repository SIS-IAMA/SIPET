package iama.sipet.service;


import iama.sipet.entity.EquipoTecnologicoEntity;
import iama.sipet.response.EquiposResponseRest;
import iama.sipet.response.ListaEquipoResponseRest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

public interface IEquipoService {
    public ResponseEntity<EquiposResponseRest> buscarEquipos();
    public ResponseEntity<EquiposResponseRest> buscarEquiposActivos();
    public ResponseEntity<EquiposResponseRest> buscarEquiposPorIds(List<Integer> id);
    public ResponseEntity<EquiposResponseRest> buscarPorId(Integer id);
    public ResponseEntity<EquiposResponseRest> crear(EquipoTecnologicoEntity equipoTecnologicoEntity, MultipartFile file) throws IOException;
    public ResponseEntity<EquiposResponseRest> actualizar( EquipoTecnologicoEntity equipoTecnologicoEntity, Integer id, MultipartFile file) throws IOException;
    public ResponseEntity<ListaEquipoResponseRest> listar(List<Integer> id, String tipo, MultipartFile file) throws IOException;
}
