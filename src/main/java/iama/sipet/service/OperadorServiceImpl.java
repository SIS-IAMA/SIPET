package iama.sipet.service;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.*;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import iama.sipet.entity.UserEntity;
import iama.sipet.repository.UserRepository;
import iama.sipet.response.UserResponseRest;
import org.springframework.web.multipart.MultipartFile;

@Service
public class OperadorServiceImpl implements IOperadorService {
    private static final Logger log = LoggerFactory.getLogger(OperadorServiceImpl.class);

    @Autowired
    private UploadService uploadService;
    @Autowired
    private UserRepository userRepository;

    String url = "https://sipet-iama.onrender.com/upload/FotosOperador/";
    String urlUpload = "upload/FotosOperador/";

    // obtener todos los operadores activos
    @Override
    public ResponseEntity<UserResponseRest> buscarOperadores() {
        log.info("Buscando operadores");
        UserResponseRest response = new UserResponseRest();

        try {
            // Obtener todos los usuarios con rol "OPERADOR" y estado "ACTIVO"
            List<UserEntity> user = userRepository.findOperadoresActivos();

            // Verificar si se encontraron usuarios
            if (user.isEmpty()) {
                log.info("No se encontraron operadores");
                response.setMetada("Respuesta OK", "00", "No se encontraron operadores");
                return new ResponseEntity<UserResponseRest>(response, HttpStatus.NOT_FOUND);
            }

            // Iterar sobre los operadores y agregar sus datos al response
            for (UserEntity operador : user) {
                // Verificar si el operador tiene una foto
                if (operador.getFoto() != null && !operador.getFoto().isEmpty()) {
                    String fotoUrl = url + operador.getFoto();
                    operador.setFoto(fotoUrl);
                } else {
                    operador.setFoto(null); // Si no tiene foto, establecer como null
                }
            }

            response.getUserResponse().setUser(user);
            response.setMetada("Respuesta OK", "00", "Respuesta exitosa");
        } catch (Exception e) {
            response.setMetada("Respuesta FALLIDA", "-1", "Respuesta fallida");
            log.error("Error al buscar usuarios", e);
            e.getStackTrace();
            return new ResponseEntity<UserResponseRest>(response, HttpStatus.INTERNAL_SERVER_ERROR);
        }
        return new ResponseEntity<UserResponseRest>(response, HttpStatus.OK);
    }

    // Buscar un usuario por ID
    @Override
    public ResponseEntity<UserResponseRest> buscarPorId(Integer id) {
        log.info("Buscar por ID");
        UserResponseRest response = new UserResponseRest();
        List<UserEntity> list = new ArrayList<>();
        try {
            Optional<UserEntity> user = userRepository.findById(id);

            // Verificar si el usuario existe
            if (user.isPresent()) {

                UserEntity userEntity = user.get();

                if (userEntity.getFoto() == null || userEntity.getFoto().isEmpty()) {
                    userEntity.setFoto(null);
                } else {
                    userEntity.setFoto(url + userEntity.getFoto());
                }

                list.add(userEntity);
                response.getUserResponse().setUser(list);
                response.setMetada("Respuesta OK", "00", "Respuesta exitosa");
            } else {
                log.info("No se encontro el usuario con ID: {}", id);
                response.setMetada("Respuesta no encontrada", "-1", "usuario no encontrado");
                return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
            }
        } catch (Exception e) {
            response.setMetada("Respuesta FALLIDA", "-1", "Respuesta fallida");
            log.error("Error al buscar users", e);
            e.getStackTrace();
            return new ResponseEntity<UserResponseRest>(response, HttpStatus.INTERNAL_SERVER_ERROR);
        }
        return new ResponseEntity<UserResponseRest>(response, HttpStatus.OK);
    }

