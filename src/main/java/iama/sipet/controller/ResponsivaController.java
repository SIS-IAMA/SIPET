package iama.sipet.controller;

import java.io.IOException;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import iama.sipet.entity.ResponsivaEntity;
import iama.sipet.service.IResponsivaService;

@RestController
@RequestMapping("/api/responsivas")
public class ResponsivaController {
    @Autowired
    private IResponsivaService responsivaService;

    //Guardar Responsiva
    @PostMapping
    public ResponseEntity<ResponsivaEntity> saveResponsiva(@RequestPart MultipartFile file) throws IOException {
        return new ResponseEntity<>(responsivaService.save(file), HttpStatus.CREATED);
    }

    //Obtener Responsivas
    @GetMapping
    public ResponseEntity<List<ResponsivaEntity>> getAllResponsivas() {
        List<ResponsivaEntity> responsivas = responsivaService.findAll();
        return new ResponseEntity<>(responsivas, HttpStatus.OK);
    }

    //Obtener Responsiva por ID
    @GetMapping("/{id}")
    public ResponseEntity<ResponsivaEntity> getResponsivaById(@PathVariable Integer id) {
        ResponsivaEntity responsiva = responsivaService.findById(id);
        return new ResponseEntity<>(responsiva, HttpStatus.OK);
    }

    //Actualizar Responsiva
    @PutMapping("/{id}")
    public ResponseEntity<ResponsivaEntity> updateResponsiva(@PathVariable Integer id, @RequestPart MultipartFile file) throws IOException {
        return new ResponseEntity<>(responsivaService.update(id, file), HttpStatus.OK);
    }

    //Eliminar Responsiva
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteResponsiva(@PathVariable Integer id) {
        responsivaService.deleteById(id);
        return new ResponseEntity<>(HttpStatus.OK);
    }
}
