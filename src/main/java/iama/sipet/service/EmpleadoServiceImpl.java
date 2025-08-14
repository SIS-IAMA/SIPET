package iama.sipet.service;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.*;

import iama.sipet.entity.ResponsivaEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import iama.sipet.entity.EmpleadoEntity;
import iama.sipet.entity.PeticionesEntity;
import iama.sipet.response.EmpleadoResponseRest;
import iama.sipet.repository.EmpleadoRepository;
import iama.sipet.repository.UserRepository;

import org.springframework.web.multipart.MultipartFile;

@Service
public class EmpleadoServiceImpl implements IEmpleadoService {
    private static final Logger log = LoggerFactory.getLogger(EmpleadoServiceImpl.class);

    @Autowired
    private PeticionesServiceImpl peticionesService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private EmpleadoRepository empleadoRepository;

    @Autowired
    private UploadService uploadService;

    String url = "https://sipet-iama.onrender.com/upload/FotosEmpleado/";
    String urlUpload = "upload/FotosEmpleado/";

    String urlResponsiva = "https://sipet-iama.onrender.com/upload/Responsivas/";

    @Override
    public ResponseEntity<EmpleadoResponseRest> buscarEmpleados() {
        log.info("Buscando empleados");
        EmpleadoResponseRest response = new EmpleadoResponseRest();

        try {
            List<EmpleadoEntity> empleados = empleadoRepository.findActive();

            // Comprobar si la lista de empleados está vacía
            if (empleados.isEmpty()) {
                response.setMetada("Respuesta FALLIDA", "-1", "No se encontraron empleados");
                return new ResponseEntity<EmpleadoResponseRest>(response, HttpStatus.NOT_FOUND);
            }

            for (EmpleadoEntity empleado : empleados) {
                if (empleado.getFoto() != null && !empleado.getFoto().isEmpty()) {
                    empleado.setFoto(url + empleado.getFoto());
                }

                if (empleado.getResponsiva() != null && !empleado.getResponsiva().isEmpty()) {
                    for (ResponsivaEntity responsiva : empleado.getResponsiva()) {
                        if (responsiva.getPdf() != null && !responsiva.getPdf().isEmpty()) {
                            responsiva.setPdf(urlResponsiva + responsiva.getPdf());
                        }
                    }
                }

            }

            response.getEmpleadoResponse().setEmpleado(empleados);
            response.setMetada("Respuesta OK", "00", "Respuesta exitosa");
        } catch (Exception e) {
            response.setMetada("Respuesta FALLIDA", "-1", "Respuesta fallida");
            log.error("Error al buscar empleados", e);
            e.getStackTrace();
            return new ResponseEntity<EmpleadoResponseRest>(response, HttpStatus.INTERNAL_SERVER_ERROR);
        }
        return new ResponseEntity<EmpleadoResponseRest>(response, HttpStatus.OK);
    }

    @Override
    public ResponseEntity<EmpleadoResponseRest> buscarPorId(Integer id) {
        log.info("Buscando empleado con ID: {}", id);
        EmpleadoResponseRest response = new EmpleadoResponseRest();
        try {
            Optional<EmpleadoEntity> empleado = empleadoRepository.findById(id);

            // Comprobar si el empleado existe
            if (empleado.isPresent()) {

                if (empleado.get().getFoto() != null && !empleado.get().getFoto().isEmpty()) {
                    empleado.get().setFoto(url + empleado.get().getFoto());
                }

                if (empleado.get().getResponsiva() != null && !empleado.get().getResponsiva().isEmpty()) {
                    for (ResponsivaEntity responsiva : empleado.get().getResponsiva()) {
                        if (responsiva.getPdf() != null && !responsiva.getPdf().isEmpty()) {
                            responsiva.setPdf(urlResponsiva + responsiva.getPdf());
                        }
                    }
                }

                List<EmpleadoEntity> empleados = new ArrayList<>();
                empleados.add(empleado.get());

                response.getEmpleadoResponse().setEmpleado(empleados);
                response.setMetada("Respuesta OK", "00", "Respuesta exitosa");
            } else {
                response.setMetada("Respuesta FALLIDA", "-1", "Empleado no encontrado");
                return new ResponseEntity<EmpleadoResponseRest>(response, HttpStatus.NOT_FOUND);
            }
        } catch (Exception e) {
            response.setMetada("Respuesta FALLIDA", "-1", "Respuesta fallida");
            log.error("Error al buscar empleados", e);
            e.getStackTrace();
            return new ResponseEntity<EmpleadoResponseRest>(response, HttpStatus.INTERNAL_SERVER_ERROR);
        }
        return new ResponseEntity<EmpleadoResponseRest>(response, HttpStatus.OK);
    }

