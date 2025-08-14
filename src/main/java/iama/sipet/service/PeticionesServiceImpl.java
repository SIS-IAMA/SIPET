package iama.sipet.service;

import java.io.IOException;
import java.util.*;

import iama.sipet.event.PeticionCreadaEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import iama.sipet.entity.AsignacionEntity;
import iama.sipet.entity.AsignacionHistoryEntity;
import iama.sipet.entity.EmpleadoEntity;
import iama.sipet.entity.EquipoTecnologicoEntity;
import iama.sipet.entity.ListaEquiposEntity;
import iama.sipet.entity.PeticionesEntity;
import iama.sipet.entity.ResponsivaEntity;
import iama.sipet.entity.UserEntity;
import iama.sipet.repository.AsignacionHistoryRepository;
import iama.sipet.repository.AsignacionRespository;
import iama.sipet.repository.EmpleadoRepository;
import iama.sipet.repository.EquipoTecnologicoRespository;
import iama.sipet.repository.ListaEquiposRepository;
import iama.sipet.repository.PeticionesRepository;
import iama.sipet.repository.ResponsivaRespository;
import iama.sipet.repository.UserRepository;
import iama.sipet.response.PeticionesResponseRest;

@Service
public class PeticionesServiceImpl implements IPeticionService {
    private static final Logger log = LoggerFactory.getLogger(PeticionesServiceImpl.class);

    @Autowired
    private PeticionesRepository peticionesRepository;

    @Autowired
    private AsignacionRespository asignacionRespository;

    @Autowired
    private AsignacionHistoryRepository asignacionHistoryRepository;

    @Autowired
    private ResponsivaRespository responsivaRespository;

    @Autowired
    private EmpleadoRepository empleadoRepository;

    @Autowired
    private EquipoTecnologicoRespository equipoRepository;

    @Autowired
    private ListaEquiposRepository listaEquiposRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ApplicationEventPublisher eventPublisher;

    @Autowired
    private UploadService uploadService;

    String url = "https://sipet-iama.onrender.com/";

    String urlResponsivaUpload = "upload/Responsivas/";
    String urlListasDesechoUpload = "upload/ListasEquipos/Desecho/";
    String urlListasDonacionUpload = "upload/ListasEquipos/Donacion/";

    // Crear peticiones
    @Override
    public Boolean create(PeticionesEntity peticionesEntity, MultipartFile file) throws IOException {
        log.info("Creando Peticion");
        try {
            // Comprobar operador
            if (peticionesEntity.getUser() == null || Objects.equals(peticionesEntity.getUser().toString(), "")) {
                throw new IllegalArgumentException("No se recibio ningun usuario");
            }

            Optional<UserEntity> userOptional = userRepository.findById(peticionesEntity.getUser().getId());
            if (!userOptional.isPresent()) {
                throw new IllegalArgumentException("No se encontro ningun usuario");
            }

            // Comprobar datos recibidos
            // Comprobar tipo de peticion
            if (peticionesEntity.getTipo_peticion().isEmpty() || peticionesEntity.getTipo_peticion().equals(null)) {
                throw new IllegalArgumentException("No se recibio el tipo de peticion");
            }

            // Comprobar categoria de la peticion
            if (peticionesEntity.getCategoria().isEmpty() || peticionesEntity.getCategoria().equals(null)) {
                throw new IllegalArgumentException("No se recibio la categoria de la peticion");
            }

            // Comprobar entidad
            if ((peticionesEntity.getId_entidad() == null || peticionesEntity.getId_entidad() <= 0) &&
                    (peticionesEntity.getCategoria() != null && !peticionesEntity.getCategoria().equals("OPERADOR"))) {
                throw new IllegalArgumentException("No se recibio ningun id de alguna entidad");
            }

            // Decidir que tipo de peticion crear depenidendo de la categoria
            switch (peticionesEntity.getCategoria()) {
                // Crear peticion para asignacion
                case "ASIGNACIONES":
                    // Verificar que exista la asignacion
                    Optional<AsignacionEntity> asignacionOptional = asignacionRespository
                            .findById(peticionesEntity.getId_entidad());
                    if (asignacionOptional.isEmpty()) {
                        throw new IllegalArgumentException("No se pudo encontrar ninguna asignacion");
                    }
                    break;
                // Crear peticion para empleados
                case "EMPLEADOS":
                    // Verificar que exista el empleado
                    Optional<EmpleadoEntity> empleadoOptional = empleadoRepository
                            .findById(peticionesEntity.getId_entidad());
                    if (empleadoOptional.isEmpty()) {
                        throw new IllegalArgumentException("No se pudo encontrar ningun empleado");
                    }
                    // Verificar que el empleado no tenga una peticion para deshabilitar pendiente
                    Optional<PeticionesEntity> deleteEmpleado = peticionesRepository.findByEntidadCategoriaTipo(
                            peticionesEntity.getId_entidad(), peticionesEntity.getCategoria(),
                            peticionesEntity.getTipo_peticion());
                    if (deleteEmpleado.isPresent()) {
                        throw new IllegalArgumentException(
                                "Ya tienes una peticion para " + peticionesEntity.getTipo_peticion().toLowerCase() + " "
                                        + peticionesEntity.getCategoria().toLowerCase() + " pendiente");
                    }
                    break;
                // Crear peticion para listas
                case "LISTAS":
                    // Verificar que exista la lista
                    Optional<ListaEquiposEntity> listaOptional = listaEquiposRepository
                            .findById(peticionesEntity.getId_entidad());
                    if (listaOptional.isEmpty()) {
                        throw new IllegalArgumentException("No se pudo encontrar ninguna lista");
                    }
                    break;
                // Crear peticion para operadores
                case "OPERADOR":// Esta peticion viene de EmailServiceImpl
                    // Verificar que exista el operador
                    Optional<UserEntity> operadorOptional = userRepository.findById(peticionesEntity.getOperadorId());
                    if (operadorOptional.isEmpty()) {
                        throw new IllegalArgumentException("No existe algun usuario con ese nombre de usuario");
                    }
                    // Verificar que no exista otra peticion para recuperar contraseña para ese
                    // operador
                    Optional<PeticionesEntity> rePassword = peticionesRepository.findByUserPassword(
                            peticionesEntity.getUser().getId(), peticionesEntity.getTipo_peticion());
                    if (rePassword.isPresent()) {
                        if (!rePassword.get().getEstado().equals("ACEPTADA")) {
                            throw new IllegalArgumentException(
                                    "Ya tienes una peticion para recuperar contraseña pendiente");
                        }
                        rePassword.get().setEstado("PENDIENTE");
                        peticionesRepository.save(rePassword.get());
                        return true;
                    }
                    break;

                default:
                    throw new IllegalArgumentException("Categoria invalida");
            }

            peticionesEntity.setFecha_registro(new Date());
            peticionesEntity.setEstado("PENDIENTE");
            peticionesRepository.save(peticionesEntity);
        } catch (IllegalArgumentException e) {
            log.error("Error al crear token", e);
            throw e;
        } catch (Exception e) {
            log.info("Algo salio mal al crear la peticion", e);
            throw new IllegalArgumentException("Algo salio mal al crear la peticion");
        }
        return true;
    }

