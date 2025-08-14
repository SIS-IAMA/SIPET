package iama.sipet.controller;

import iama.sipet.response.EmpleadoResponseRest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.web.bind.annotation.*;

import iama.sipet.service.IEmpleadoService;
import iama.sipet.entity.EmpleadoEntity;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@EnableMethodSecurity(prePostEnabled = true)
@Configuration
@RestController
@RequestMapping("/api/empleado")
public class EmpleadoController {
    @Autowired
    private IEmpleadoService empleadoService;

    //Crear empleado
    @PostMapping
    public ResponseEntity<EmpleadoResponseRest> crear(@RequestPart EmpleadoEntity empleadoEntity, @RequestPart MultipartFile file) throws IOException {
        ResponseEntity<EmpleadoResponseRest> response = empleadoService.crear(empleadoEntity, file);
        return response;
    }

    //Obtener empleado por ID
    @GetMapping("/{id}")
    public ResponseEntity<EmpleadoResponseRest> buscarPorId(@PathVariable Integer id) {
        ResponseEntity<EmpleadoResponseRest> response = empleadoService.buscarPorId(id);
        return response;
    }

    //Obtener empleados
    @GetMapping
    public ResponseEntity<EmpleadoResponseRest> buscarEmpleados() {
        ResponseEntity<EmpleadoResponseRest> response = empleadoService.buscarEmpleados();
        return response;
    }

    //Actualizar empleado
    @PutMapping("/{id}")
    public ResponseEntity<EmpleadoResponseRest> actualizarEmpleado(@PathVariable Integer id, @RequestPart EmpleadoEntity empleadoEntity, @RequestPart MultipartFile file) throws IOException {
        ResponseEntity<EmpleadoResponseRest> response = empleadoService.actualizar(empleadoEntity, id, file);
        return response;
    }

    //Deshabilitar empleado
    @DeleteMapping("/deshabilitar/{id}")
    public ResponseEntity<EmpleadoResponseRest> eliminarEmpleado(@PathVariable Integer id) {
        ResponseEntity<EmpleadoResponseRest> response = empleadoService.eliminar(id);
        return response;
    }

}
