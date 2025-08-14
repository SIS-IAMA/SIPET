package iama.sipet.controller;

import java.io.IOException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.web.bind.annotation.*;

import iama.sipet.service.OperadorServiceImpl;
import iama.sipet.entity.UserEntity;
import iama.sipet.response.UserResponseRest;
import org.springframework.web.multipart.MultipartFile;

@EnableMethodSecurity(prePostEnabled = true)
@Configuration
@RestController
@RequestMapping("/usuarios")
public class UserController {
    @Autowired
    private OperadorServiceImpl operadorService;
       
    //Operador ----------------------------------------
    //Crear operador
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping(consumes = "multipart/form-data")
    public ResponseEntity<UserResponseRest> crearOperador(@RequestPart UserEntity userEntity, @RequestPart MultipartFile file) throws IOException {
        ResponseEntity<UserResponseRest> response = operadorService.crear(userEntity, file);
        return response;
    }
    
    //Obtener operadores
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping ("/operadores")
    public ResponseEntity<UserResponseRest> buscarOperadores() {
        ResponseEntity<UserResponseRest> response = operadorService.buscarOperadores();
        return response;
    }

    //Obtener operadores por id
    @GetMapping("/{id}")
    public ResponseEntity<UserResponseRest> buscarOperadorPorId(@PathVariable Integer id) {
        ResponseEntity<UserResponseRest> response = operadorService.buscarPorId(id);
        return response;
    }
    
    //Actualizar usuario
    @PutMapping("/{id}")
    public ResponseEntity<UserResponseRest> actualizarUsuario(@PathVariable Integer id, @RequestPart UserEntity userEntity, @RequestPart MultipartFile file) throws IOException {
        ResponseEntity<UserResponseRest> response = operadorService.actualizar(userEntity, id, file);
        return response;
    }

    //Deshabilitar operador
    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/deshabilitar/{id}")
    public ResponseEntity<UserResponseRest> eliminarOperador(@PathVariable Integer id) {
        ResponseEntity<UserResponseRest> response = operadorService.eliminar(id);
        return response;
    }

    //Actualizar contraseña
    @PutMapping("/password/{username}/{password}")
    public ResponseEntity<UserResponseRest> updatePassword(@PathVariable String username, @PathVariable String password) {
        ResponseEntity<UserResponseRest> response = operadorService.updatePassword(username, password);
        return response;
    }

    //Actualizar username
    @PutMapping("/user/{username}/{user}")
    public ResponseEntity<UserResponseRest> updateUser(@PathVariable String username, @PathVariable String user) {
        ResponseEntity<UserResponseRest> response = operadorService.updateUser(username, user);
        return response;
    }
}