    @Override
    public ResponseEntity<PeticionesResponseRest> gestionar(Integer id, Boolean estado,
            PeticionesEntity comentarioEntity) {
        log.info("Gestionando Peticion");
        PeticionesResponseRest response = new PeticionesResponseRest();
        List<PeticionesEntity> list = new ArrayList<>();

        try {
            // Verificar que se envio el Id de la peticion
            if (id == null) {
                response.setMetada("Respuesta fallida", "-1", "No se recibio ningun id");
                return new ResponseEntity<PeticionesResponseRest>(response, HttpStatus.NOT_FOUND);
            }

            // Verificar que existe la peticion
            Optional<PeticionesEntity> peticion = peticionesRepository.findById(id);
            if (peticion.isEmpty()) {
                response.setMetada("Respuesta FALLIDA", "-1", "No se encontro ninguna peticion");
                return new ResponseEntity<PeticionesResponseRest>(response, HttpStatus.NOT_FOUND);
            }
            // Guardar peticion existente
            PeticionesEntity existPeticion = peticion.get();

            // Verificar que se recibio el estado para actulizar la peticion
            if (existPeticion.getEstado().equals("ACEPTADA")) {
                response.setMetada("Respuesta FALLIDA", "-1", "Esta peticion ya ha sido validada ");
                return new ResponseEntity<PeticionesResponseRest>(response, HttpStatus.CONFLICT);
            }
            // Decidir si se aprueba o no
            if (estado) {
                // Verificar categoria de la peticion
                switch (existPeticion.getCategoria()) {
                    // Categoria = Asignacion
                    case "ASIGNACIONES":
                        // Comprobar que la asignacion exista
                        Optional<AsignacionEntity> asignacionOptional = asignacionRespository
                                .findById(existPeticion.getId_entidad());
                        if (asignacionOptional.isEmpty()) {
                            throw new IllegalArgumentException("No se pudo encontrar ninguna asignacion");
                        }

                        // Guardar la asignacion existente
                        AsignacionEntity existAsignacion = asignacionOptional.get();

                        // Obtener el empleado de la asignacion para su uso posterior
                        Optional<EmpleadoEntity> empleadoAsignacionOptional = empleadoRepository
                                .findById(existAsignacion.getEmpleado().getId());
                        if (empleadoAsignacionOptional.isEmpty()) {
                            throw new IllegalArgumentException("No se pudo encontrar el empleado de la asignacion");
                        }
                        EmpleadoEntity existEmpleadoAsignacion = empleadoAsignacionOptional.get();

                        // Obtener la lista de equipos de la asignacion para su uso posterior
                        Optional<ListaEquiposEntity> listaAsignacionOptional = listaEquiposRepository
                                .findById(existAsignacion.getListaEquipos().getId());
                        if (listaAsignacionOptional.isEmpty()) {
                            throw new IllegalArgumentException("No se pudo encontrar la lista de la asignacion");
                        }
                        ListaEquiposEntity existLista = listaAsignacionOptional.get();

                        // Obtener la responsiva activa asignacion para su uso posterior
                        Optional<ResponsivaEntity> responsivaOptional = responsivaRespository
                                .findById(existAsignacion.getResponsivaActiva().getId());
                        if (responsivaOptional.isEmpty()) {
                            throw new IllegalArgumentException("No se pudo encontrar responsiva de la asignacion");
                        }
                        ResponsivaEntity responsivaActiva = responsivaOptional.get();

                        // Verificar el tipo de peticion
                        switch (existPeticion.getTipo_peticion()) {
                            // Validar una peticion y Actualizar lista y equipos (Se da la alta para crear y
                            // actualizar una asignacion)
                            case "VALIDAR":
                                // Iterar sobre los equipos de la lista para cambiar su estado
                                for (EquipoTecnologicoEntity equipos : existLista.getEquipoTecnologico()) {
                                    // Verificar que ele equipo exista
                                    Optional<EquipoTecnologicoEntity> equipoOptional = equipoRepository
                                            .findById(equipos.getId());
                                    if (equipoOptional.isEmpty()) {
                                        throw new IllegalArgumentException("No se pudo encontrar ningun equipo");
                                    }
                                    // Guardar el equipo existente
                                    EquipoTecnologicoEntity existEquipo = equipoOptional.get();
                                    // Actualizar el estado del equipo
                                    existEquipo.setEstado("ASIGNADO");
                                    // Actualizar datos del equipo
                                    equipoRepository.save(existEquipo);
                                }

                                // Actualizar el estado de la lista
                                existLista.setEstado(true);

                                // Actualizar responsiva
                                responsivaActiva.setEstado(true);
                                responsivaActiva.setEmpleado(existEmpleadoAsignacion);
                                responsivaRespository.save(responsivaActiva);

                                // Cambiamos el estado de la asignacion a ACTIVA
                                existAsignacion.setEstado("ACTIVA");

                                // Actualizamos la fecha
                                existAsignacion.setFecha_registro(new Date());
                                // Guardamos la asignacion
                                asignacionRespository.save(existAsignacion);
                                eventPublisher.publishEvent(new PeticionCreadaEvent(this, existPeticion));

                                response.setMetada("Respuesta OK", "00", "La asignacion ha sido aceptada");
                                break;

                            case "ACTUALIZAR":
                                // Iterar sobre los equipos de la lista para cambiar su estado
                                for (EquipoTecnologicoEntity equipos : existLista.getEquipoTecnologico()) {
                                    // Verificar que el equipo exista
                                    Optional<EquipoTecnologicoEntity> equipoOptional = equipoRepository
                                            .findById(equipos.getId());
                                    if (equipoOptional.isEmpty()) {
                                        throw new IllegalArgumentException("No se pudo encontrar ningun equipo");
                                    }
                                    // Guardar el equipo existente
                                    EquipoTecnologicoEntity existEquipo = equipoOptional.get();
                                    // Actualizar el estado del equipo
                                    existEquipo.setEstado("ASIGNADO");
                                    // Actualizar datos del equipo
                                    equipoRepository.save(existEquipo);
                                }

                                for (Integer idHistory : existAsignacion.getAsignacionHistory().getIdEquiposList()){
                                    // Verificar que el equipo exista
                                    Optional<EquipoTecnologicoEntity> equipoOptional = equipoRepository
                                            .findById(idHistory);
                                    if (equipoOptional.isEmpty()) {
                                        throw new IllegalArgumentException("No se pudo encontrar ningun equipo");
                                    }
                                    // Guardar el equipo existente
                                    EquipoTecnologicoEntity existEquipo = equipoOptional.get();
                                    // Comprobar el estado del equipo
                                    if (existEquipo.getEstado().equals("PENDIENTE")) {
                                        // Actualizar datos del equipo

                                        existEquipo.setEstado("ACTIVO");
                                        equipoRepository.save(existEquipo);

                                    }
                                }

                                // Actualizar el estado de la lista
                                existLista.setEstado(true);

                                // Actualizar responsiva
                                responsivaActiva.setEstado(true);
                                responsivaActiva.setEmpleado(existEmpleadoAsignacion);
                                responsivaRespository.save(responsivaActiva);

                                // Actualizamos la fecha de modificacion
                                existAsignacion.setFecha_modificacion(new Date());

                                // Cambiamos el estado de la asignacion a ACTIVA
                                existAsignacion.setEstado("ACTIVA");
                                // Guardamos la asignacion
                                AsignacionEntity saveAsignacion = asignacionRespository.save(existAsignacion);

                                Optional<AsignacionHistoryEntity> existAsignacionHistory = asignacionHistoryRepository
                                        .findById(saveAsignacion.getAsignacionHistory().getId());
                                if (!existAsignacionHistory.isPresent()) {
                                    throw new IllegalArgumentException(
                                            "Hubo un error, si no se realizo el cambio vuelve a intentarlo");
                                }

                                asignacionRespository.save(saveAsignacion);
                                asignacionHistoryRepository.save(existAsignacionHistory.get());
                                asignacionHistoryRepository.delete(existAsignacionHistory.get());
                                eventPublisher.publishEvent(new PeticionCreadaEvent(this, existPeticion));

                                response.setMetada("Respuesta OK", "00", "La asignacion ha sido actualizada");
                                break;
                            // Deshacer asignacion y Actualizar lista y equipos
                            case "DESHACER":
                                // Iterar sobre los equipos de la lista para cambiar su estado y dejarlos libres
                                // para asignar a otra lista
                                for (EquipoTecnologicoEntity equipos : existLista.getEquipoTecnologico()) {
                                    // Verificar que el equipo existe
                                    Optional<EquipoTecnologicoEntity> equipoOptional = equipoRepository
                                            .findById(equipos.getId());
                                    if (equipoOptional.isEmpty()) {
                                        throw new IllegalArgumentException("No se pudo encontrar un equipo");
                                    }
                                    // Guardar el equipo
                                    EquipoTecnologicoEntity existEquipo = equipoOptional.get();
                                    // Actualizar y guardar datos del equipo
                                    existEquipo.setEstado("ACTIVO");
                                    existEquipo.setListaEquipos(null);
                                    equipoRepository.save(existEquipo);
                                }

                                // Actualizar datos de la lista para que no afecte a otras entidades
                                existLista.getEquipoTecnologico().clear();
                                existLista.setAsignacion(null);
                                listaEquiposRepository.save(existLista);
                                listaEquiposRepository.delete(existLista);

                                // Desvincular la responsiva de la lista para que no afecte a el log del
                                // empleado
                                responsivaActiva.setEstado(false);
                                responsivaActiva.setAsignacionActiva(null);
                                responsivaRespository.save(responsivaActiva);
                                existAsignacion.setResponsivaActiva(null);

                                // Desvinculamos al empleado para que pueda ser asignado a otra asignacion
                                // diferente
                                existEmpleadoAsignacion.setAsignacion(null);
                                empleadoRepository.save(existEmpleadoAsignacion);
                                existAsignacion.setEmpleado(null);

                                // Eliminamos la asignacion
                                asignacionRespository.save(existAsignacion);
                                asignacionRespository.delete(existAsignacion);

                                response.setMetada("Respuesta OK", "00", "La asignacion ha sido deshecha");
                                break;
                            default:
                                break;
                        }
                        break;

                    // Categoria = Empleado
                    case "EMPLEADOS":
                        // Verificar que el empleado exista
                        Optional<EmpleadoEntity> empleadoOptional = empleadoRepository
                                .findById(existPeticion.getId_entidad());
                        if (empleadoOptional.isEmpty()) {
                            throw new IllegalArgumentException("No se pudo encontrar ningun empleado");
                        }
                        // Guardar el empleado
                        EmpleadoEntity existEmpleado = empleadoOptional.get();
                        // Actualizar y guardar los datos del empleado
                        existEmpleado.setEstado(false);
                        // Actualizamos la fecha para tenerla de referencia de cuando se Desha
                        existEmpleado.setFecha_registro(new Date());
                        empleadoRepository.save(existEmpleado);

                        response.setMetada("Respuesta OK", "00", "El empleado fue deshabilitado correctamente");
                        break;

                    // Categoria = Listas
                    case "LISTAS":
                        // Verificar que la lista exista
                        Optional<ListaEquiposEntity> listaOptional = listaEquiposRepository
                                .findById(existPeticion.getId_entidad());
                        if (listaOptional.isEmpty()) {
                            throw new IllegalArgumentException("No se pudo encontrar ninguna lista");
                        }
                        // Guardar la lista
                        ListaEquiposEntity existList = listaOptional.get();

                        // Actualizar los datos de la lista y equipos
                        for (EquipoTecnologicoEntity equipos : existList.getEquipoTecnologico()) {
                            // Verificar que el equipo exista
                            Optional<EquipoTecnologicoEntity> equipoOptional = equipoRepository
                                    .findById(equipos.getId());
                            if (equipoOptional.isEmpty()) {
                                throw new IllegalArgumentException("No se pudo encontrar ningun equipo");
                            }
                            // Guardar el equipo
                            EquipoTecnologicoEntity existEquipo = equipoOptional.get();

                            // Actualizar el estado del equipo dependiendo del estado
                            switch (existList.getTipo()) {
                                case "DONACION":
                                    existEquipo.setEstado("DONADO");
                                    break;
                                case "DESECHO":
                                    existEquipo.setEstado("DESECHADO");
                                    break;
                                default:
                                    throw new IllegalArgumentException("Tipo de lista invalido");
                            }
                            // Guardar datos del equipo actualizado
                            equipoRepository.save(existEquipo);
                        }
                        // Actualizar y guardar estado de la lista
                        existList.setEstado(true);
                        listaEquiposRepository.save(existList);
                        eventPublisher.publishEvent(new PeticionCreadaEvent(this, existPeticion));

                        break;

                    case "OPERADOR":
                        // Verificar que el tipo de peticion es para la contraseña
                        if (existPeticion.getTipo_peticion().equals("PASSWORD")) {
                            if (existPeticion.getUser().getRol().equals(false)) {
                                BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
                                String contrasenaEncriptada = passwordEncoder.encode("12345");
                                existPeticion.getUser().setPassword(contrasenaEncriptada);
                                userRepository.save(existPeticion.getUser());
                                response.setMetada("Respuesta OK", "00",
                                        "Se ha establecido la contraseña defaul para el operador " + existPeticion.getUser().getNombre());
                            }
                        }
                        break;
                    default:
                        throw new IllegalArgumentException("Categoria invalida");
                }

                existPeticion.setEstado("ACEPTADA");

            } else { // Debe llevar un comentario en caso de rechazar la peticion
                if (comentarioEntity.getComentario().isEmpty()) {
                    response.setMetada("Respuesta FALLIDA", "-1", "No se recibio ningun comentario");
                    return new ResponseEntity<PeticionesResponseRest>(response, HttpStatus.NOT_FOUND);
                }
                existPeticion.setEstado("RECHAZADA");
                existPeticion.setComentario(comentarioEntity.getComentario());
            }

            peticionesRepository.save(existPeticion);
            list.add(existPeticion);
            response.getPeticionesResponse().setPeticion(list);
        } catch (IllegalArgumentException e) {
            log.error("Error al crear una asignacion", e);
            response.setMetada("Respuesta FALLIDA", "-1", e.getMessage());
            return new ResponseEntity<PeticionesResponseRest>(response, HttpStatus.INTERNAL_SERVER_ERROR);
        } catch (Exception e) {
            response.setMetada("Respuesta FALLIDA", "-1", e.getMessage());
            log.error("Error al gestionar la peticion", e);
            e.getStackTrace();
            return new ResponseEntity<PeticionesResponseRest>(response, HttpStatus.INTERNAL_SERVER_ERROR);
        }
        return new ResponseEntity<PeticionesResponseRest>(response, HttpStatus.OK);
    }