    // Crear un nuevo usuario operador
    @Override
    public ResponseEntity<UserResponseRest> crear(UserEntity userEntity, MultipartFile file) throws IOException {
        log.info("Crear nuevo operador");
        String encode = URLEncoder.encode(Objects.requireNonNull(file.getOriginalFilename()), StandardCharsets.UTF_8)
                .replace("+", "-");

        UserResponseRest response = new UserResponseRest();
        List<UserEntity> list = new ArrayList<>();
        try {
            // Verificar si hay algun campo nulo o vacío
            if (userEntity.getUsername() == null || userEntity.getUsername().isEmpty() ||
                    userEntity.getNombre() == null || userEntity.getNombre().isEmpty() ||
                    userEntity.getApellido_p() == null || userEntity.getApellido_p().isEmpty() ||
                    userEntity.getApellido_m() == null || userEntity.getApellido_m().isEmpty()) {
                list.add(userEntity);
                response.getUserResponse().setUser(list);
                response.setMetada("Respuesta FALLIDA", "-1", "No se permiten campos vacíos");
                return new ResponseEntity<UserResponseRest>(response, HttpStatus.CONFLICT);
            }

            // Verificar si el usuario ya existe
            if (userRepository.findByUsername(userEntity.getUsername()).isPresent()) {
                list.add(userEntity);
                response.getUserResponse().setUser(list);
                response.setMetada("Respuesta FALLIDA", "-1", "Ya existe un operador con ese nombre de usuario");
                return new ResponseEntity<UserResponseRest>(response, HttpStatus.CONFLICT);
            }

            // Verificar foto
            if (file != null && !file.isEmpty()) {
                // Guardar la nueva foto y actualizar el usuario
                String name = uploadService.saveUpload(file, urlUpload);
                userEntity.setFoto(name);
            }

            // Encriptar la contraseña
            BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
            String contrasenaEncriptada = passwordEncoder.encode("12345");

            // Guardar la contraseña encriptada
            userEntity.setPassword(contrasenaEncriptada);

            // setear rol de operador true=administrador false=operador
            userEntity.setRol(false);

            // Establecer la fecha de registro
            userEntity.setFecha_registro(new Date());

            // Establecer el estado del usuario como ACTIVO
            userEntity.setEstado(true);

            // Guardar el usuario
            UserEntity userGuardar = userRepository.save(userEntity);

            list.add(userGuardar);
            response.getUserResponse().setUser(list);
            response.setMetada("Respuesta OK", "00", "Creacion Exitosa");
        } catch (Exception e) {
            response.setMetada("Respuesta FALLIDA", "-1", "Error al crear al operador");
            log.error("Error al guardar operador", e);
            e.getStackTrace();
            return new ResponseEntity<UserResponseRest>(response, HttpStatus.INTERNAL_SERVER_ERROR);
        }
        return new ResponseEntity<UserResponseRest>(response, HttpStatus.OK);
    }

    // Actualizar un usuario
    @Override
    public ResponseEntity<UserResponseRest> actualizar(UserEntity userEntity, Integer id, MultipartFile file)
            throws IOException {
        log.info("Actualizar usuario con ID: {}", id);
        String encode = URLEncoder.encode(Objects.requireNonNull(file.getOriginalFilename()), StandardCharsets.UTF_8)
                .replace("+", "-");
        UserResponseRest response = new UserResponseRest();
        List<UserEntity> list = new ArrayList<>();
        try {
            Optional<UserEntity> userOptional = userRepository.findById(id);
            if (userOptional.isPresent()) {
                UserEntity existingUser = userOptional.get();

                // Verificar que al menos un campo recibido sea diferente al existente
                boolean campoDiferente = false;

                // Verificar que el usuario sea diferente al que se está actualizando
                if (userEntity.getUsername() != null && !userEntity.getUsername().isEmpty()
                        && !existingUser.getUsername().equals(userEntity.getUsername())) {

                    // Verificar si el nuevo nombre de usuario ya existe
                    if (userRepository.findByUsername(userEntity.getUsername()).isPresent()) {
                        response.setMetada("Respuesta FALLIDA", "-1", "Ya existe un usuario con ese nombre de usuario");
                        return new ResponseEntity<UserResponseRest>(response, HttpStatus.CONFLICT);
                    }

                    // Verificar si el usuario es ADMIN
                    if (!existingUser.getRol()) {
                        response.setMetada("Respuesta FALLIDA", "-1",
                                "Solo los usuarios con rol ADMIN pueden cambiar el nombre de usuario");
                        return new ResponseEntity<UserResponseRest>(response, HttpStatus.CONFLICT);
                    }

                    // Verificar que el nuevo usuario tenga un formato de correo electornico
                    if (!userEntity.getUsername().matches("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Z|a-z]{2,}$")) {
                        response.setMetada("Respuesta FALLIDA", "-1",
                                "El nombre de usuario debe ser un correo electrónico válido");
                        return new ResponseEntity<UserResponseRest>(response, HttpStatus.CONFLICT);
                    }

                    existingUser.setUsername(userEntity.getUsername());
                    campoDiferente = true;
                }

                // Verificar nombre
                if (userEntity.getNombre() != null && !userEntity.getNombre().isEmpty()
                        && !existingUser.getNombre().equals(userEntity.getNombre())) {
                    existingUser.setNombre(userEntity.getNombre());
                    campoDiferente = true;
                }

                // Verificar apellido paterno
                if (userEntity.getApellido_p() != null && !userEntity.getApellido_p().isEmpty()
                        && !existingUser.getApellido_p().equals(userEntity.getApellido_p())) {
                    existingUser.setApellido_p(userEntity.getApellido_p());
                    campoDiferente = true;
                }

                // Verificar apellido materno
                if (userEntity.getApellido_m() != null && !userEntity.getApellido_m().isEmpty()
                        && !existingUser.getApellido_m().equals(userEntity.getApellido_m())) {
                    existingUser.setApellido_m(userEntity.getApellido_m());
                    campoDiferente = true;
                }

                // Verificar foto
                if ((encode != null && !encode.isEmpty()) && (file != null && !file.isEmpty())) {
                    log.info("entramos");
                    // Si hay un nuevo archivo, eliminar la foto anterior si existe
                    if (existingUser.getFoto() != null && !existingUser.getFoto().isEmpty()) {
                        uploadService.deleteUpload(existingUser.getFoto(), urlUpload);
                    }

                    // Guardar la nueva foto y actualizar el usuario
                    String name = uploadService.saveUpload(file, urlUpload);
                    existingUser.setFoto(name);
                    campoDiferente = true;

                }

                if (campoDiferente == false) {
                    response.setMetada("Respuesta FALLIDA", "-1", "No se han realizado cambios en el usuario");
                    return new ResponseEntity<UserResponseRest>(response, HttpStatus.CONFLICT);
                }

                // Datos que se pueden actualizar dependiendo del rol
                // Operador Nombre, Apellidos, Foto
                // Admin Nombre, Apellidos, Foto, Usuario
                UserEntity updatedUser = userRepository.save(existingUser);
                list.add(updatedUser);
                response.getUserResponse().setUser(list);
                response.setMetada("Respuesta OK", "00", "Actualización exitosa");
            } else {
                response.setMetada("Respuesta no encontrada", "-1", "Usuario no encontrado");
                return new ResponseEntity<UserResponseRest>(response, HttpStatus.NOT_FOUND);
            }
        } catch (Exception e) {
            response.setMetada("Respuesta FALLIDA", "-1", "Error al actualizar el usuario");
            log.error("Error al actualizar el usuario", e);
            return new ResponseEntity<UserResponseRest>(response, HttpStatus.INTERNAL_SERVER_ERROR);
        }
        return new ResponseEntity<UserResponseRest>(response, HttpStatus.OK);
    }

