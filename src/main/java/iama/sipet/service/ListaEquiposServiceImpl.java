package iama.sipet.service;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import iama.sipet.entity.EquipoTecnologicoEntity;
import iama.sipet.entity.ListaEquiposEntity;
import iama.sipet.entity.PeticionesEntity;
import iama.sipet.repository.EquipoTecnologicoRespository;
import iama.sipet.repository.ListaEquiposRepository;
import iama.sipet.repository.UserRepository;
import iama.sipet.response.ListaEquipoResponseRest;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
public class ListaEquiposServiceImpl implements IListaEquiposService {
    private static final Logger log = LoggerFactory.getLogger(ListaEquiposServiceImpl.class);

    @Autowired
    private ListaEquiposRepository listaEquiposRepository;

    @Autowired
    private EquipoTecnologicoRespository equipoTecnologicoRespository;

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
    public ResponseEntity<ListaEquipoResponseRest> findAll() {
        log.info("Buscando operadores");
        ListaEquipoResponseRest response = new ListaEquipoResponseRest();

        try {
            // Obtener todas las listas de equipos
            List<ListaEquiposEntity> listaEquipos = listaEquiposRepository.findAll();

            // Verificar si se encontraron listas
            if (listaEquipos.isEmpty()) {
                log.info("No se encontro la lista de equipos");
                response.setMetada("Respuesta OK", "00", "No se encontro la lista de equipos");
                return new ResponseEntity<ListaEquipoResponseRest>(response, HttpStatus.NOT_FOUND);
            }

            // Iterar sobre las listas de equipo para agregar datos a la foto en el response
            for (ListaEquiposEntity existListEquipos : listaEquipos) {

                // Asignar ruta para la foto dependiendo del tipo
                if (existListEquipos.getTipo().equals("DESECHO")) {
                    existListEquipos.setPdf(urlDesecho + existListEquipos.getPdf());
                }
                if (existListEquipos.getTipo().equals("DONACION")) {
                    existListEquipos.setPdf(urlDonacion + existListEquipos.getPdf());
                }

                // Iterar sobre los equipos para agregar datos a la foto en el response
                for (EquipoTecnologicoEntity equipo : existListEquipos.getEquipoTecnologico()) {
                    Optional<EquipoTecnologicoEntity> equipoOptional = equipoTecnologicoRespository
                            .findById(equipo.getId());
                    if (equipoOptional.isPresent()) {
                        EquipoTecnologicoEntity existEquipo = equipoOptional.get();
                        if (existEquipo.getFoto() != null) {
                            existEquipo.setFoto(url + existEquipo.getFoto());
                        }
                    } else {
                        response.setMetada("Respuesta Fallida", "-1",
                                "Equipo con id " + equipoOptional.get().getId() + " no encontrado");
                        return new ResponseEntity<ListaEquipoResponseRest>(response, HttpStatus.NOT_FOUND);
                    }
                }

            }

            response.getListaEquipoResponse().setListaEquipos(listaEquipos);
            response.setMetada("Respuesta OK", "00", "Respuesta exitosa");
        } catch (Exception e) {
            response.setMetada("Respuesta FALLIDA", "-1", "Respuesta fallida");
            log.error("Error al buscar usuarios", e);
            e.getStackTrace();
            return new ResponseEntity<ListaEquipoResponseRest>(response, HttpStatus.INTERNAL_SERVER_ERROR);
        }
        return new ResponseEntity<ListaEquipoResponseRest>(response, HttpStatus.OK);
    }