    @Override
    public Boolean upload(Integer id, String file) throws IOException {
        log.info("Actualizando peticion");
        try {
            Optional<PeticionesEntity> peticionOptional = peticionesRepository.findById(id);
            if (peticionOptional.isEmpty()) {
                throw new IllegalArgumentException("No encontro la peticion");
            }
            PeticionesEntity peticionesEntity = peticionOptional.get();
            peticionesEntity.setAnexo(file);
            peticionesEntity.setEstado("PENDIENTE");

            peticionesRepository.save(peticionesEntity);
        } catch (IllegalArgumentException e) {
            log.error("Error al crear token", e);
            throw e;
        } catch (Exception e) {
            log.info("Algo salio mal al crear la peticion", e);
            throw new IllegalArgumentException("Algo salio mal al crear la peticion");
        }
        return true;
    }

    @Override
    public ResponseEntity<PeticionesResponseRest> delete(Integer id) {
        log.info("Eliminar peticion");
        PeticionesResponseRest response = new PeticionesResponseRest();
        List<PeticionesEntity> list = new ArrayList<>();
        try {
            Optional<PeticionesEntity> peticion = peticionesRepository.findById(id);
            if (peticion.isEmpty()) {
                response.setMetada("Respuesta ok", "00", "No se encontro la peticion ");
                return new ResponseEntity<PeticionesResponseRest>(response, HttpStatus.NOT_FOUND);
            }

            PeticionesEntity existPeticion = peticion.get();

            if (!existPeticion.getEstado().equals("ACEPTADA")) {

                // Verificar categoria de la peticion
                switch (existPeticion.getCategoria()) {
                    // Categoria = Asignacion
                    case "ASIGNACIONES":
                        // Comprobar que la asignacion exista
                        Optional<AsignacionEntity> asignacionOptional = asignacionRespository
                                .findById(existPeticion.getId_entidad());
                        if (asignacionOptional.isEmpty()) {
                            throw new IllegalArgumentException("No se pudo encontrar ninguna asignacion");
                        }
                        // Guardar la asignacion existente
                        AsignacionEntity existAsignacion = asignacionOptional.get();

                        // Obtener la lista de equipos de la asignacion para su uso posterior
                        ListaEquiposEntity existLista = existAsignacion.getListaEquipos();

                        // Verificar el tipo de peticion
                        switch (existPeticion.getTipo_peticion()) {
                            // Validar una peticion y Actualizar lista y equipos (Se da la alta para crear y
                            // actualizar una asignacion)
                            case "VALIDAR":
                                if (!existPeticion.getEstado().equals("ACEPTADA")) {
                                    // Validación defensiva
                                    if (existLista != null) {
                                        // Iterar sobre los equipos para liberarlos
                                        for (EquipoTecnologicoEntity equipo : existLista.getEquipoTecnologico()) {
                                            Optional<EquipoTecnologicoEntity> equipoOptional = equipoRepository
                                                    .findById(equipo.getId());
                                            if (equipoOptional.isEmpty()) {
                                                throw new IllegalArgumentException(
                                                        "No se pudo encontrar un equipo con ID: " + equipo.getId());
                                            }

                                            EquipoTecnologicoEntity existEquipo = equipoOptional.get();
                                            existEquipo.setEstado("ACTIVO");
                                            existEquipo.setListaEquipos(null);
                                            equipoRepository.save(existEquipo);
                                        }

                                        // Limpiar relaciones entre lista y asignación
                                        existLista.setAsignacion(null);

                                        // Guardar cambios antes de eliminar
                                        listaEquiposRepository.save(existLista);

                                        // Eliminar la lista
                                        listaEquiposRepository.delete(existLista);
                                    }

                                    existAsignacion.setListaEquipos(null);

                                    if (existAsignacion.getResponsivaActiva() != null) {
                                        // Eliminar responsiva relacionadas
                                        String nombre = existAsignacion.getResponsivaActiva().getPdf();
                                        uploadService.deleteUpload(nombre, urlResponsivaUpload);
                                        existAsignacion.getResponsivaActiva().setAsignacionActiva(null);
                                        responsivaRespository.delete(existAsignacion.getResponsivaActiva());
                                    }

                                    EmpleadoEntity existEmpleado = existAsignacion.getEmpleado();
                                    if (existEmpleado != null) {
                                        // Eliminar relación con empleado
                                        existEmpleado.setAsignacion(null);
                                        empleadoRepository.save(existEmpleado);
                                    }
                                    existAsignacion.setEmpleado(null);

                                    // Eliminar la asignación
                                    asignacionRespository.delete(existAsignacion);
                                }
                                // Mensaje de éxito
                                response.setMetada("Respuesta OK", "00", "La asignación ha sido deshecha");
                                break;

                            case "ACTUALIZAR":
                                if (!existAsignacion.getEstado().equals("ACEPTADA")) {
                                    Optional<AsignacionHistoryEntity> asignacionHistoryOptional = asignacionHistoryRepository
                                            .findByAsignacion(existAsignacion.getId());
                                    if (asignacionHistoryOptional.isEmpty()) {

                                    }
                                    AsignacionHistoryEntity existAsignacionHistory = asignacionHistoryOptional.get();

                                    // Revobinar equipos
                                    if (existAsignacionHistory.getIdEquiposList() != null) {
                                        // Validación defensiva
                                        if (existLista != null) {
                                            // Iterar sobre los equipos para liberarlos
                                            for (EquipoTecnologicoEntity equipo : existLista.getEquipoTecnologico()) {
                                                Optional<EquipoTecnologicoEntity> equipoOptional = equipoRepository
                                                        .findById(equipo.getId());
                                                if (equipoOptional.isEmpty()) {
                                                    throw new IllegalArgumentException(
                                                            "No se pudo encontrar un equipo con ID: " + equipo.getId());
                                                }

                                                EquipoTecnologicoEntity existEquipo = equipoOptional.get();
                                                existEquipo.setEstado("ACTIVO");
                                                existEquipo.setListaEquipos(null);
                                                equipoRepository.save(existEquipo);
                                            }

                                            for (Integer idEquipos : existAsignacionHistory.getIdEquiposList()) {
                                                Optional<EquipoTecnologicoEntity> equipoOptional = equipoRepository
                                                        .findById(idEquipos);
                                                if (equipoOptional.isEmpty()) {
                                                    throw new IllegalArgumentException(
                                                            "No se pudo encontrar un equipo");
                                                }
                                                EquipoTecnologicoEntity existEquipo = equipoOptional.get();
                                                existEquipo.setEstado("ASIGNADO");
                                                existEquipo.setListaEquipos(existLista);
                                                equipoRepository.save(existEquipo);
                                            }

                                            // Guardar cambios
                                            listaEquiposRepository.save(existLista);

                                        }

                                    }

                                    // Revobinar Ubicacion
                                    if (existAsignacionHistory.getUbicacion() != null) {
                                        existAsignacion.setUbicacion(existAsignacionHistory.getUbicacion());
                                    }

                                    // Revobinar fecha
                                    if (existAsignacionHistory.getFecha_registro() != null) {
                                        existAsignacion.setFecha_modificacion(existAsignacionHistory.getFecha_registro());
                                    } else {
                                        existAsignacion.setFecha_modificacion(null);
                                    }

                                    // Revobinar responsiva
                                    if (existAsignacion.getResponsivaActiva() != null) {
                                        // Eliminar responsiva relacionada
                                        String nombre = existAsignacion.getResponsivaActiva().getPdf();
                                        uploadService.deleteUpload(nombre, urlResponsivaUpload);
                                        existAsignacion.getResponsivaActiva().setAsignacionActiva(null);
                                        responsivaRespository.delete(existAsignacion.getResponsivaActiva());
                                    }

                                    Optional<ResponsivaEntity> responsivaOptional = responsivaRespository
                                            .findById(existAsignacionHistory.getIdResponsivaActiva());
                                    if (responsivaOptional.isEmpty()) {
                                        throw new IllegalArgumentException("No se pudo encontrar la asignacion anterior");
                                    }
                                    ResponsivaEntity existResponsiva = responsivaOptional.get();
                                    existResponsiva.setEstado(true);
                                    existResponsiva.setAsignacionActiva(existAsignacion);
                                    responsivaRespository.save(existResponsiva);

                                    existAsignacion.setResponsivaActiva(existResponsiva);
                                    existAsignacion.setEstado("ACTIVA");
                                    // Eliminar la asignación
                                    asignacionRespository.save(existAsignacion);
                                    response.setMetada("Respuesta OK", "00",
                                            "La peticion ha sido eliminada y la asignacion ha vuelto a su estado anterior");
                                } else {
                                    response.setMetada("Respuesta OK", "00", "La peticion ha sido eliminada");
                                }
                                break;

                            // Deshacer asignacion y Actualizar lista y equipos
                            case "DESHACER":
                                if (!existAsignacion.getEstado().equals("ACEPTADA")) {

                                    // Iterar sobre los equipos de la lista para cambiar su estado
                                    for (EquipoTecnologicoEntity equipos : existLista.getEquipoTecnologico()) {
                                        // Verificar que el equipo exista
                                        Optional<EquipoTecnologicoEntity> equipoOptional = equipoRepository
                                                .findById(equipos.getId());
                                        if (equipoOptional.isEmpty()) {
                                            throw new IllegalArgumentException("No se pudo encontrar ningun equipo");
                                        }
                                        // Guardar el equipo existente
                                        EquipoTecnologicoEntity existEquipo = equipoOptional.get();
                                        // Actualizar el estado del equipo
                                        existEquipo.setEstado("ASIGNADO");
                                        // Actualizar datos del equipo
                                        equipoRepository.save(existEquipo);
                                    }

                                    // Actualizar el estado de la lista
                                    existLista.setEstado(true);

                                    // Cambiamos el estado de la asignacion a ACTIVO
                                    existAsignacion.setEstado("ACTIVA");
                                    // Guardamos la asignacion
                                    asignacionRespository.save(existAsignacion);

                                    response.setMetada("Respuesta OK", "00", "La asignacion ha sido aceptada");
                                }
                                break;
                            default:
                                break;
                        }
                        break;

                    // Categoria = Listas
                    case "LISTAS":

                        // Verificar que la lista exista
                        Optional<ListaEquiposEntity> listaOptional = listaEquiposRepository
                                .findById(existPeticion.getId_entidad());
                        if (listaOptional.isEmpty()) {
                            throw new IllegalArgumentException("No se pudo encontrar ninguna lista");
                        }
                        // Guardar la lista
                        ListaEquiposEntity existList = listaOptional.get();

                        // Actualizar los datos de la lista y equipos
                        for (EquipoTecnologicoEntity equipos : existList.getEquipoTecnologico()) {
                            // Verificar que el equipo exista
                            Optional<EquipoTecnologicoEntity> equipoOptional = equipoRepository.findById(equipos.getId());
                            if (equipoOptional.isEmpty()) {
                                throw new IllegalArgumentException("No se pudo encontrar ningun equipo");
                            }
                            // Guardar el equipo
                            EquipoTecnologicoEntity existEquipo = equipoOptional.get();

                            // Actualizar el estado del equipo
                            existEquipo.setEstado("ACTIVO");
                            existEquipo.setListaEquipos(null);
                            // Guardar datos del equipo actualizado
                            equipoRepository.save(existEquipo);
                        }
                        // Actualizar y guardar estado de la lista
                        String nombre = existList.getPdf();
                        // Actualizar el estado del equipo dependiendo del estado
                        switch (existList.getTipo()) {
                            case "DONACION":
                                uploadService.deleteUpload(nombre, urlListasDonacionUpload);
                                break;
                            case "DESECHO":
                                uploadService.deleteUpload(nombre, urlListasDesechoUpload);
                                break;
                            default:
                                throw new IllegalArgumentException("Tipo de lista invalido");
                        }
                        existList.getEquipoTecnologico().clear();
                        listaEquiposRepository.save(existList);
                        listaEquiposRepository.delete(existList);
                        break;
                    default:
                }
            }

            existPeticion.setUser(null);
            peticionesRepository.save(existPeticion);
            peticionesRepository.delete(existPeticion);

            list.add(peticion.get());
            response.getPeticionesResponse().setPeticion(list);
            response.setMetada("Respuesta OK", "00", "Respuesta exitosa, peticion eliminada");
        } catch (Exception e) {
            response.setMetada("Respuesta FALLIDA", "-1", "Respuesta fallida");
            log.error("Error interno, comunicate con un administrador", e);
            e.getStackTrace();
            return new ResponseEntity<PeticionesResponseRest>(response, HttpStatus.INTERNAL_SERVER_ERROR);
        }
        return new ResponseEntity<PeticionesResponseRest>(response, HttpStatus.OK);
    }

