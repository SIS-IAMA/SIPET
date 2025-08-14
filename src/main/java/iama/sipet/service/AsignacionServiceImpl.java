package iama.sipet.service;

import iama.sipet.entity.*;
import iama.sipet.repository.*;
import iama.sipet.response.AsignacionResponseRest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class AsignacionServiceImpl implements IAsignacionService {
    private static final Logger log = LoggerFactory.getLogger(AsignacionServiceImpl.class);

    @Autowired
    private PeticionesServiceImpl peticionesService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private IResponsivaService responsivaService;

    @Autowired
    private EquipoTecnologicoRespository equipoRepository;

    @Autowired
    private EmpleadoRepository empleadoRepository;

    @Autowired
    private ListaEquiposRepository listaEquiposRepository;

    @Autowired
    private AsignacionRespository asignacionRespository;

    @Autowired
    private AsignacionHistoryRepository asignacionHistoryRepository;

    @Autowired
    private ResponsivaRespository responsivaRespository;

    String url = "https://sipet-iama.onrender.com/upload/Responsivas/";
    String urlUpload = "upload/Responsivas/";
    String urlEmpleado = "https://sipet-iama.onrender.com/upload/FotosEmpleado/";

    // Servicio para crear una asignacion
    @Override
    public ResponseEntity<AsignacionResponseRest> create(AsignacionEntity asignacionEntity, MultipartFile file)
            throws IOException {
        log.info("Creando asignacion", asignacionEntity);
        AsignacionResponseRest response = new AsignacionResponseRest();

        try {
            // Comprobar campos vacios
            if (asignacionEntity.getEmpleado().toString().isEmpty() || asignacionEntity.getEmpleado() == null) {
                response.setMetada("Respuesta FALLIDA", "-1", "No se recibio ningun empleado");
                return new ResponseEntity<AsignacionResponseRest>(response, HttpStatus.NOT_FOUND);
            }

            if (asignacionEntity.getListaEquipos().toString().isEmpty() || asignacionEntity.getListaEquipos() == null){
                response.setMetada("Respuesta FALLIDA", "-1", "No se recibio ninguna lista");
                return new ResponseEntity<AsignacionResponseRest>(response, HttpStatus.NOT_FOUND);
            }

            if (asignacionEntity.getUbicacion() == null || asignacionEntity.getUbicacion().isEmpty()) {
                response.setMetada("Respuesta FALLIDA", "-1", "No se recibio la ubicacion");
                return new ResponseEntity<AsignacionResponseRest>(response, HttpStatus.NOT_FOUND);
            }

            // Comprobar si existe el empleado
            Optional<EmpleadoEntity> optionalEmpleado = empleadoRepository
                    .findById(asignacionEntity.getEmpleado().getId());
            if (optionalEmpleado.isEmpty()) {
                response.setMetada("Respuesta FALLIDA", "-1",
                        "el empleado con id " + asignacionEntity.getEmpleado().getId() + " no existe");
                return new ResponseEntity<AsignacionResponseRest>(response, HttpStatus.NOT_FOUND);
            }

            // Comprobar si ya existe una asignacion con el mismo empleado
            Optional<AsignacionEntity> asignacionOptionalEmpleado = asignacionRespository
                    .findByEmpleado(asignacionEntity.getEmpleado().getId());
            if (asignacionOptionalEmpleado.isPresent()) {

                // Comprobamos que la asignacion sea valida
                if (!asignacionOptionalEmpleado.get().getEstado().isEmpty()
                        && !asignacionOptionalEmpleado.get().getEstado().equals("INVALIDA")) {
                    log.error(asignacionOptionalEmpleado.get().getEstado().toString());
                    response.setMetada("Respuesta FALLIDA", "-1", "Ya existe una asignacion activa para "
                            + asignacionOptionalEmpleado.get().getEmpleado().getNombre());
                    return new ResponseEntity<AsignacionResponseRest>(response, HttpStatus.CONFLICT);
                }
            }

            // Guardar datos
            optionalEmpleado.get().setAsignacion(asignacionEntity);
            asignacionEntity.setEmpleado(optionalEmpleado.get());

            // Comprobar si existe la lista
            Optional<ListaEquiposEntity> optionalListaEquipos = listaEquiposRepository
                    .findById(asignacionEntity.getListaEquipos().getId());
            if (optionalListaEquipos.isEmpty()) {
                response.setMetada("Respuesta FALLIDA", "-1",
                        "No se encontro la lista de equipos, vuelve a intentar");
                return new ResponseEntity<AsignacionResponseRest>(response, HttpStatus.NOT_FOUND);
            }

            // Comprobar que la lista sea una de tipo asignacion
            if (!optionalListaEquipos.get().getTipo().equals("ASIGNACION")) {
                response.setMetada("Respuesta FALLIDA", "-1",
                        "la lista vinculada es un tipo de lista invalida, vuelve a intentar");
                return new ResponseEntity<AsignacionResponseRest>(response, HttpStatus.NOT_FOUND);
            }

            // Comprobar si ya existe una asignacion con la misma lista de equipos
            Optional<AsignacionEntity> asignacionOptionalList = asignacionRespository
                    .findByList(asignacionEntity.getListaEquipos().getId());
            if (asignacionOptionalList.isPresent()) {
                response.setMetada("Respuesta FALLIDA", "-1", "Ya existe una asignacion el conjunto de equipos");
                return new ResponseEntity<AsignacionResponseRest>(response, HttpStatus.CONFLICT);
            }

            // Guardar datos
            optionalListaEquipos.get().setAsignacion(asignacionEntity);
            asignacionEntity.setListaEquipos(optionalListaEquipos.get());

            // Asignar datos
            asignacionEntity.setEstado("PENDIENTE");
            asignacionEntity.setFecha_registro(new Date());

            // Registrar Asignacion
            AsignacionEntity asignacionGuardada = asignacionRespository.save(asignacionEntity);

            if (file.isEmpty()) {
                response.setMetada("Respuesta FALLIDA", "-1", "No se recibio la responsiva");
                return new ResponseEntity<AsignacionResponseRest>(response, HttpStatus.NOT_FOUND);
            }

            // Crear Responsiva
            ResponsivaEntity responsiva = new ResponsivaEntity();
            responsiva.setAsignacionActiva(asignacionGuardada);
            responsiva.setEstado(false);
            responsiva.setFecha_registro(new Date());

            // LLamar metodo para crear responsiva (aqui se vinvula la asignacion y la
            // responsiva)
            ResponsivaEntity existResponsiva = responsivaService.create(responsiva, file);

            // Crear la peticon para validar la asignacion
            PeticionesEntity peticion = new PeticionesEntity();

            peticion.setCategoria("ASIGNACIONES");
            peticion.setEstado("PENDIENTE");
            peticion.setId_entidad(asignacionGuardada.getId());
            peticion.setTipo_peticion("VALIDAR");

            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String username = authentication.getName(); // El username del usuario autenticado
            peticion.setUser(userRepository.findByUsername(username).get());

            peticion.setAnexo(urlUpload + existResponsiva.getPdf());

            peticionesService.create(peticion, file);

            List<AsignacionEntity> asignacion = new ArrayList<>();
            asignacion.add(asignacionGuardada);
            response.getAsignacionResponse().setAsignacion(asignacion);
            response.setMetada("Respuesta OK", "00", "Asignacion para "+
                    asignacionGuardada.getEmpleado().getNombre() + " creada exitosamente" );
        } catch (Exception e) {
            response.setMetada("Respuesta FALLIDA", "-1", "Respuesta fallida");
            log.error("Error al crear una asignacion", e);
            return new ResponseEntity<AsignacionResponseRest>(response, HttpStatus.INTERNAL_SERVER_ERROR);
        }
        return new ResponseEntity<AsignacionResponseRest>(response, HttpStatus.OK);

    }

    // Servicio para actualizar una asignacion y peticion
    @Override
    public ResponseEntity<AsignacionResponseRest> update(Integer id, Integer idPeticion,
            AsignacionEntity asignacionEntity, MultipartFile file) throws IOException {
        log.info("Actualizar asignacion y peticion");
        AsignacionResponseRest response = new AsignacionResponseRest();
        List<AsignacionEntity> list = new ArrayList<>();

        try {
            // Comprobar campos vacios
            if ((id == null || id <= 0)) {
                response.setMetada("Respuesta FALLIDA", "-1", "el id de la asignacion para actualizar esta vacio");
                return new ResponseEntity<AsignacionResponseRest>(response, HttpStatus.NOT_FOUND);
            }

            if ((idPeticion == null || id <= 0)) {
                response.setMetada("Respuesta FALLIDA", "-1", "el id de la peticion para actualizar esta vacio");
                return new ResponseEntity<AsignacionResponseRest>(response, HttpStatus.NOT_FOUND);
            }

            // Comprobar que la asignacion existe
            Optional<AsignacionEntity> asignacionOptional = asignacionRespository.findById(id);
            if (asignacionOptional.isEmpty()) {
                response.setMetada("Respuesta FALLIDA", "-1", "No se encontro la asignacion para actualizar ");
                return new ResponseEntity<AsignacionResponseRest>(response, HttpStatus.NOT_FOUND);
            }

            AsignacionEntity existAsignacion = asignacionOptional.get();

            // Comprobar que la ubicacion no este vacia
            if (asignacionEntity.getUbicacion() != null && !asignacionEntity.getUbicacion().isBlank()) {
                existAsignacion.setUbicacion(asignacionEntity.getUbicacion());
            }

            // Modificar datos de la asignacion
            existAsignacion.setEstado("PENDIENTE");

            Optional<ResponsivaEntity> saveResponsiva = responsivaRespository
                    .findById(existAsignacion.getResponsivaActiva().getId());
            if (saveResponsiva.isEmpty()) {
                response.setMetada("Respuesta FALLIDA", "-1", "No se encontro la responsiva para actualizar ");
                return new ResponseEntity<AsignacionResponseRest>(response, HttpStatus.NOT_FOUND);
            }
            saveResponsiva.get().setAsignacionActiva(null);
            responsivaRespository.save(saveResponsiva.get());
            existAsignacion.setResponsivaActiva(null);

            // Crea la responsiva y asigna la asignación ya guardada
            ResponsivaEntity responsiva = new ResponsivaEntity();
            responsiva.setAsignacionActiva(existAsignacion);
            responsiva.setEstado(false);
            responsiva.setFecha_registro(new Date());

            // Guarda la responsiva
            ResponsivaEntity responsivaGuardada = responsivaService.create(responsiva, file);

            // Asocia la responsiva a la asignación y guarda la asignación de nuevo
            existAsignacion.setResponsivaActiva(responsivaGuardada);

            asignacionRespository.save(existAsignacion);

            // Se actualiza la peticon
            try {
                peticionesService.upload(idPeticion, urlUpload + responsivaGuardada.getPdf());
            } catch (Exception e) {
                response.setMetada("Respuesta FALLIDA", "-1", "Error al crear la asignacion");
                log.error("Error al crear la peticion", e);
                return new ResponseEntity<AsignacionResponseRest>(response, HttpStatus.INTERNAL_SERVER_ERROR);
            }

            list.add(existAsignacion);
            response.getAsignacionResponse().setAsignacion(list);
            response.setMetada("Respuesta OK", "00", "Respuesta exitosa");
        } catch (Exception e) {
            response.setMetada("Respuesta FALLIDA", "-1", "Respuesta fallida");
            log.error("Error al crear una asignacion", e);
            return new ResponseEntity<AsignacionResponseRest>(response, HttpStatus.INTERNAL_SERVER_ERROR);
        }
        return new ResponseEntity<AsignacionResponseRest>(response, HttpStatus.OK);

    }

    // Servicio para actualizar los datos de una asignacion activa
    @Override
    public ResponseEntity<AsignacionResponseRest> updateData(Integer id, AsignacionEntity asignacionEntity,
            ListaEquiposEntity listaEquiposEntity, MultipartFile file) throws IOException {
        log.info("Actualizar asignacion datos");
        AsignacionResponseRest response = new AsignacionResponseRest();
        List<AsignacionEntity> list = new ArrayList<>();

        try {
            // Comprobar campos vacios
            if ((id == null || id <= 0)) {
                response.setMetada("Respuesta FALLIDA", "-1", "el id de la asignacion para actualizar esta vacio");
                return new ResponseEntity<AsignacionResponseRest>(response, HttpStatus.NOT_FOUND);
            }

            // Comprobar que la asignacion existe
            Optional<AsignacionEntity> asignacionOptional = asignacionRespository.findById(id);
            if (asignacionOptional.isEmpty()) {
                response.setMetada("Respuesta FALLIDA", "-1", "No se encontro la asignacion para actualizar ");
                return new ResponseEntity<AsignacionResponseRest>(response, HttpStatus.NOT_FOUND);
            }

            // Guardar asignacion existente y crear una asignacionHistory para tenerla de referencia
            AsignacionEntity existAsignacion = asignacionOptional.get();

            AsignacionHistoryEntity asignacionHistoryEntity = new AsignacionHistoryEntity();

            // Guardar datos de respaldo
            // Comproba que no haya sido actualizada previamente
            if (existAsignacion.getAsignacionHistory() != null) {
                asignacionHistoryEntity = existAsignacion.getAsignacionHistory();
                asignacionHistoryEntity.setIdEquipos(null);
                asignacionHistoryEntity.setUbicacion(null);
                asignacionHistoryEntity.setFecha_registro(null);
                asignacionHistoryEntity.setIdResponsivaActiva(null);
            }else {
                asignacionHistoryEntity.setAsignacion(existAsignacion);
            }

            // Guardar fecha
            if (existAsignacion.getFecha_modificacion() != null) {
                asignacionHistoryEntity.setFecha_registro(existAsignacion.getFecha_modificacion());
            } else {
                asignacionHistoryEntity.setFecha_registro(null);
            }

            // Guardar Id de Responsiva
            asignacionHistoryEntity.setIdResponsivaActiva(existAsignacion.getResponsivaActiva().getId());

            // Comprobar que la ubicacion no este vacia
            if (asignacionEntity.getUbicacion() != null && !asignacionEntity.getUbicacion().isBlank()) {
                asignacionHistoryEntity.setUbicacion(existAsignacion.getUbicacion());
                existAsignacion.setUbicacion(asignacionEntity.getUbicacion());
            }

            // Guardar Ids de equipos
            // Comprobar que la lista no este vacia
            if (listaEquiposEntity.getEquipoTecnologico() != null) {
                // Guardar ids en una lista
                List<Integer> idsEquipos = new ArrayList<>();
                for (EquipoTecnologicoEntity equipo : existAsignacion.getListaEquipos().getEquipoTecnologico()) {
                    idsEquipos.add(equipo.getId());
                }
                asignacionHistoryEntity.setIdEquiposList(idsEquipos);

                // Actualizar lista de equipos
                ListaEquiposEntity existList = existAsignacion.getListaEquipos();

                // Obtener los equipos actuales en la lista
                List<EquipoTecnologicoEntity> equiposActuales = existList.getEquipoTecnologico();
                List<Integer> idsNuevos = listaEquiposEntity.getEquipoTecnologico()
                        .stream()
                        .map(EquipoTecnologicoEntity::getId)
                        .collect(Collectors.toList());

                // Desasociar equipos que ya no están y actualizar su estado
                for (EquipoTecnologicoEntity equipo : equiposActuales) {
                    if (!idsNuevos.contains(equipo.getId())) {
                        equipo.setListaEquipos(null);
                        equipo.setEstado("PENDIENTE");
                        equipoRepository.save(equipo);
                    }
                }

                // Preparar nuevos equipos
                List<EquipoTecnologicoEntity> nuevosEquipos = new ArrayList<>();
                for (EquipoTecnologicoEntity equipo : listaEquiposEntity.getEquipoTecnologico()) {
                    Optional<EquipoTecnologicoEntity> equipoOptional = equipoRepository.findById(equipo.getId());
                    if (!equipoOptional.isPresent()) {
                        response.setMetada("Respuesta FALLIDA", "-1", "No se encontró uno de los equipos de la lista");
                        return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
                    }
                    EquipoTecnologicoEntity existEquipo = equipoOptional.get();
                    // Validaciones adicionales
                    if (existEquipo.getListaEquipos() != null && !existEquipo.getListaEquipos().getId().equals(existAsignacion.getListaEquipos().getId())) {
                        if (List.of("DONADO", "DESECHADO", "ASIGNADO").contains(existEquipo.getEstado())) {
                            response.setMetada("Respuesta FALLIDA", "-1",
                                    "El equipo con ID " + equipo.getId() + " está asignado a otro listado");
                            return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
                        }
                    }

                    if (existEquipo.getListaEquipos() == null && existEquipo.getEstado().equals("ACTIVO")) {
                        existEquipo.setEstado("PENDIENTE");
                    }

                    existEquipo.setListaEquipos(existList);
                    nuevosEquipos.add(existEquipo);
                }

                // Reemplazar la lista de equipos en la lista existente
                existList.setEquipoTecnologico(nuevosEquipos);
                listaEquiposRepository.save(existList);
            }


            if ((file == null && file.isEmpty())) {
                response.setMetada("Respuesta FALLIDA", "-1",
                        "No se recibio una nueva responsiva ");
                return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
            }

            // Guardar id de la responsiva activa
            asignacionHistoryEntity.setIdResponsivaActiva(existAsignacion.getResponsivaActiva().getId());
            // Guardar Asignacion de referencia
            AsignacionHistoryEntity saveAsignacionHistory = asignacionHistoryRepository.save(asignacionHistoryEntity);
            existAsignacion.setAsignacionHistory(saveAsignacionHistory);
            // Actualizar responsiva
            // Desvincular responsiva Activa
            Optional<ResponsivaEntity> existResponsiva = responsivaRespository.findById(existAsignacion.getResponsivaActiva().getId());
            existResponsiva.get().setAsignacionActiva(null);
            existResponsiva.get().setEstado(false);
            responsivaRespository.save(existResponsiva.get());

            // Crear nueva responsiva
            ResponsivaEntity responsiva = new ResponsivaEntity();
            responsiva.setAsignacionActiva(existAsignacion);
            responsiva.setEstado(false);
            responsiva.setFecha_registro(new Date());

            // LLamar metodo para crear responsiva (aqui se vinvula la asignacion y la responsiva)
            ResponsivaEntity saveResponsiva = responsivaService.create(responsiva, file);

            // Asocia la responsiva a la asignación y guarda la asignación de nuevo
            existAsignacion.setResponsivaActiva(saveResponsiva);

            // Modificar datos de la asignacion
            existAsignacion.setEstado("PENDIENTE");
            existAsignacion.setFecha_modificacion(new Date());

            asignacionRespository.save(existAsignacion);

            // Se crea la peticon para validar la actualizacion de la asignacion
            try {
                PeticionesEntity peticion = new PeticionesEntity();

                peticion.setCategoria("ASIGNACIONES");
                peticion.setEstado("PENDIENTE");
                peticion.setId_entidad(existAsignacion.getId());
                peticion.setTipo_peticion("ACTUALIZAR");

                Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
                String username = authentication.getName(); // El username del usuario autenticado
                peticion.setUser(userRepository.findByUsername(username).get());

                peticion.setAnexo(urlUpload + saveResponsiva.getPdf());

                peticionesService.create(peticion, file);

            } catch (Exception e) {
                response.setMetada("Respuesta FALLIDA", "-1", "Error al crear la asignacion");
                log.error("Error al crear la peticion", e);
                return new ResponseEntity<AsignacionResponseRest>(response, HttpStatus.INTERNAL_SERVER_ERROR);
            }

            list.add(existAsignacion);
            response.getAsignacionResponse().setAsignacion(list);
            response.setMetada("Respuesta OK", "00", "Se ha enviado una peticion para actualizar la asignacion de " +
                    existAsignacion.getEmpleado().getNombre());
        } catch (

        Exception e) {
            response.setMetada("Respuesta FALLIDA", "-1", "Respuesta fallida");
            log.error("Error al crear una asignacion", e);
            return new ResponseEntity<AsignacionResponseRest>(response, HttpStatus.INTERNAL_SERVER_ERROR);
        }
        return new ResponseEntity<AsignacionResponseRest>(response, HttpStatus.OK);

    }

    // Servicio para actualizar la responsiva de una asigncacion
    @Override
    public ResponseEntity<AsignacionResponseRest> updateResponsiva(Integer id, Integer idPeticion, MultipartFile file)
            throws IOException {
        log.info("Actualizar asignacion responsiva");
        AsignacionResponseRest response = new AsignacionResponseRest();
        List<AsignacionEntity> list = new ArrayList<>();

        try {
            // Comprobar campos vacios
            if (id == null || id <= 0) {
                response.setMetada("Respuesta FALLIDA", "-1", "No se recibio la asignacion");
                return new ResponseEntity<AsignacionResponseRest>(response, HttpStatus.NOT_FOUND);
            }

            if (file == null || file.isEmpty()) {
                response.setMetada("Respuesta FALLIDA", "-1", "No se recibio la responsiva firmada");
                return new ResponseEntity<AsignacionResponseRest>(response, HttpStatus.NOT_FOUND);
            }

            // Comprobar que la asignacion existe
            Optional<AsignacionEntity> asignacionOptional = asignacionRespository.findById(id);
            if (asignacionOptional.isEmpty()) {
                response.setMetada("Respuesta FALLIDA", "-1", "No se encontro la asignacion para actualizar ");
                return new ResponseEntity<AsignacionResponseRest>(response, HttpStatus.NOT_FOUND);
            }

            AsignacionEntity existAsignacion = asignacionOptional.get();

            // Actualiza la responsiva, Asocia la responsiva a la asignación y guarda la
            // asignación de nuevo
            ResponsivaEntity responsivaGuardada = responsivaService
                    .updatee(existAsignacion.getResponsivaActiva().getId(), file);
            asignacionRespository.save(existAsignacion);

            // Actualizar la peticion
            peticionesService.upload(idPeticion, urlUpload + responsivaGuardada.getPdf());

            list.add(existAsignacion);
            response.getAsignacionResponse().setAsignacion(list);
            response.setMetada("Respuesta OK", "00", "Respuesta exitosa");
        } catch (IllegalArgumentException e) {
            log.error("Error al crear una asignacion", e);
            response.setMetada("Respuesta FALLIDA", "-1", e.getMessage());
            return new ResponseEntity<AsignacionResponseRest>(response, HttpStatus.INTERNAL_SERVER_ERROR);
        } catch (Exception e) {
            log.error("Error al crear una asignacion", e);
            response.setMetada("Respuesta FALLIDA", "-1", "Respuesta fallida");
            return new ResponseEntity<AsignacionResponseRest>(response, HttpStatus.INTERNAL_SERVER_ERROR);
        }
        return new ResponseEntity<AsignacionResponseRest>(response, HttpStatus.OK);

    }

    // Servicio para deshacer una asignacion
    @Override
    public ResponseEntity<AsignacionResponseRest> delete(Integer id) {
        log.info("Borrar asignacion");
        AsignacionResponseRest response = new AsignacionResponseRest();

        try {
            // Comprobar que se recibio un valor
            if (id == null || id <= 0) {
                response.setMetada("Respuesta FALLIDA", "-1", "No se recibio una peticion, vuelve a intentarlo");
                return new ResponseEntity<AsignacionResponseRest>(response, HttpStatus.NOT_FOUND);
            }
            // Comprobar que la lista existe
            Optional<AsignacionEntity> asignacionOptional = asignacionRespository.findById(id);
            if (asignacionOptional.isEmpty()) {
                response.setMetada("Respuesta FALLIDA", "-1", "No se encontro la asignacion");
                return new ResponseEntity<AsignacionResponseRest>(response, HttpStatus.NOT_FOUND);
            }

            AsignacionEntity existAsignacion = asignacionOptional.get();

            existAsignacion.setEstado("PENDIENTE");
            existAsignacion.setFecha_modificacion(new Date());

            // Se crea la peticion para su validacion
            PeticionesEntity peticion = new PeticionesEntity();

            peticion.setCategoria("ASIGNACIONES");
            peticion.setEstado("PENDIENTE");
            peticion.setId_entidad(existAsignacion.getId());
            peticion.setTipo_peticion("DESHACER");

            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String username = authentication.getName(); // El username del usuario autenticado
            peticion.setUser(userRepository.findByUsername(username).get());
            // Obtener la responsiva activa para añadirla anexo de la peticion
            Optional<ResponsivaEntity> existResponsiva = responsivaRespository
                    .findById(existAsignacion.getResponsivaActiva().getId());
            if (existResponsiva.isEmpty()) {
                response.setMetada("Respuesta FALLIDA", "-1", "No se encontro la responsiva activa");
                return new ResponseEntity<AsignacionResponseRest>(response, HttpStatus.NOT_FOUND);
            }
            peticion.setAnexo(urlUpload + existResponsiva.get().getPdf());

            asignacionRespository.save(existAsignacion);
            peticionesService.create(peticion, null);

            List<AsignacionEntity> list = new ArrayList<>();
            list.add(existAsignacion);
            response.getAsignacionResponse().setAsignacion(list);
            response.setMetada("Respuesta OK", "00", "Se ha enviado una peticion para deshacer la asignacion de " + existAsignacion.getEmpleado().getNombre());
        } catch (Exception e) {
            response.setMetada("Respuesta FALLIDA", "-1", "Respuesta fallida");
            log.error("Error al eliminar una asignacion", e);
            return new ResponseEntity<AsignacionResponseRest>(response, HttpStatus.INTERNAL_SERVER_ERROR);
        }
        return new ResponseEntity<AsignacionResponseRest>(response, HttpStatus.OK);

    }

    // Servicio para obtener todas las asignaciones
    @Override
    public ResponseEntity<AsignacionResponseRest> getAll() {
        log.info("Buscando asignaciones");
        AsignacionResponseRest response = new AsignacionResponseRest();

        try {
            List<AsignacionEntity> asinaciones = asignacionRespository.findAll();

            // Comprobar si la lista de asignaciones está vacía
            if (asinaciones.isEmpty()) {
                response.setMetada("Respuesta FALLIDA", "-1", "No se encontraron asignaciones");
                return new ResponseEntity<AsignacionResponseRest>(response, HttpStatus.NOT_FOUND);
            }
            for (AsignacionEntity asignacion : asinaciones) {

                ResponsivaEntity saveResponsiva = responsivaRespository
                        .findById(asignacion.getResponsivaActiva().getId()).get();
                if (saveResponsiva.getPdf() != null && !saveResponsiva.getPdf().isEmpty()) {
                    saveResponsiva.setPdf(url + saveResponsiva.getPdf());
                }

                EmpleadoEntity saveEmpleado = empleadoRepository.findById(asignacion.getEmpleado().getId()).get();
                if (saveEmpleado.getFoto() != null && !saveEmpleado.getFoto().isEmpty()) {
                    saveEmpleado.setFoto(urlEmpleado + saveEmpleado.getFoto());
                }
            }

            response.getAsignacionResponse().setAsignacion(asinaciones);
            response.setMetada("Respuesta OK", "00", "Respuesta exitosa");
        } catch (Exception e) {
            response.setMetada("Respuesta FALLIDA", "-1", "Respuesta fallida");
            log.error("Error al buscar asignaciones", e.getMessage());
            return new ResponseEntity<AsignacionResponseRest>(response, HttpStatus.INTERNAL_SERVER_ERROR);
        }
        return new ResponseEntity<AsignacionResponseRest>(response, HttpStatus.OK);
    }

    // Servicio para obtener una asignacion por id
    @Override
    public ResponseEntity<AsignacionResponseRest> getById(Integer id) {
        log.info("Buscando asignaciones");
        AsignacionResponseRest response = new AsignacionResponseRest();
        List<AsignacionEntity> list = new ArrayList<>();
        try {
            Optional<AsignacionEntity> asignacionOptional = asignacionRespository.findById(id);

            // Comprobar si la asignacion está vacía
            if (asignacionOptional.isEmpty()) {
                response.setMetada("Respuesta FALLIDA", "-1", "No se encontro la asignacion con id " + id);
                return new ResponseEntity<AsignacionResponseRest>(response, HttpStatus.NOT_FOUND);
            }

            AsignacionEntity asignacion = asignacionOptional.get();

            ResponsivaEntity saveResponsiva = responsivaRespository.findById(asignacion.getResponsivaActiva().getId())
                    .get();
            if (saveResponsiva.getPdf() != null && !saveResponsiva.getPdf().isEmpty()) {
                saveResponsiva.setPdf(url + saveResponsiva.getPdf());
            }

            EmpleadoEntity saveEmpleado = empleadoRepository.findById(asignacion.getEmpleado().getId()).get();
            if (saveEmpleado.getFoto() != null && !saveEmpleado.getFoto().isEmpty()) {
                saveEmpleado.setFoto(urlEmpleado + saveEmpleado.getFoto());
            }

            list.add(asignacion);
            response.getAsignacionResponse().setAsignacion(list);
            response.setMetada("Respuesta OK", "00", "Respuesta exitosa");
        } catch (Exception e) {
            response.setMetada("Respuesta FALLIDA", "-1", "Respuesta fallida");
            log.error("Error al buscar asignaciones", e);
            return new ResponseEntity<AsignacionResponseRest>(response, HttpStatus.INTERNAL_SERVER_ERROR);
        }
        return new ResponseEntity<AsignacionResponseRest>(response, HttpStatus.OK);
    }
}
