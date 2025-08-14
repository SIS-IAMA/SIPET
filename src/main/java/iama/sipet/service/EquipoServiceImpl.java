package iama.sipet.service;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.*;

import iama.sipet.response.ListaEquipoResponseRest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.multipart.MultipartFile;

import iama.sipet.entity.EquipoTecnologicoEntity;
import iama.sipet.entity.ListaEquiposEntity;
import iama.sipet.entity.PeticionesEntity;
import iama.sipet.repository.EquipoTecnologicoRespository;
import iama.sipet.repository.ListaEquiposRepository;
import iama.sipet.repository.UserRepository;
import iama.sipet.response.EquiposResponseRest;

@Service
public class EquipoServiceImpl implements IEquipoService {
    private static final Logger log = LoggerFactory.getLogger(EmpleadoServiceImpl.class);

    @Autowired
    private EquipoTecnologicoRespository equipoRepository;

    @Autowired
    private ListaEquiposRepository listaRespository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PeticionesServiceImpl peticionesService;

    @Autowired
    private UploadService uploadService;

    String url = "https://sipet-iama.onrender.com/upload/FotosEquipos/";
    String urlDonacion = "https://sipet-iama.onrender.com/upload/ListasEquipos/Donacion/";
    String urlDesecho = "https://sipet-iama.onrender.com/upload/ListasEquipos/Desecho/";
    String urlUpload = "upload/FotosEquipos/";
    String urlUploadDonacion = "upload/ListasEquipos/Donacion/";
    String urlUploadDesecho = "upload/ListasEquipos/Desecho/";

    @Override
    public ResponseEntity<EquiposResponseRest> crear(EquipoTecnologicoEntity equipoTecnologicoEntity,
            MultipartFile file) throws IOException {
        log.info("Creando equipo" + equipoTecnologicoEntity.toString());
        String encode = URLEncoder.encode(Objects.requireNonNull(file.getOriginalFilename()), StandardCharsets.UTF_8)
                .replace("+", "-");
        EquiposResponseRest response = new EquiposResponseRest();
        try {

            // Comprobar si ya existe un equipo con el mismos numero de serie
            Optional<EquipoTecnologicoEntity> existingEquipo = equipoRepository
                    .findByNumeroSerie(equipoTecnologicoEntity.getNumero_serie());
            if (existingEquipo.isPresent()) {
                response.setMetada("Respuesta FALLIDA", "-1", "Ya existe un equipo con el mismo número de serie");
                return new ResponseEntity<EquiposResponseRest>(response, HttpStatus.CONFLICT);
            }

            // Comprobar campos vacios private String tipo; private int numero_serie;
            // private String marca; private String modelo;
            if (equipoTecnologicoEntity.getTipo() == null ||
                    equipoTecnologicoEntity.getNumero_serie() == null ||
                    equipoTecnologicoEntity.getMarca() == null ||
                    equipoTecnologicoEntity.getModelo() == null) {
                response.setMetada("Respuesta FALLIDA", "-1", "Campos obligatorios no pueden estar vacíos");
                log.info(equipoTecnologicoEntity.toString());
                return new ResponseEntity<EquiposResponseRest>(response, HttpStatus.BAD_REQUEST);
            }

            // Verificar si el archivo es nulo o vacío
            if (encode != null && !encode.isEmpty()) {
                String name = uploadService.saveUpload(file, urlUpload);
                equipoTecnologicoEntity.setFoto(name);
            }

            equipoTecnologicoEntity.setEstado("ACTIVO");

            // equipoTecnologicoEntity.setFecha_registro(java.time.LocalDate.now());

            // Guardar el equipo
            EquipoTecnologicoEntity savedEquipo = equipoRepository.save(equipoTecnologicoEntity);

            List<EquipoTecnologicoEntity> equipo = new ArrayList<>();
            equipo.add(savedEquipo);
            response.getEquiposResponse().setEquipo(equipo);
            response.setMetada("Respuesta OK", "00", "Respuesta exitosa");
        } catch (Exception e) {
            response.setMetada("Respuesta FALLIDA", "-1", "Respuesta fallida");
            log.error("Error al crear empleado", e);
            e.getStackTrace();
            return new ResponseEntity<EquiposResponseRest>(response, HttpStatus.INTERNAL_SERVER_ERROR);
        }
        return new ResponseEntity<EquiposResponseRest>(response, HttpStatus.CREATED);
    }