    @Override
    public ResponseEntity<UserResponseRest> eliminar(Integer id) {
        log.info("Eliminar usuario con ID: {}", id);
        UserResponseRest response = new UserResponseRest();
        try {
            // Buscar usuario por id
            Optional<UserEntity> userOptional = userRepository.findByid(id);
            if (userOptional.isPresent()) {
                UserEntity userExisting = userOptional.get();

                // Cambiar el estado del operador a INACTIVO
                userExisting.setEstado(false);

                userRepository.save(userExisting);

                response.setMetada("Respuesta OK", "00", "Usuario deshabilitado exitosamente");
            } else {
                response.setMetada("Respuesta no encontrada", "-1", "Usuario no encontrado");
                return new ResponseEntity<UserResponseRest>(response, HttpStatus.NOT_FOUND);
            }
        } catch (Exception e) {
            response.setMetada("Respuesta FALLIDA", "-1", "Error al eliminar el usuario");
            log.error("Error al eliminar el usuario", e);
            e.getStackTrace();
            return new ResponseEntity<UserResponseRest>(response, HttpStatus.INTERNAL_SERVER_ERROR);
        }
        return new ResponseEntity<UserResponseRest>(response, HttpStatus.OK);
    }

    @Override
    public ResponseEntity<UserResponseRest> updatePassword(String username, String password) {
        log.info("Actualizar contraseña del usuario: {}", username, password);
        UserResponseRest response = new UserResponseRest();
        List<UserEntity> list = new ArrayList<>();
        try {
            Optional<UserEntity> userOptional = userRepository.findByUsername(username);

            // Verificar si el usuario existe
            if (userOptional.isPresent()) {
                UserEntity existingUser = userOptional.get();
                // Verificar que la nueva contraseña no sea nula o vacía
                if (password == null || password.isEmpty()) {
                    response.setMetada("Respuesta FALLIDA", "-1", "La nueva contraseña no puede ser nula o vacía");
                    return new ResponseEntity<UserResponseRest>(response, HttpStatus.CONFLICT);
                }

                // Verificar que la nueva contraseña sea diferente a la actual
                BCryptPasswordEncoder passwordEncoderV = new BCryptPasswordEncoder();
                if (passwordEncoderV.matches(password, existingUser.getPassword())) {
                    response.setMetada("Respuesta FALLIDA", "-1", "La nueva contraseña no puede ser igual a la actual");
                    return new ResponseEntity<UserResponseRest>(response, HttpStatus.CONFLICT);
                }

                // Encriptar la nueva contraseña
                BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
                String contrasenaEncriptada = passwordEncoder.encode(password);
                existingUser.setPassword(contrasenaEncriptada);

                // Guardar el usuario con la nueva contraseña
                UserEntity updatedUser = userRepository.save(existingUser);

                list.add(updatedUser);
                response.getUserResponse().setUser(list);
                response.setMetada("Respuesta OK", "00", "Contraseña actualizada exitosamente");
            } else {
                response.setMetada("Respuesta no encontrada", "-1", "Usuario no encontrado");
                return new ResponseEntity<UserResponseRest>(response, HttpStatus.NOT_FOUND);
            }
        } catch (Exception e) {
            response.setMetada("Respuesta FALLIDA", "-1", "Error al actualizar la contraseña");
            log.error("Error al actualizar la contraseña", e);
            return new ResponseEntity<UserResponseRest>(response, HttpStatus.INTERNAL_SERVER_ERROR);
        }
        return new ResponseEntity<UserResponseRest>(response, HttpStatus.OK);
    }

