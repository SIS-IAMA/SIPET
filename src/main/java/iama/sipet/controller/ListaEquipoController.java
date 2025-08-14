package iama.sipet.controller;

import java.io.IOException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import iama.sipet.entity.ListaEquiposEntity;
import iama.sipet.response.ListaEquipoResponseRest;
import iama.sipet.service.IListaEquiposService;


@RestController
@RequestMapping("/api/listasEquipos")
public class ListaEquipoController {
    @Autowired
    private IListaEquiposService listaEquiposService;

    @GetMapping
    public ResponseEntity<ListaEquipoResponseRest> findAll(){
        ResponseEntity<ListaEquipoResponseRest> response = listaEquiposService.findAll();
        return response;
    }

    @GetMapping("/{id}")
    public ResponseEntity<ListaEquipoResponseRest> findByID(@PathVariable Integer id){
        ResponseEntity<ListaEquipoResponseRest> response = listaEquiposService.findById(id);
        return response;
    }

    @PutMapping("/{id}/{idPeticion}")
    public ResponseEntity<ListaEquipoResponseRest> update(@PathVariable Integer id, @PathVariable Integer idPeticion, @RequestPart ListaEquiposEntity listaEquiposEntity, @RequestPart MultipartFile file)  throws IOException {
        ResponseEntity<ListaEquipoResponseRest> response = listaEquiposService.update(id, idPeticion, listaEquiposEntity, file);
        return response;
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ListaEquipoResponseRest> delete(@PathVariable Integer id){
        ResponseEntity<ListaEquipoResponseRest> response = listaEquiposService.deshacer(id);
        return response;
    }
    
}