    @Override
    public ResponseEntity<PeticionesResponseRest> getAll() {
        log.info("Buscar peticiones");
        PeticionesResponseRest response = new PeticionesResponseRest();
        try {

            List<PeticionesEntity> peticiones = peticionesRepository.findAll();

            if (peticiones.isEmpty()) {
                response.setMetada("Respuesta ok", "00", "No se encontraron peticiones");
                return new ResponseEntity<PeticionesResponseRest>(response, HttpStatus.NOT_FOUND);
            }

            // Iterar sobre las peticiones y agregar sus datos al response
            for (PeticionesEntity peticion : peticiones) {
                // Verificar si el operador tiene una foto
                if (peticion.getAnexo() != null && !peticion.getAnexo().isEmpty()) {
                    String fotoUrl = url + peticion.getAnexo();
                    peticion.setAnexo(fotoUrl);
                } else {
                    peticion.setAnexo(null); // Si no tiene anexo, establecer como null
                }
            }

            response.getPeticionesResponse().setPeticion(peticiones);
            response.setMetada("Respuesta OK", "00", "Respuesta exitosa");
        } catch (Exception e) {
            response.setMetada("Respuesta FALLIDA", "-1", "Respuesta fallida");
            log.error("Error al buscar peticiones", e);
            return new ResponseEntity<PeticionesResponseRest>(response, HttpStatus.INTERNAL_SERVER_ERROR);
        }
        return new ResponseEntity<PeticionesResponseRest>(response, HttpStatus.OK);
    }