    @Override
    public ResponseEntity<EquiposResponseRest> buscarEquipos() {
        log.info("Buscando equipos");
        EquiposResponseRest response = new EquiposResponseRest();

        try {
            // Obtener todos los equipos "ACTIVO", "ASIGNADO" y "PENDIENTE"
            List<EquipoTecnologicoEntity> equipos = equipoRepository.findByEstado();

            // Verificar si se encontraron usuarios
            if (equipos.isEmpty()) {
                log.info("No se encontraron equipos");
                response.setMetada("Respuesta OK", "00", "No se encontraron equipos");
                return new ResponseEntity<EquiposResponseRest>(response, HttpStatus.NOT_FOUND);
            }

            for (EquipoTecnologicoEntity equipo : equipos) {
                if (equipo.getFoto() != null && !equipo.getFoto().isEmpty()) {
                    equipo.setFoto(url + equipo.getFoto());
                }
            }

            response.getEquiposResponse().setEquipo(equipos);
            response.setMetada("Respuesta OK", "00", "Respuesta exitosa");
        } catch (Exception e) {
            response.setMetada("Respuesta FALLIDA", "-1", "Respuesta fallida");
            log.error("Error al buscar equipos", e);
            e.getStackTrace();
            return new ResponseEntity<EquiposResponseRest>(response, HttpStatus.INTERNAL_SERVER_ERROR);
        }
        return new ResponseEntity<EquiposResponseRest>(response, HttpStatus.OK);
    }

    @Override
    public ResponseEntity<EquiposResponseRest> buscarEquiposActivos() {
        log.info("Buscando equipos");
        EquiposResponseRest response = new EquiposResponseRest();

        try {
            // Obtener todos los equipos "ACTIVO", "ASIGNADO" y "PENDIENTE"
            List<EquipoTecnologicoEntity> equipos = equipoRepository.findByEstadoActivo();

            // Verificar si se encontraron usuarios
            if (equipos.isEmpty()) {
                log.info("No se encontraron equipos");
                response.setMetada("Respuesta OK", "00", "No se encontraron equipos");
                return new ResponseEntity<EquiposResponseRest>(response, HttpStatus.NOT_FOUND);
            }

            for (EquipoTecnologicoEntity equipo : equipos) {
                if (equipo.getFoto() != null && !equipo.getFoto().isEmpty()) {
                    equipo.setFoto(url + equipo.getFoto());
                }
            }

            response.getEquiposResponse().setEquipo(equipos);
            response.setMetada("Respuesta OK", "00", "Respuesta exitosa");
        } catch (Exception e) {
            response.setMetada("Respuesta FALLIDA", "-1", "Respuesta fallida");
            log.error("Error al buscar equipos", e);
            e.getStackTrace();
            return new ResponseEntity<EquiposResponseRest>(response, HttpStatus.INTERNAL_SERVER_ERROR);
        }
        return new ResponseEntity<EquiposResponseRest>(response, HttpStatus.OK);
    }

    @Override
    public ResponseEntity<EquiposResponseRest> buscarEquiposPorIds(List<Integer> id) {
        log.info("Buscando equipos con id ", id);
        EquiposResponseRest response = new EquiposResponseRest();

        try {
            // Obtener todos los equipos "ACTIVO", "ASIGNADO" y "PENDIENTE"
            List<EquipoTecnologicoEntity> equipos = equipoRepository.findAllById(id);

            // Verificar si se encontraron usuarios
            if (equipos.isEmpty()) {
                log.info("No se encontraron equipos");
                response.setMetada("Respuesta OK", "00", "No se encontraron equipos");
                return new ResponseEntity<EquiposResponseRest>(response, HttpStatus.NOT_FOUND);
            }

            for (EquipoTecnologicoEntity equipo : equipos) {
                if (equipo.getFoto() != null && !equipo.getFoto().isEmpty()) {
                    equipo.setFoto(url + equipo.getFoto());
                }
            }

            response.getEquiposResponse().setEquipo(equipos);
            response.setMetada("Respuesta OK", "00", "Respuesta exitosa");
        } catch (Exception e) {
            response.setMetada("Respuesta FALLIDA", "-1", "Respuesta fallida");
            log.error("Error al buscar equipos", e);
            e.getStackTrace();
            return new ResponseEntity<EquiposResponseRest>(response, HttpStatus.INTERNAL_SERVER_ERROR);
        }
        return new ResponseEntity<EquiposResponseRest>(response, HttpStatus.OK);
    }

