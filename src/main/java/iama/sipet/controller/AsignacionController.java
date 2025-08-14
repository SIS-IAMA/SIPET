package iama.sipet.controller;

import java.io.IOException;

import iama.sipet.entity.AsignacionEntity;
import iama.sipet.entity.ListaEquiposEntity;
import iama.sipet.response.AsignacionResponseRest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.ResponseEntity;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import iama.sipet.service.IAsignacionService;

// Este controller contiene los endpoint principales realcionados a las asignaciones
@EnableMethodSecurity(prePostEnabled = true)
@Configuration
@RestController
@RequestMapping("/api/asignacion")
public class AsignacionController {
    @Autowired
    private IAsignacionService asignacionService;

    //Crear una asignacion
    @PostMapping
    public ResponseEntity<AsignacionResponseRest> crear(@RequestPart AsignacionEntity asignacion, @RequestPart MultipartFile file) throws IOException {
        ResponseEntity<AsignacionResponseRest> response = asignacionService.create(asignacion, file);
        return response;
    }

    // Actualizar una asignacion que tiene relacion con una peticion
    @PutMapping("/{id}/{idPeticion}")
    public ResponseEntity<AsignacionResponseRest> update(@PathVariable Integer id,@PathVariable Integer idPeticion, @RequestPart AsignacionEntity asignacion, @RequestPart MultipartFile file) throws IOException {
        ResponseEntity<AsignacionResponseRest> response = asignacionService.update(id, idPeticion, asignacion, file);
        return response;
    }

    // Actualizar los datos de una asignacion activa
    @PutMapping("/{id}")
    public ResponseEntity<AsignacionResponseRest> updateData(@PathVariable Integer id, @RequestPart ListaEquiposEntity listaEquipos, @RequestPart AsignacionEntity asignacion, @RequestPart MultipartFile file) throws IOException {
        ResponseEntity<AsignacionResponseRest> response = asignacionService.updateData(id, asignacion, listaEquipos, file);
        return response;
    }

    // Actualizar la responsiva de una asignacion
    @PutMapping("/firma/{id}/{idPeticion}")
    public ResponseEntity<AsignacionResponseRest> updateResponsiva(@PathVariable Integer id, @PathVariable Integer idPeticion, @RequestPart MultipartFile file) throws IOException {
        ResponseEntity<AsignacionResponseRest> response = asignacionService.updateResponsiva(id, idPeticion, file);
        return response;
    }

    // Deshacer una asignacion
    @DeleteMapping("/{id}")
    public ResponseEntity<AsignacionResponseRest> delete(@PathVariable Integer id)  {
        ResponseEntity<AsignacionResponseRest> response = asignacionService.delete(id);
        return response;
    }

    // Obtener todas las asignaciones
    @GetMapping
    public ResponseEntity<AsignacionResponseRest> findAll()  {
        ResponseEntity<AsignacionResponseRest> response = asignacionService.getAll();
        return response;
    }

    // Obtener una asignacion por su id
    @GetMapping("/{id}")
    public ResponseEntity<AsignacionResponseRest> findById(@PathVariable Integer id)  {
        ResponseEntity<AsignacionResponseRest> response = asignacionService.getById(id);
        return response;
    }

}
