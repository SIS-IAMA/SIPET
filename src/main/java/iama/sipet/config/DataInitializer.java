package iama.sipet.config;

import iama.sipet.entity.EmpleadoEntity;
import iama.sipet.entity.EquipoTecnologicoEntity;
import iama.sipet.entity.UserEntity;
import iama.sipet.repository.EmpleadoRepository;
import iama.sipet.repository.EquipoTecnologicoRespository;
import iama.sipet.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final EquipoTecnologicoRespository equipoTecnologicoRespository;
    private final EmpleadoRepository empleadoRepo;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {

        // === USUARIOS ===
        crearUsuario("20233tn181@utez.edu.mx", "admin", "Juan", "Perez", "Lopez", true);
        crearUsuario("operador1", "12345", "Ana", "Martinez", "Gomez", false);
        crearUsuario("operador2", "12345", "Luis", "Hernandez", "Ruiz", false);
        crearUsuario("operador3", "12345", "Maria", "Diaz", "Sanchez", false);

        // === EQUIPOS ===
        crearEquipo("Laptop", "SERIE1234", "HP", "Pavilion", "Windows 11", 16,216, "Intel i5", "Equipo en buenas condiciones", "ACTIVO");
        crearEquipo("Tablet", "SERIE5678", "Samsung", "Galaxy Tab", "Android", 4,64, "Exynos", "Equipo usado en campo", "ACTIVO");
        crearEquipo("Desktop", "SERIE9012", "Dell", "OptiPlex", "Windows 10", 8, 216,"Intel i7", "Equipo de oficina", "ACTIVO");
        crearEquipo("Impresora", "SERIE3456", "Epson", "EcoTank", "Firmware", 0, 0,"ARM", "Impresora de oficina", "ACTIVO");
        crearEquipo("Proyector", "SERIE7890", "BenQ", "MW535A", "Firmware", 0,0, "DLP", "Proyector para presentaciones", "ACTIVO");
        crearEquipo("Servidor", "SERIE1122", "Lenovo", "ThinkSystem", "Windows Server 2019",3000, 32, "Intel Xeon", "Servidor principal", "ACTIVO");
        crearEquipo("Switch", "SERIE3344", "Cisco", "Catalyst 2960", "Firmware", 0,0, "ARM", "Switch de red", "ACTIVO");
        crearEquipo("Scanner", "SERIE5566", "Canon", "DR-C225", "Firmware", 0, 0,"ARM", "Escáner de documentos", "ACTIVO");
        crearEquipo("Monitor", "SERIE7788", "LG", "UltraWide", "Firmware", 0, 0,"ARM", "Monitor de escritorio", "ACTIVO");
        crearEquipo("Celular", "SERIE9900", "Apple", "iPhone 13", "iOS", 4, 216,"A15 Bionic", "Celular institucional", "ACTIVO");
        crearEquipo("Laptop", "SERIE2024A", "Acer", "Aspire 5", "Windows 11", 8, 216,"Intel i3", "Laptop para capacitación", "ACTIVO");
        crearEquipo("Tablet", "SERIE2024B", "Lenovo", "Tab M10", "Android", 4,128, "MediaTek", "Tablet para inventario", "ACTIVO");
        crearEquipo("Desktop", "SERIE2024C", "HP", "EliteDesk", "Windows 10", 16, 216,"Intel i5", "PC de laboratorio", "ACTIVO");
        crearEquipo("Impresora", "SERIE2024D", "Brother", "HL-L2350DW", "Firmware", 0, 0,"ARM", "Impresora secundaria", "ACTIVO");
        crearEquipo("Proyector", "SERIE2024E", "Epson", "PowerLite", "Firmware", 0, 0,"LCD", "Proyector portátil", "ACTIVO");
        crearEquipo("Servidor", "SERIE2024F", "Dell", "PowerEdge", "Ubuntu Server", 64, 2000,"Intel Xeon", "Servidor de respaldo", "ACTIVO");
        crearEquipo("Switch", "SERIE2024G", "TP-Link", "TL-SG1024", "Firmware", 0, 0,"ARM", "Switch de laboratorio", "ACTIVO");
        crearEquipo("Scanner", "SERIE2024H", "HP", "ScanJet Pro", "Firmware", 0, 0,"ARM", "Escáner rápido", "ACTIVO");
        crearEquipo("Monitor", "SERIE2024I", "Samsung", "Curved", "Firmware", 0, 0,"ARM", "Monitor para diseño", "ACTIVO");
        crearEquipo("Celular", "SERIE2024J", "Xiaomi", "Redmi Note 11", "Android", 6, 128,"Snapdragon", "Celular de pruebas", "ACTIVO");
        
        // === EMPLEADOS ===
        crearEmpleado("Carlos", "Ramirez", "Luna", "Técnico", "Soporte", 7771234567L);
        crearEmpleado("Elena", "Torres", "Vega", "Analista", "TI", 7772345678L);
        crearEmpleado("Pedro", "Morales", "Cruz", "Supervisor", "Operaciones", 7773456789L);
        crearEmpleado("María", "González", "Pérez", "Ingeniera", "Desarrollo", 7774567890L);
        crearEmpleado("Jorge", "López", "Martínez", "Diseñador", "Marketing", 7775678901L);
        crearEmpleado("Ana", "Hernández", "Sánchez", "Técnica", "Soporte", 7776789012L);
        crearEmpleado("Luis", "Díaz", "Ramírez", "Administrador", "TI", 7777890123L);
        crearEmpleado("Fernanda", "Cruz", "García", "Supervisora", "Logística", 7778901234L);
        crearEmpleado("Ricardo", "Mendoza", "Flores", "Coordinador", "Recursos Humanos", 7779012345L);
        crearEmpleado("Gabriela", "Ortega", "Morales", "Analista", "Finanzas", 7770123456L);
        crearEmpleado("Santiago", "Ramos", "Vargas", "Técnico", "Mantenimiento", 7771234500L);
        crearEmpleado("Patricia", "Soto", "Lara", "Supervisora", "Calidad", 7772345600L);
        crearEmpleado("Eduardo", "Navarro", "Castillo", "Ingeniero", "Infraestructura", 7773456700L);
        crearEmpleado("Diana", "Silva", "Pacheco", "Diseñadora", "Diseño UX", 7774567800L);
        crearEmpleado("Miguel", "Reyes", "Torres", "Especialista", "Base de Datos", 7775678900L);
        crearEmpleado("Valeria", "Mejía", "Haro", "Técnica", "Redes", 7776789000L);
        crearEmpleado("Andrés", "Camacho", "Ibarra", "Programador", "Desarrollo", 7777890000L);
        crearEmpleado("Isabel", "Velázquez", "Rojo", "Soporte", "TI", 7778900000L);

    }

    private void crearUsuario(String username, String password, String nombre, String apellidoP, String apellidoM, boolean rol) {
        Optional<UserEntity> userOptional = userRepository.findByUsername(username);
        if (!userOptional.isPresent()) {
            UserEntity user = new UserEntity();
            user.setUsername(username);
            user.setPassword(passwordEncoder.encode(password));
            user.setNombre(nombre);
            user.setApellido_p(apellidoP);
            user.setApellido_m(apellidoM);
            user.setRol(rol); // true = admin
            user.setFecha_registro(new Date());
            user.setFoto("");
            user.setEstado(true);
            user.setToken("");
            user.setFecha_token(null);
            userRepository.save(user);
        }
    }


    private void crearEquipo(String tipo, String serie, String marca, String modelo, String so, int ram, int almacenamiento, String procesador, String comentario, String estado) {
        Optional<EquipoTecnologicoEntity> equipoOptional = equipoTecnologicoRespository.findByNumeroSerie(serie);
        if (!equipoOptional.isPresent()) {
            EquipoTecnologicoEntity equipo = new EquipoTecnologicoEntity();
            equipo.setTipo(tipo);
            equipo.setNumero_serie(serie);
            equipo.setMarca(marca);
            equipo.setModelo(modelo);
            equipo.setSistema_operativo(so);
            equipo.setRam(ram);
            equipo.setAlmacenamiento(almacenamiento);
            equipo.setProcesador(procesador);
            equipo.setComentario(comentario);
            equipo.setFoto("");
            equipo.setEstado(estado);
            equipoTecnologicoRespository.save(equipo);
        }
    }

    private void crearEmpleado(String nombre, String apellidoP, String apellidoM, String puesto, String depto, Long telefono) {
        Optional<EmpleadoEntity> empleadoOptional =empleadoRepo.findByPhone(telefono);
        if (!empleadoOptional.isPresent()) {
            EmpleadoEntity emp = new EmpleadoEntity();
            emp.setNombre(nombre);
            emp.setApellido_p(apellidoP);
            emp.setApellido_m(apellidoM);
            emp.setPuesto(puesto);
            emp.setDepartamento(depto);
            emp.setTelefono(telefono);
            emp.setFecha_registro(new Date());
            emp.setFoto("");
            emp.setEstado(true);
            emp.setAsignacion(null); // aún sin asignación
            empleadoRepo.save(emp);
        }
    }

}


