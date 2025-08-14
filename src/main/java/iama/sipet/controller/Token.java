package iama.sipet.controller;

import iama.sipet.entity.UserEntity;
import iama.sipet.repository.UserRepository;
import iama.sipet.response.UserResponseRest;

import iama.sipet.service.IMailService;
import iama.sipet.service.IOperadorService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.web.bind.annotation.*;

import iama.sipet.security.JwtTokenProvider;

import java.util.Optional;

@RestController
@RequestMapping("/auth")
public class Token {
    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    @Autowired
    private IOperadorService operadorService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private IMailService mailService;


    @GetMapping("/login")
    public String generateToken(@RequestParam String username, @RequestParam String password) {
        Optional<UserEntity> user = userRepository.findByUsername(username);
        if (user.isEmpty()) {
            throw new RuntimeException("Usuario no encontrado");
        }

        // Obtener estado del usuario
        Boolean estado = user.get().getEstado();
        if (!estado) {
            throw new RuntimeException("Usuario inactivo");
        }

        // Autenticación del usuario
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(username, password));
        return jwtTokenProvider.generateToken(username, user.get().getRol() ? "ADMIN" : "OPERADOR");
    }

    // Crear Token para recuperar contraseña
    @GetMapping("/{User}")
    public ResponseEntity<UserResponseRest> createToken(@PathVariable String User) {
        ResponseEntity<UserResponseRest> response = mailService.createToken(User);
        return response;
    }

    // Comprobar token para recupera contraseña
    @GetMapping("/token/{Token}")
    public ResponseEntity<UserResponseRest> checkToken(@PathVariable String Token) {
        ResponseEntity<UserResponseRest> response = mailService.checkToken(Token);
        return response;
    }


    @GetMapping("/buscar/{user}")
    public ResponseEntity<UserResponseRest> buscarUsuario(@PathVariable String user) {
        ResponseEntity<UserResponseRest> response = operadorService.buscarUsuario(user);
        return response;
    }
}
