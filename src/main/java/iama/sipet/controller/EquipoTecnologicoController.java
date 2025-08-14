package iama.sipet.controller;

import iama.sipet.response.ListaEquipoResponseRest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.UrlResource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Objects;
import java.util.List;

import iama.sipet.entity.EquipoTecnologicoEntity;
import iama.sipet.response.EquiposResponseRest;
import iama.sipet.service.IEquipoService;
import jakarta.annotation.Resource;


@RestController
@RequestMapping("/api/equipos")
public class EquipoTecnologicoController {
    @Autowired
    private IEquipoService equipoService;

    @GetMapping
    public ResponseEntity<EquiposResponseRest> buscarEquipos() {
        ResponseEntity<EquiposResponseRest> response = equipoService.buscarEquipos();
        return response;
    }

    @GetMapping("/activos")
    public ResponseEntity<EquiposResponseRest> buscarEquiposActivos() {
        ResponseEntity<EquiposResponseRest> response = equipoService.buscarEquiposActivos();
        return response;
    }

    @GetMapping("/seleccionados/{id}")
    public ResponseEntity<EquiposResponseRest> buscarEquiposPorIds(@PathVariable List<Integer> id) {
        ResponseEntity<EquiposResponseRest> response = equipoService.buscarEquiposPorIds(id);
        return response;
    }

    @GetMapping("/{id}")
    public ResponseEntity<EquiposResponseRest> buscarPorId(@PathVariable Integer id) {
        ResponseEntity<EquiposResponseRest> response = equipoService.buscarPorId(id);
        return response;
    }

    @PostMapping
    public ResponseEntity<EquiposResponseRest> crear(@RequestPart EquipoTecnologicoEntity equipoTecnologicoEntity, @RequestPart MultipartFile file) throws IOException {
        ResponseEntity<EquiposResponseRest> response = equipoService.crear(equipoTecnologicoEntity, file);
        return response;
    }

    @PutMapping("/{id}")
    public ResponseEntity<EquiposResponseRest> actualizar(@RequestPart EquipoTecnologicoEntity equipoTecnologicoEntity, @PathVariable Integer id, @RequestPart MultipartFile file) throws IOException {
        ResponseEntity<EquiposResponseRest> response = equipoService.actualizar(equipoTecnologicoEntity, id, file);
        return response;
    }

    @PostMapping("/{tipo}")
    public ResponseEntity<ListaEquipoResponseRest> Listar(@RequestPart List<Integer> id, @PathVariable String tipo, @RequestPart MultipartFile file) throws IOException {
        ResponseEntity<ListaEquipoResponseRest> response = equipoService.listar(id, tipo, file);
        return response;
    }

    
}