    @Override
    public ResponseEntity<EquiposResponseRest> buscarPorId(Integer id) {
        log.info("Buscar por ID");
        EquiposResponseRest response = new EquiposResponseRest();
        List<EquipoTecnologicoEntity> list = new ArrayList<>();
        try {
            Optional<EquipoTecnologicoEntity> equipo = equipoRepository.findById(id);

            // Verificar si el equipo existe
            if (equipo.isPresent()) {

                EquipoTecnologicoEntity equipoTecnologicoEntity = equipo.get();

                if (equipoTecnologicoEntity.getFoto() != null && !equipoTecnologicoEntity.getFoto().isEmpty()) {
                    equipoTecnologicoEntity.setFoto(url + equipoTecnologicoEntity.getFoto());
                }

                list.add(equipoTecnologicoEntity);
                response.getEquiposResponse().setEquipo(list);
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
            return new ResponseEntity<EquiposResponseRest>(response, HttpStatus.INTERNAL_SERVER_ERROR);
        }
        return new ResponseEntity<EquiposResponseRest>(response, HttpStatus.OK);
    }

    @Override
    public ResponseEntity<EquiposResponseRest> actualizar(EquipoTecnologicoEntity equipoTecnologicoEntity, Integer id,
            MultipartFile file) throws IOException {
        log.info("Actualizando el equipo");
        String encode = URLEncoder.encode(Objects.requireNonNull(file.getOriginalFilename()), StandardCharsets.UTF_8)
                .replace("+", "-");
        EquiposResponseRest response = new EquiposResponseRest();
        List<EquipoTecnologicoEntity> list = new ArrayList<>();
        try {
            // Verificar si el equipo existe
            Optional<EquipoTecnologicoEntity> equipoOptional = equipoRepository.findById(id);
            if (!equipoOptional.isPresent()) {
                response.setMetada("Respuesta no encontrada", "-1", "Equipo no encontrado");
                return new ResponseEntity<EquiposResponseRest>(response, HttpStatus.NOT_FOUND);
            }

            EquipoTecnologicoEntity existingEquipo = equipoOptional.get();

            // Verificar que este en un estado valido
            if (!existingEquipo.getEstado().equals("ACTIVO")) {
                response.setMetada("Respuesta FALLIDA", "-1",
                        "El equipo debe estar en un estado activo para poder actualizar los datos");
                return new ResponseEntity<EquiposResponseRest>(response, HttpStatus.CONFLICT);
            }

            // Comprobar si el número de serie ya existe en otro equipo
            if (equipoTecnologicoEntity.getNumero_serie() != existingEquipo.getNumero_serie()) {
                Optional<EquipoTecnologicoEntity> existingByNumeroSerie = equipoRepository
                        .findByNumeroSerie(equipoTecnologicoEntity.getNumero_serie());
                if (existingByNumeroSerie.isPresent()
                        && !existingByNumeroSerie.get().getId().equals(existingEquipo.getId())) {
                    response.setMetada("Respuesta FALLIDA", "-1", "Ya existe un equipo con el mismo número de serie");
                    return new ResponseEntity<EquiposResponseRest>(response, HttpStatus.CONFLICT);
                }
            }

            boolean sinCambios = true;

            // Comprobar Tipo de Equipo
            if (equipoTecnologicoEntity.getTipo() != null && !equipoTecnologicoEntity.getTipo().isEmpty()
                    && !equipoTecnologicoEntity.getTipo().equals(existingEquipo.getTipo())) {
                existingEquipo.setTipo(equipoTecnologicoEntity.getTipo());
                sinCambios = false;
            }

            // Comprobar Numero de Serie
            if (equipoTecnologicoEntity.getNumero_serie() != null
                    && !equipoTecnologicoEntity.getNumero_serie().isEmpty()
                    && !equipoTecnologicoEntity.getNumero_serie().equals(existingEquipo.getNumero_serie())) {
                existingEquipo.setNumero_serie(equipoTecnologicoEntity.getNumero_serie());
                sinCambios = false;
            }

            // Comprobar Marca
            if (equipoTecnologicoEntity.getMarca() != null && !equipoTecnologicoEntity.getMarca().isEmpty()
                    && !equipoTecnologicoEntity.getMarca().equals(existingEquipo.getMarca())) {
                existingEquipo.setMarca(equipoTecnologicoEntity.getMarca());
                sinCambios = false;
            }

            // Comprobar Modelo
            if (equipoTecnologicoEntity.getModelo() != null && !equipoTecnologicoEntity.getModelo().isEmpty()
                    && !equipoTecnologicoEntity.getModelo().equals(existingEquipo.getModelo())) {
                existingEquipo.setModelo(equipoTecnologicoEntity.getModelo());
                sinCambios = false;
            }

            // Comprobar Sistema Operativo
            if (equipoTecnologicoEntity.getSistema_operativo() != null
                    && !equipoTecnologicoEntity.getSistema_operativo().isEmpty() &&
                    !equipoTecnologicoEntity.getSistema_operativo().equals(existingEquipo.getSistema_operativo())) {
                existingEquipo.setSistema_operativo(equipoTecnologicoEntity.getSistema_operativo());
                sinCambios = false;
            }

            // Comprobar RAM
            if (equipoTecnologicoEntity.getRam() > 0 && equipoTecnologicoEntity.getRam() != existingEquipo.getRam()) {
                existingEquipo.setRam(equipoTecnologicoEntity.getRam());
                sinCambios = false;
            }

            // Comprobar RAM
            if (equipoTecnologicoEntity.getAlmacenamiento() > 0
                    && equipoTecnologicoEntity.getAlmacenamiento() != existingEquipo.getAlmacenamiento()) {
                existingEquipo.setAlmacenamiento(equipoTecnologicoEntity.getAlmacenamiento());
                sinCambios = false;
            }

            // Comprobar Procesador
            if (equipoTecnologicoEntity.getProcesador() != null && !equipoTecnologicoEntity.getProcesador().isEmpty() &&
                    !equipoTecnologicoEntity.getProcesador().equals(existingEquipo.getProcesador())) {
                existingEquipo.setProcesador(equipoTecnologicoEntity.getProcesador());
                sinCambios = false;
            }

            // Comprobar Comentario
            if (equipoTecnologicoEntity.getComentario() != null && !equipoTecnologicoEntity.getComentario().isEmpty() &&
                    !equipoTecnologicoEntity.getComentario().equals(existingEquipo.getComentario())) {
                existingEquipo.setComentario(equipoTecnologicoEntity.getComentario());
                sinCambios = false;
            }

            // Verificar si se proporciona un archivo para actualizar la foto
            if ((encode != null && !encode.isEmpty()) && (file != null && !file.isEmpty())) {
                // Si el equipo tiene una foto previa, se elimina del servidor
                String nombreFotoAnterior = existingEquipo.getFoto();
                if (nombreFotoAnterior != null && !nombreFotoAnterior.isEmpty()) {
                    uploadService.deleteUpload(nombreFotoAnterior, urlUpload);
                }

                // Guardar la nueva foto y actualizar el nombre en el equipo
                String nuevaFoto = uploadService.saveUpload(file, urlUpload);
                existingEquipo.setFoto(nuevaFoto);
                sinCambios = false;
            }

            if (sinCambios == true) {
                response.setMetada("Respuesta FALLIDA", "-1", "No se han realizado cambios en el equipo");
                return new ResponseEntity<EquiposResponseRest>(response, HttpStatus.BAD_REQUEST);
            }

            // Guardar los cambios en la base de datos
            EquipoTecnologicoEntity updatedEquipo = equipoRepository.save(existingEquipo);
            updatedEquipo.setFoto(url + updatedEquipo.getFoto());
            list.add(updatedEquipo);
            response.getEquiposResponse().setEquipo(list);
            response.setMetada("Respuesta OK", "00", "Equipo actualizado correctamente");

        } catch (Exception e) {
            response.setMetada("Respuesta FALLIDA", "-1", "Respuesta fallida");
            log.error("Error al actualizar el equipo", e);
            e.getStackTrace();
            return new ResponseEntity<EquiposResponseRest>(response, HttpStatus.INTERNAL_SERVER_ERROR);
        }
        return new ResponseEntity<EquiposResponseRest>(response, HttpStatus.OK);
    }

    @Override
    public ResponseEntity<ListaEquipoResponseRest> listar(List<Integer> id, String tipo, MultipartFile file)
            throws IOException {
        log.info("Listar equipos por ID");
        String encode = URLEncoder.encode(Objects.requireNonNull(file.getOriginalFilename()), StandardCharsets.UTF_8)
                .replace("+", "-");
        ListaEquipoResponseRest response = new ListaEquipoResponseRest();
        List<ListaEquiposEntity> list = new ArrayList<>();
        try {
            ListaEquiposEntity savedList = new ListaEquiposEntity();

            // Verificar si la lista de IDs está vacía
            if (id.isEmpty()) {
                response.setMetada("Respuesta FALLIDA", "-1", "La lista de equipos está vacía");
                return new ResponseEntity<ListaEquipoResponseRest>(response, HttpStatus.BAD_REQUEST);
            }

            // Convertir tipo a mayúsculas para evitar problemas de comparación
            tipo = tipo.toUpperCase();
            // Verificar si el tipo es válido
            if (tipo == null || tipo.isEmpty()
                    || (!tipo.equals("DONACION") && !tipo.equals("DESECHO") && !tipo.equals("ASIGNACION"))) {
                response.setMetada("Respuesta FALLIDA", "-1", "Tipo de lista no válido");
                return new ResponseEntity<ListaEquipoResponseRest>(response, HttpStatus.BAD_REQUEST);
            }

            savedList.setTipo(tipo);

            if (file.isEmpty() && !tipo.equals("ASIGNACION")) {
                response.setMetada("Respuesta FALLIDA", "-1", "No se adjunto ningun archivo");
                return new ResponseEntity<ListaEquipoResponseRest>(response, HttpStatus.NOT_FOUND);
            }

            // Verificar si el archivo es nulo o vacío
            if (encode != null && !encode.isEmpty() && !tipo.equals("ASIGNACION")) {
                if (tipo.equals("DONACION")) {
                    savedList.setPdf(uploadService.saveUpload(file, urlUploadDonacion));// url para donacion
                } else {
                    if (tipo.equals("DESECHO")) {
                        savedList.setPdf(uploadService.saveUpload(file, urlUploadDesecho));// url para desecho
                    } else {
                        response.setMetada("Respuesta FALLIDA", "-1", "No fue posible guardar el pdf");
                        return new ResponseEntity<ListaEquipoResponseRest>(response, HttpStatus.CONFLICT);
                    }
                }
            }

            // Asignar la fecha de registro
            savedList.setFecha_registro(new Date());

            // Asignar el estado
            savedList.setEstado(false);

            List<EquipoTecnologicoEntity> equipos = new ArrayList<>();
            // Iterar sobre la lista de IDs
            for (Integer equipoId : id) {

                // Verificar si los IDs existen en la base de datos
                Optional<EquipoTecnologicoEntity> equipoOptional = equipoRepository.findById(equipoId);
                if (!equipoOptional.isPresent()) {
                    response.setMetada("Respuesta FALLIDA", "-1", "El equipo con ID " + equipoId + " no existe");
                    return new ResponseEntity<ListaEquipoResponseRest>(response, HttpStatus.NOT_FOUND);
                }

                EquipoTecnologicoEntity existingEquipo = equipoOptional.get();

                // Verificar que el equipo tenga un estado válido
                if (!existingEquipo.getEstado().equals("ACTIVO")) {
                    response.setMetada("Respuesta FALLIDA", "-1", "El equipo con ID " + equipoId
                            + " no está en un estado válido para ser agregado a la lista");
                    return new ResponseEntity<ListaEquipoResponseRest>(response, HttpStatus.CONFLICT);
                }

                existingEquipo.setEstado("PENDIENTE");

                existingEquipo.setListaEquipos(savedList);
                equipos.add(existingEquipo);
            }

            // Guardar la lista de equipo
            savedList.setEquipoTecnologico(equipos);

            ListaEquiposEntity Lista = listaRespository.save(savedList);

            if (!Lista.getTipo().equals("ASIGNACION")) {
                PeticionesEntity peticion = new PeticionesEntity();

                peticion.setCategoria("LISTAS");
                peticion.setEstado("PENDIENTE");
                peticion.setId_entidad(Lista.getId());
                peticion.setTipo_peticion("VALIDAR");

                Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
                String username = authentication.getName(); // El username del usuario autenticado
                peticion.setUser(userRepository.findByUsername(username).get());

                if (savedList.getTipo().equals("DONACION")) {
                    peticion.setAnexo(urlUploadDonacion + Lista.getPdf());
                    log.info(urlUploadDonacion + Lista.getPdf());
                }

                if (savedList.getTipo().equals("DESECHO")) {
                    peticion.setAnexo(urlUploadDesecho + Lista.getPdf());
                    log.info(urlUploadDesecho + Lista.getPdf());
                }

                peticionesService.create(peticion, file);

            }

            list.add(savedList);
            response.getListaEquipoResponse().setListaEquipos(list);
            response.setMetada("Respuesta OK", "00",
                    "La lista para " + tipo.toLowerCase() + " se ha creado correctamente");
        } catch (Exception e) {
            response.setMetada("Respuesta FALLIDA", "-1", "Respuesta fallida");
            log.error("Error al buscar users", e);
            e.getStackTrace();
            return new ResponseEntity<ListaEquipoResponseRest>(response, HttpStatus.INTERNAL_SERVER_ERROR);
        }
        return new ResponseEntity<ListaEquipoResponseRest>(response, HttpStatus.OK);
    }

}