    @Override
    public ResponseEntity<ListaEquipoResponseRest> findById(Integer id) {
        log.info("Buscando lista por id");
        ListaEquipoResponseRest response = new ListaEquipoResponseRest();
        List<ListaEquiposEntity> list = new ArrayList<>();

        try {
            Optional<ListaEquiposEntity> listOptional = listaEquiposRepository.findById(id);
            if (listOptional.isPresent()) {

                // Asignar datos para el listado pdf dependiendo del tipo de lista
                ListaEquiposEntity existList = listOptional.get();
                log.info(existList.getTipo());
                if (existList.getTipo().equals("DESECHO")) {
                    existList.setPdf(urlDesecho + existList.getPdf());
                } else {
                    if (existList.getTipo().equals("DONACION")) {
                        existList.setPdf(urlDonacion + existList.getPdf());
                    } else {
                        if (!existList.getTipo().equals("ASIGNACION")) {
                            response.setMetada("Respuesta Fallida", "-1", "Lista con id " + existList.getId()
                                    + " no fue posible asignar una ruta para obtener su listado pdf");
                            return new ResponseEntity<ListaEquipoResponseRest>(response, HttpStatus.BAD_REQUEST);
                        }
                    }
                }

                // Iterar sobre los equipos para agregar datos a la foto en el response
                for (EquipoTecnologicoEntity equipo : existList.getEquipoTecnologico()) {
                    Optional<EquipoTecnologicoEntity> equipoOptional = equipoTecnologicoRespository
                            .findById(equipo.getId());
                    if (equipoOptional.isPresent()) {
                        EquipoTecnologicoEntity existEquipo = equipoOptional.get();
                        existEquipo.setFoto(url + existEquipo.getFoto());
                    } else {
                        response.setMetada("Respuesta Fallida", "-1",
                                "Equipo con id " + equipoOptional.get().getId() + " no encontrado");
                        return new ResponseEntity<ListaEquipoResponseRest>(response, HttpStatus.BAD_REQUEST);
                    }
                }

                list.add(existList);
            } else {
                response.setMetada("Respuesta Fallida", "-1", "Lista no encontrada");
                return new ResponseEntity<ListaEquipoResponseRest>(response, HttpStatus.NOT_FOUND);
            }

            response.getListaEquipoResponse().setListaEquipos(list);
            response.setMetada("Respuesta OK", "00", "Respuesta exitosa");
        } catch (Exception e) {
            response.setMetada("Respuesta FALLIDA", "-1", "Respuesta fallida");
            log.error("Error al buscar usuarios", e.getMessage());
            e.getStackTrace();
            return new ResponseEntity<ListaEquipoResponseRest>(response, HttpStatus.INTERNAL_SERVER_ERROR);
        }
        return new ResponseEntity<ListaEquipoResponseRest>(response, HttpStatus.OK);

    }