    @Override
    public ResponseEntity<EmpleadoResponseRest> crear(EmpleadoEntity empleadoEntity, MultipartFile file)
            throws IOException {
        log.info("Creando empleado");
        String encode = URLEncoder.encode(Objects.requireNonNull(file.getOriginalFilename()), StandardCharsets.UTF_8)
                .replace("+", "-");
        EmpleadoResponseRest response = new EmpleadoResponseRest();
        try {

            // Comprobar si ya existe un empleado con los mismos datos
            Optional<EmpleadoEntity> existingEmpleado = empleadoRepository.findByFullData(empleadoEntity.getNombre(),
                    empleadoEntity.getApellido_p(), empleadoEntity.getApellido_m(), empleadoEntity.getPuesto(),
                    empleadoEntity.getDepartamento(), String.valueOf(empleadoEntity.getTelefono()));
            if (existingEmpleado.isPresent()) {
                response.setMetada("Respuesta FALLIDA", "-1", "Ya existe un emplado con los mismos datos");
                return new ResponseEntity<EmpleadoResponseRest>(response, HttpStatus.CONFLICT);
            }

            // Comprobar campos vacios
            if (empleadoEntity.getNombre() == null || empleadoEntity.getNombre().isEmpty() ||
                    empleadoEntity.getApellido_p() == null || empleadoEntity.getApellido_p().isEmpty() ||
                    empleadoEntity.getApellido_m() == null || empleadoEntity.getApellido_m().isEmpty() ||
                    empleadoEntity.getPuesto() == null || empleadoEntity.getPuesto().isEmpty() ||
                    empleadoEntity.getDepartamento() == null || empleadoEntity.getDepartamento().isEmpty()) {
                log.info(empleadoEntity.toString());
                response.setMetada("Respuesta FALLIDA", "-1", "Hay campos vacios");
                return new ResponseEntity<EmpleadoResponseRest>(response, HttpStatus.BAD_REQUEST);
            }

            // Verificar si el teléfono ya está registrado
            Optional<EmpleadoEntity> empleadoByPhone = empleadoRepository.findByPhone(empleadoEntity.getTelefono());
            if (empleadoByPhone.isPresent()) {
                response.setMetada("Respuesta FALLIDA", "-1", "Ya existe un empleado con este teléfono");
                return new ResponseEntity<EmpleadoResponseRest>(response, HttpStatus.CONFLICT);
            }
            if (empleadoEntity.getTelefono().toString().length() != 10) {
                response.setMetada("Respuesta FALLIDA", "-1", "Un numero solo cuenta con 10 digitos");
                return new ResponseEntity<EmpleadoResponseRest>(response, HttpStatus.CONFLICT);
            }

            // Verificar si el archivo es nulo o vacío
            if (encode != null && !encode.isEmpty()) {
                String name = uploadService.saveUpload(file, urlUpload);
                empleadoEntity.setFoto(name);
            }

            empleadoEntity.setEstado(true);

            empleadoEntity.setFecha_registro(new Date());

            // Guardar el empleado
            EmpleadoEntity savedEmpleado = empleadoRepository.save(empleadoEntity);

            List<EmpleadoEntity> empleados = new ArrayList<>();
            empleados.add(savedEmpleado);
            response.getEmpleadoResponse().setEmpleado(empleados);
            response.setMetada("Respuesta OK", "00", "Respuesta exitosa");
        } catch (Exception e) {
            response.setMetada("Respuesta FALLIDA", "-1", "Respuesta fallida");
            log.error("Error al crear empleado", e);
            e.getStackTrace();
            return new ResponseEntity<EmpleadoResponseRest>(response, HttpStatus.INTERNAL_SERVER_ERROR);
        }
        return new ResponseEntity<EmpleadoResponseRest>(response, HttpStatus.CREATED);
    }