    @Override
    public ResponseEntity<UserResponseRest> updateUser(String username, String user) {
        log.info("Actualizar username del usuario: {}", username, user);
        UserResponseRest response = new UserResponseRest();
        List<UserEntity> list = new ArrayList<>();
        try {
            Optional<UserEntity> userOptional = userRepository.findByUsername(username);

            // Verificar si el usuario existe
            if (userOptional.isPresent()) {
                UserEntity existingUser = userOptional.get();
                // Verificar que el usuario sea diferente al que se está actualizando
                if (user == null && user.isEmpty() && existingUser.getUsername().equals(user)) {
                    response.setMetada("Respuesta FALLIDA", "-1", "El nombre esta vacio o es el mismo");
                    return new ResponseEntity<UserResponseRest>(response, HttpStatus.CONFLICT);
                }
                // Verificar que el nuevo username tenga un formato de correo electornico
                if (!user.matches("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Z|a-z]{2,}$")) {
                    response.setMetada("Respuesta FALLIDA", "-1", "El nombre de usuario debe ser un correo electrónico válido");
                    return new ResponseEntity<UserResponseRest>(response, HttpStatus.CONFLICT);
                }

                // Verificar si el nuevo nombre de usuario ya existe
                if (userRepository.findByUsername(user).isPresent()) {
                    response.setMetada("Respuesta FALLIDA", "-1", "Ya existe un usuario con ese nombre de usuario");
                    return new ResponseEntity<UserResponseRest>(response, HttpStatus.CONFLICT);
                }

                // Verificar si el usuario es ADMIN
                if (!existingUser.getRol()) {
                    response.setMetada("Respuesta FALLIDA", "-1",
                            "Solo los usuarios con rol ADMIN pueden cambiar el nombre de usuario");
                    return new ResponseEntity<UserResponseRest>(response, HttpStatus.CONFLICT);
                }

                existingUser.setUsername(user);
                // Guardar el usuario con el nuevo usuario
                UserEntity updatedUser = userRepository.save(existingUser);

                list.add(updatedUser);
                response.getUserResponse().setUser(list);
                response.setMetada("Respuesta OK", "00", "Usuario actualizado exitosamente");
            } else {
                response.setMetada("Respuesta no encontrada", "-1", "Usuario no encontrado");
                return new ResponseEntity<UserResponseRest>(response, HttpStatus.NOT_FOUND);
            }
        } catch (Exception e) {
            response.setMetada("Respuesta FALLIDA", "-1", "Error al actualizar el usuario");
            log.error("Error al actualizar el usuario", e);
            return new ResponseEntity<UserResponseRest>(response, HttpStatus.INTERNAL_SERVER_ERROR);
        }
        return new ResponseEntity<UserResponseRest>(response, HttpStatus.OK);
    }

    @Override
    public ResponseEntity<UserResponseRest> buscarUsuario(String user) {
        log.info("Obteniendo usuario");
        UserResponseRest response = new UserResponseRest();
        List<UserEntity> list = new ArrayList<>();
        try {
            Optional<UserEntity> userOptional = userRepository.findByUsername(user);

            // Verificar si el usuario existe
            if (userOptional.isPresent()) {

                UserEntity userEntity = userOptional.get();

                if (userEntity.getFoto() == null || userEntity.getFoto().isEmpty()) {
                    userEntity.setFoto(null);
                } else {
                    userEntity.setFoto(url + userEntity.getFoto());
                }

                list.add(userEntity);
                response.getUserResponse().setUser(list);
                response.setMetada("Respuesta OK", "00", "Respuesta exitosa");
            } else {
                log.info("No se encontro el usuario con ID: {}", user);
                response.setMetada("Respuesta no encontrada", "-1", "usuario no encontrado");
                return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
            }
        } catch (Exception e) {
            response.setMetada("Respuesta FALLIDA", "-1", "Respuesta fallida");
            log.error("Error al buscar users", e);
            e.getStackTrace();
            return new ResponseEntity<UserResponseRest>(response, HttpStatus.INTERNAL_SERVER_ERROR);
        }
        return new ResponseEntity<UserResponseRest>(response, HttpStatus.OK);
    }

}