    @Override
    public ResponseEntity<ListaEquipoResponseRest> update(Integer id, Integer idPeticion, ListaEquiposEntity listaEquiposEntity,
            MultipartFile file) throws IOException {
        log.info("Actualizando lista");
        String encode = URLEncoder.encode(Objects.requireNonNull(file.getOriginalFilename()), StandardCharsets.UTF_8)
                .replace("+", "-");
        ListaEquipoResponseRest response = new ListaEquipoResponseRest();
        List<ListaEquiposEntity> list = new ArrayList<>();

        try {
            // Buscar lista
            Optional<ListaEquiposEntity> listOptional = listaEquiposRepository.findById(id);
            if (listOptional.isPresent()) {
                ListaEquiposEntity existList = listOptional.get();
                // Actualizar fecha de la lista
                existList.setFecha_registro(new Date());

                // Comprobar los datos de la lista para saber si se actualizan los datos o solo
                // la lista
                if (listaEquiposEntity.getEquipoTecnologico().isEmpty()) {
                    log.info("No se adjuntaron datos a la lista de equipos");
                } else {
                    // crear lista HashSet para no tener equipos duplicados
                    Set<EquipoTecnologicoEntity> equipoSet = new HashSet<EquipoTecnologicoEntity>();

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
                            equipo.setEstado("ACTIVO");
                            equipoTecnologicoRespository.save(equipo);
                        }
                    }
                    
                    // Preparar nuevos equipos
                    List<EquipoTecnologicoEntity> nuevosEquipos = new ArrayList<>();
                    for (EquipoTecnologicoEntity equipo : listaEquiposEntity.getEquipoTecnologico()) {
                        Optional<EquipoTecnologicoEntity> equipoOptional = equipoTecnologicoRespository.findById(equipo.getId());
                        if (!equipoOptional.isPresent()) {
                            response.setMetada("Respuesta FALLIDA", "-1", "No se encontró uno de los equipos de la lista");
                            return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
                        }
                        EquipoTecnologicoEntity existEquipo = equipoOptional.get();

                        // Validaciones adicionales
                        if (existEquipo.getListaEquipos() != null && !existEquipo.getListaEquipos().getId().equals(id)) {
                            if (List.of("DONADO", "DESECHADO", "ASIGNADO").contains(existEquipo.getEstado())) {
                                response.setMetada("Respuesta FALLIDA", "-1",
                                        "El equipo " + equipo.getModelo() + " está asignado a otro listado");
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

                }

                // Verificar si el archivo es nulo o vacío
                if (file != null && !file.isEmpty() && !existList.getTipo().equals("ASIGNACION")) {
                    String nombre = existList.getPdf();
                    if (existList.getTipo().equals("DONACION")) {
                        uploadService.deleteUpload(nombre, urlUploadDonacion);
                        existList.setPdf(uploadService.saveUpload(file, urlUploadDonacion));// url para donacion
                    } else {
                        if (existList.getTipo().equals("DESECHO")) {
                            uploadService.deleteUpload(nombre, urlUploadDesecho);
                            existList.setPdf(uploadService.saveUpload(file, urlUploadDesecho));// url para desecho
                        } else {
                            response.setMetada("Respuesta FALLIDA", "-1", "No fue posible guardar el pdf");
                            return new ResponseEntity<ListaEquipoResponseRest>(response, HttpStatus.CONFLICT);
                        }
                    }
                }

                ListaEquiposEntity Lista = listaEquiposRepository.save(existList);

                log.info("tipo lista ", Lista.getTipo().toString());

                if (!Lista.getTipo().equals("ASIGNACION")) {
                    log.info("Entramos");

                    if (Lista.getTipo().equals("DONACION")) {
                        peticionesService.upload(idPeticion,urlUploadDonacion+Lista.getPdf());
                    }
                    if (Lista.getTipo().equals("DESECHO")) {
                        peticionesService.upload(idPeticion,urlUploadDesecho+Lista.getPdf());
                    }

                }

                list.add(existList);
            } else {
                response.setMetada("Respuesta Fallida", "-1", "Lista no encontrada");
                return new ResponseEntity<ListaEquipoResponseRest>(response, HttpStatus.NOT_FOUND);
            }

            response.getListaEquipoResponse().setListaEquipos(list);
            response.setMetada("Respuesta OK", "00", "Respuesta exitosa");

        } catch (Exception e) {
            response.setMetada("Respuesta FALLIDA", "-1", "Respuesta Error interno para actualizar la lista");
            log.error("Error interno para actualizar lista", e);
            return new ResponseEntity<ListaEquipoResponseRest>(response, HttpStatus.INTERNAL_SERVER_ERROR);
        }
        return new ResponseEntity<ListaEquipoResponseRest>(response, HttpStatus.OK);
    }

    public ResponseEntity<ListaEquipoResponseRest> deshacer(Integer id) {
        log.info("deshacer listaEquiposEntity");
        ListaEquipoResponseRest response = new ListaEquipoResponseRest();
        try {
            Optional<ListaEquiposEntity> listaOptional = listaEquiposRepository.findById(id);
            if (listaOptional.isEmpty()) {
                response.setMetada("Respuesta Fallida", "-1", "Lista no encontrada");
                return new ResponseEntity<ListaEquipoResponseRest>(response, HttpStatus.NOT_FOUND);
            }

            ListaEquiposEntity existList = listaOptional.get();

            for (EquipoTecnologicoEntity equipo : existList.getEquipoTecnologico()) {
                equipo.setEstado("ACTIVO");
                equipo.setListaEquipos(null);
            }

            existList.setEquipoTecnologico(null);
            existList.setAsignacion(null);
            String nombre = existList.getPdf();
            if (existList.getTipo().equals("DESECHO")) {
                uploadService.deleteUpload(nombre, urlUploadDesecho);
            }

            if (existList.getTipo().equals("DONACION")) {
                uploadService.deleteUpload(nombre, urlUploadDonacion);
            }

            listaEquiposRepository.delete(existList);

        } catch (Exception e) {
            response.setMetada("Respuesta FALLIDA", "-1", "Respuesta Error interno para actualizar la lista");
            log.error("Error interno para actualizar lista", e);
            e.getStackTrace();
            return new ResponseEntity<ListaEquipoResponseRest>(response, HttpStatus.INTERNAL_SERVER_ERROR);
        }
        return new ResponseEntity<ListaEquipoResponseRest>(response, HttpStatus.OK);
    }

    @Override
    public void changeEstadoEquipos(Integer id) {
        log.info("Cambiando el estado de los equipos de la asignacion validada");

        try {
            Optional<ListaEquiposEntity> listaOptional = listaEquiposRepository.findById(id);
            if (listaOptional.isEmpty()) {
                log.error("No se encontro ninguna lista");
            }

            ListaEquiposEntity existList = listaOptional.get();
            for (EquipoTecnologicoEntity existEquipo : existList.getEquipoTecnologico()) {
                // Asignar el nuevo estado del equipo que se agregó a la lista
                if (existEquipo.getListaEquipos() == null && existEquipo.getEstado().equals("ACTIVO")) {
                    log.info("Switch");
                    switch (existList.getTipo()) {
                        case "ASIGNACION": {
                            existEquipo.setEstado("ASIGNADO");
                            break;
                        }
                        case "DESECHO": {
                            existEquipo.setEstado("DESECHADO");
                            break;
                        }
                        case "DONACION": {
                            existEquipo.setEstado("DONADO");
                            break;
                        }
                        default: {
                            log.error("Respuesta FALLIDA", "-1",
                                    "El equipo con ID " + existEquipo.getId() + ":" + existEquipo.getEstado()
                                            + " no está en un estado válido para ser agregado a la lista");
                        }
                    }
                }
            }

        } catch (Exception e) {
            log.error("Sucedio un error en el metodo para cmbiar equipos", e);

        }
    }
}