    @Override
    public ResponseEntity<EmpleadoResponseRest> actualizar(EmpleadoEntity empleadoEntity, Integer id,
            MultipartFile file) throws IOException {
        log.info("Actualizando empleado");
        String encode = URLEncoder.encode(Objects.requireNonNull(file.getOriginalFilename()), StandardCharsets.UTF_8)
                .replace("+", "-");
        EmpleadoResponseRest response = new EmpleadoResponseRest();
        List<EmpleadoEntity> list = new ArrayList<>();

        try {
            // Verificar si el empleado existe
            Optional<EmpleadoEntity> empleado = empleadoRepository.findById(id);
            if (empleado.isPresent()) {
                EmpleadoEntity existingEmpleado = empleado.get();

                log.info(encode);

                // Comprobar que al menos un campo a actualizar sea diferente
                boolean campoDiferente = false;

                // Comprobar nombre
                if (empleadoEntity.getNombre() != null && !empleadoEntity.getNombre().isEmpty()
                        && !empleadoEntity.getNombre().equals(existingEmpleado.getNombre())) {
                    existingEmpleado.setNombre(empleadoEntity.getNombre());
                    campoDiferente = true;
                }

                // Comprobar apellido paterno
                if (empleadoEntity.getApellido_p() != null && !empleadoEntity.getApellido_p().isEmpty()
                        && !empleadoEntity.getApellido_p().equals(existingEmpleado.getApellido_p())) {
                    existingEmpleado.setApellido_p(empleadoEntity.getApellido_p());
                    campoDiferente = true;
                }

                // Comprobar apellido materno
                if (empleadoEntity.getApellido_m() != null && !empleadoEntity.getApellido_m().isEmpty()
                        && !existingEmpleado.getApellido_m().equals(empleadoEntity.getApellido_m())) {
                    existingEmpleado.setApellido_m(empleadoEntity.getApellido_m());
                    campoDiferente = true;
                }

                // Comprobar puesto
                if (empleadoEntity.getPuesto() != null && !empleadoEntity.getPuesto().isEmpty()
                        && !existingEmpleado.getPuesto().equals(empleadoEntity.getPuesto())) {
                    existingEmpleado.setPuesto(empleadoEntity.getPuesto());
                    campoDiferente = true;
                }

                // Comprobar departamento
                if (empleadoEntity.getDepartamento() != null && !empleadoEntity.getDepartamento().isEmpty()
                        && !existingEmpleado.getDepartamento().equals(empleadoEntity.getDepartamento())) {
                    existingEmpleado.setDepartamento(empleadoEntity.getDepartamento());
                    campoDiferente = true;
                }

                // Comprobar teléfono
                if (!Objects.equals(empleadoEntity.getTelefono(), existingEmpleado.getTelefono())
                        && String.valueOf(empleadoEntity.getTelefono()).length() == 10) {

                    // Verificar si el teléfono ya está registrado
                    Optional<EmpleadoEntity> empleadoByPhone = empleadoRepository
                            .findByPhone(empleadoEntity.getTelefono());
                    if (empleadoByPhone.isPresent()) {
                        response.setMetada("Respuesta FALLIDA", "-1", "Ya existe un empleado con este teléfono");
                        return new ResponseEntity<EmpleadoResponseRest>(response, HttpStatus.CONFLICT);
                    }

                    existingEmpleado.setTelefono(empleadoEntity.getTelefono());
                    campoDiferente = true;
                }

                // Comprobar Foto
                if ((encode != null && !encode.isEmpty()) && (file != null && !file.isEmpty())) {
                    // Si hay un nuevo archivo, eliminar la foto anterior si existe
                    if (existingEmpleado.getFoto() != null && !existingEmpleado.getFoto().isEmpty()) {
                        uploadService.deleteUpload(existingEmpleado.getFoto(), urlUpload);
                    }

                    // Guardar la nueva foto y actualizar el Empleado
                    String name = uploadService.saveUpload(file, urlUpload);
                    existingEmpleado.setFoto(name);
                    campoDiferente = true;
                }

                // Actualizar estado
                existingEmpleado.setEstado(true);

                if (campoDiferente == false) {
                    response.setMetada("Respuesta FALLIDA", "-1",
                            "No se han realizado cambios en los datos del empleado");
                    return new ResponseEntity<EmpleadoResponseRest>(response, HttpStatus.BAD_REQUEST);
                }

                // Guardar el empleado actualizado
                EmpleadoEntity updatedUser = empleadoRepository.save(existingEmpleado);

                list.add(updatedUser);
                response.getEmpleadoResponse().setEmpleado(list);
                response.setMetada("Respuesta OK", "00", "Actualización exitosa");
            } else {
                response.setMetada("Respuesta no encontrada", "-1", "Empleado no encontrado");
                return new ResponseEntity<EmpleadoResponseRest>(response, HttpStatus.NOT_FOUND);
            }
        } catch (Exception e) {
            response.setMetada("Respuesta FALLIDA", "-1", "No se puede actualizar el empleado");
            log.error("Error al actualizar empleado", e);
            e.getStackTrace();
            return new ResponseEntity<EmpleadoResponseRest>(response, HttpStatus.INTERNAL_SERVER_ERROR);
        }
        return new ResponseEntity<EmpleadoResponseRest>(response, HttpStatus.CREATED);
    }

