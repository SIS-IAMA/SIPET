package iama.sipet.event;

import iama.sipet.entity.*;
import iama.sipet.repository.*;
import iama.sipet.service.UploadService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Component
public class EventDelete {

    // Repositorios de entidades
    @Autowired
    private EmpleadoRepository empleadoRepository;

    @Autowired
    private EquipoTecnologicoRespository equipoTecnologicoRepository;

    @Autowired
    private ListaEquiposRepository listaEquiposRepository;

    @Autowired
    private PeticionesRepository peticionesRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UploadService uploadService;

    @Autowired
    private ResponsivaRespository responsivaRespository;

    // URL
    String urlResponsivas = "upload/Responsivas/";
    String urlEmpleado = "upload/FotosEmpleado/";
    String urlEquipo = "upload/FotosEquipos/";
    String urlListaDonacion = "upload/ListasEquipos/Donacion/";
    String urlListaDesecho  = "upload/ListasEquipos/Desecho/";
    String urlOperador = "upload/FotosOperador/";


    // Fechas para comparar
    LocalDateTime fechaLimite2Meses = LocalDateTime.now().minusMonths(2);
    LocalDateTime fechaLimite6Mes = LocalDateTime.now().minusMonths(6);
    LocalDateTime fechaLimite1Semana = LocalDateTime.now().minusWeeks(1);

    /*
    @Scheduled(fixedRate = 10000) // cada 10 segundos
    public void test() {
        System.out.println(">> Evento ejecutado: " + LocalDateTime.now());
    }
    */

    @Scheduled(cron = "0 0 0 1 */2 *")
    @Transactional
    public void eliminarEmpleadosInactivos() {
        System.out.println(">> Eliminando Entidades "+ LocalDateTime.now());


        // Eliminar empleados con más de 2 meses inactivos
        List<EmpleadoEntity> empleadosAntiguos = empleadoRepository.findDeshabilitadosAntiguos(fechaLimite2Meses);
        if (!empleadosAntiguos.isEmpty()) {
            for (EmpleadoEntity empleado : empleadosAntiguos) {
                System.out.println("Eliminando empleado inactivo con más de 2 meses: " + empleado.getId());
                // Aquí puedes agregar lógica adicional si es necesario antes de eliminar
                String fotoEmpleado = empleado.getFoto();
                if (fotoEmpleado != null && !fotoEmpleado.isEmpty()) {
                    System.out.println("Eliminando foto del empleado inactivo con más de 2 meses: " + empleado.getFoto());
                    uploadService.deleteUpload(fotoEmpleado, urlEmpleado);
                }
                if (empleado.getResponsiva() != null && !empleado.getResponsiva().isEmpty()) {
                    for (ResponsivaEntity responsiva : empleado.getResponsiva()){
                        System.out.println("Eliminando responsiva del empleado inactivo con más de 2 meses: " + responsiva.getPdf());
                        String pdfResponsiva = responsiva.getPdf();
                        if (pdfResponsiva != null && !pdfResponsiva.isEmpty()) {
                            uploadService.deleteUpload(pdfResponsiva, urlResponsivas);
                        }
                        responsivaRespository.delete(responsiva);
                    }
                }
                empleadoRepository.delete(empleado);
            }
        }

        // Eliminar Operadores con más de 2 meses de inactividad
        List<UserEntity> operadoresAntiguos = userRepository.findOperadoresInactivos(fechaLimite2Meses);
        if (!operadoresAntiguos.isEmpty()) {
            for (UserEntity operador : operadoresAntiguos) {
                System.out.println("Eliminando operador con más de 2 meses de inactividad: " + operador.getId());
                String fotoOperador = operador.getFoto();
                if (fotoOperador != null && !fotoOperador.isEmpty()) {
                    System.out.println("Eliminando foto del operador con más de 2 meses de inactividad: " + operador.getFoto());
                    uploadService.deleteUpload(fotoOperador, urlOperador);
                }
                userRepository.delete(operador);
            }
        }


        // Eliminar listas con más de 2 meses inactivas
        List<ListaEquiposEntity> listasAntiguas = listaEquiposRepository.findListasInactivas(fechaLimite6Mes);
        if (!listasAntiguas.isEmpty()) {
            for (ListaEquiposEntity lista : listasAntiguas) {
                System.out.println("Eliminando Listas con más de 6 meses creadas: " + lista.getId());
                // Eliminar PDF de la lista
                String pdfLista = lista.getPdf();
                if (pdfLista != null && !pdfLista.isEmpty()) {
                    System.out.println("Eliminando pdf de Listas con más de 6 meses creadas: " + pdfLista);
                    if (lista.getTipo() != null && !lista.getTipo().isEmpty()) {
                        if (lista.getTipo().equals("DONACION")) {
                            uploadService.deleteUpload(pdfLista, urlListaDonacion);
                        }
                        if (lista.getTipo().equals("DESECHO")) {
                            uploadService.deleteUpload(pdfLista, urlListaDesecho);
                        }
                    }
                }

                // Eliminar equipos tecnológicos asociados a la lista
                List<EquipoTecnologicoEntity> equiposTecnologicos = lista.getEquipoTecnologico();
                if (equiposTecnologicos != null && !equiposTecnologicos.isEmpty()) {
                    for (EquipoTecnologicoEntity equipo : equiposTecnologicos) {
                        System.out.println("Eliminando equipo de la lista con más de 6 meses creadas: " + equipo.getId());

                        String fotoEquipo = equipo.getFoto();
                        if (fotoEquipo != null && !fotoEquipo.isEmpty()) {
                            System.out.println("Eliminando foto del equipo de la lista con más de 6 meses creada: " + equipo.getFoto());
                            uploadService.deleteUpload(fotoEquipo, urlEquipo);
                        }
                        equipoTecnologicoRepository.delete(equipo);
                    }
                }

                // Eliminar Lista
                listaEquiposRepository.delete(lista);
            }
        }

    }

    @Scheduled(cron = "0 0 0 1 * *")
    @Transactional
    public void eliminarPeticiones() {
        System.out.println(">> Eliminando peticiones " + LocalDateTime.now());
        // Eliminar peticiones con más de 1 semana aceptadas
        List<PeticionesEntity> peticionesAntiguas = peticionesRepository.findPeticionesAceptadasAntiguas(fechaLimite1Semana);
        if (!peticionesAntiguas.isEmpty()) {
            for (PeticionesEntity peticion : peticionesAntiguas) {
                System.out.println("Eliminando petición aceptada con más de 1 semana: " + peticion.getId());
                peticion.setUser(null);
                peticionesRepository.save(peticion);
                peticionesRepository.delete(peticion);
            }
        }
    }
}