    @Override
    public ResponseEntity<PeticionesResponseRest> getById(Integer id) {
        log.info("Buscar peticiones por id");
        PeticionesResponseRest response = new PeticionesResponseRest();
        List<PeticionesEntity> list = new ArrayList<>();
        try {
            Optional<PeticionesEntity> peticion = peticionesRepository.findById(id);
            if (peticion.isEmpty()) {
                response.setMetada("Respuesta ok", "00", "No se encontro la peticion con id " + id);
                return new ResponseEntity<PeticionesResponseRest>(response, HttpStatus.NOT_FOUND);
            }

            list.add(peticion.get());
            response.getPeticionesResponse().setPeticion(list);
            response.setMetada("Respuesta OK", "00", "Respuesta exitosa");
        } catch (Exception e) {
            response.setMetada("Respuesta FALLIDA", "-1", "Respuesta fallida");
            log.error("Error al buscar peticion", e);
            e.getStackTrace();
            return new ResponseEntity<PeticionesResponseRest>(response, HttpStatus.INTERNAL_SERVER_ERROR);
        }
        return new ResponseEntity<PeticionesResponseRest>(response, HttpStatus.OK);
    }

    @Override
    public ResponseEntity<PeticionesResponseRest> getByUser(String user) {
        log.info("Buscar peticiones del operador " + user);
        PeticionesResponseRest response = new PeticionesResponseRest();
        try {
            Optional<UserEntity> userOptional = userRepository.findByUsername(user);
            if (userOptional.isEmpty()) {
                response.setMetada("Respuesta ok", "-1", "No se encontro el usuario, intenta más tarde ");
                return new ResponseEntity<PeticionesResponseRest>(response, HttpStatus.NOT_FOUND);
            }

            List<PeticionesEntity> peticiones = peticionesRepository.findByUser(userOptional.get().getId());
            log.info(peticiones.toString());
            if (peticiones.isEmpty()) {
                response.setMetada("Respuesta ok", "-1", "No se encontraron peticiones");
                return new ResponseEntity<PeticionesResponseRest>(response, HttpStatus.NOT_FOUND);
            }

            // Iterar sobre las peticiones y agregar sus datos al response
            for (PeticionesEntity peticion : peticiones) {
                // Verificar si el operador tiene una foto
                if (peticion.getAnexo() != null && !peticion.getAnexo().isEmpty()) {
                    String fotoUrl = url + peticion.getAnexo();
                    peticion.setAnexo(fotoUrl);
                } else {
                    peticion.setAnexo(null); // Si no tiene anexo, establecer como null
                }
            }

            response.getPeticionesResponse().setPeticion(peticiones);
            response.setMetada("Respuesta OK", "00", "Respuesta exitosa");
        } catch (Exception e) {
            response.setMetada("Respuesta FALLIDA", "-1", "Respuesta fallida");
            log.error("Error al buscar peticiones", e);
            return new ResponseEntity<PeticionesResponseRest>(response, HttpStatus.INTERNAL_SERVER_ERROR);
        }
        return new ResponseEntity<PeticionesResponseRest>(response, HttpStatus.OK);
    }

}