    @Override
    public ResponseEntity<EmpleadoResponseRest> eliminar(Integer id) {
        log.info("Deshabilitar empleado");
        EmpleadoResponseRest response = new EmpleadoResponseRest();

        try {

            // Verificar si el empleado existe
            Optional<EmpleadoEntity> empleado = empleadoRepository.findById(id);
            if (empleado.isPresent()) {
                EmpleadoEntity existingEmpleado = empleado.get();

                // Actualizar estado
                // existingEmpleado.setEstado(false);
                // empleadoRepository.save(existingEmpleado);

                // Comrpobar que no tiene una asignacion activa
                if (existingEmpleado.getAsignacion() != null) {
                    response.setMetada("Error", "-1", "El empleado cuenta con una asignacion activa");
                    return new ResponseEntity<EmpleadoResponseRest>(response, HttpStatus.CONFLICT);
                }
                // Se crea la peticon para dar de baja al empleado
                PeticionesEntity peticion = new PeticionesEntity();

                peticion.setCategoria("EMPLEADOS");
                peticion.setEstado("PENDIENTE");
                peticion.setId_entidad(existingEmpleado.getId());
                peticion.setTipo_peticion("DESHABILITAR");

                Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
                String username = authentication.getName(); // El username del usuario autenticado
                peticion.setUser(userRepository.findByUsername(username).get());

                peticion.setAnexo(null);

                peticionesService.create(peticion, null);

                response.setMetada("Respuesta OK", "00", "Peticion para deshabilitar empleado enviada");
            } else {
                response.setMetada("Respuesta no encontrada", "-1", "Empleado no encontrado");
                return new ResponseEntity<EmpleadoResponseRest>(response, HttpStatus.NOT_FOUND);
            }
        } catch (IllegalArgumentException i) {
            response.setMetada("Respuesta FALLIDA", "-1", i.getMessage());
            log.error("Error al eliminar empleado", i);
            i.getStackTrace();
            return new ResponseEntity<EmpleadoResponseRest>(response, HttpStatus.INTERNAL_SERVER_ERROR);
        } catch (Exception e) {
            response.setMetada("Respuesta FALLIDA", "-1", "Respuesta fallida");
            log.error("Error al eliminar empleado", e);
            e.getStackTrace();
            return new ResponseEntity<EmpleadoResponseRest>(response, HttpStatus.INTERNAL_SERVER_ERROR);
        }
        return new ResponseEntity<EmpleadoResponseRest>(response, HttpStatus.CREATED);
    }
}
