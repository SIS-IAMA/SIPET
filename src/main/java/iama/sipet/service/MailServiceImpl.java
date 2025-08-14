package iama.sipet.service;

import iama.sipet.entity.AsignacionEntity;
import iama.sipet.entity.ListaEquiposEntity;
import iama.sipet.entity.PeticionesEntity;
import iama.sipet.entity.UserEntity;
import iama.sipet.event.PeticionCreadaEvent;
import iama.sipet.repository.AsignacionRespository;
import iama.sipet.repository.ListaEquiposRepository;
import iama.sipet.repository.UserRepository;
import iama.sipet.response.UserResponseRest;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.context.event.EventListener;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;

import java.io.File;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.*;

@Service
public class MailServiceImpl implements IMailService {
    private static final Logger log = LoggerFactory.getLogger(MailServiceImpl.class);

    private final JavaMailSender mailSender;
    
    @Autowired
    private JavaMailSender javaMailSender;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private AsignacionRespository asignacionRespository;

    @Autowired
    private ListaEquiposRepository listaEquiposRepository;

    @Autowired
    private IPeticionService peticionService;

    public MailServiceImpl(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    public class ConexionInternetUtil {
        public static boolean hayConexionInternet() {
            try (Socket socket = new Socket()) {
                socket.connect(new InetSocketAddress("8.8.8.8", 53), 2000); // Google DNS
                return true;
            } catch (IOException e) {
                return false;
            }
        }
    }

    @Override
    public ResponseEntity<UserResponseRest> createToken(String User) {
        log.info("Recuperar contraseña para " + User);
        UserResponseRest response = new UserResponseRest();
        List<UserEntity> list = new ArrayList<>();

        try {
            if (User == null || User.isEmpty()) {
                response.setMetada("Respuesta FALLIDA", "-1", "No se envio ningun usuario");
                log.error("No se envio ningun nombre de usuario");
                return new ResponseEntity<UserResponseRest>(response, HttpStatus.NOT_FOUND);
            }

            Optional<UserEntity> userOptional = userRepository.findByUsername(User);
            if (userOptional.isEmpty()) {
                response.setMetada("Respuesta FALLIDA", "-1",
                        "No se encontro ningun usuario con el nombre ingresado: " + User);
                log.error("No se encontro ningun usuario");
                return new ResponseEntity<UserResponseRest>(response, HttpStatus.NOT_FOUND);
            }

            UserEntity existUser = userOptional.get();
            if (existUser.getRol() == null) {
                response.setMetada("Respuesta FALLIDA", "-1", "El rol del usuario no se pude obtener");
                log.error("No se encontro el rol del usuario");
                return new ResponseEntity<UserResponseRest>(response, HttpStatus.NOT_FOUND);
            }

            if (existUser.getRol().equals(true)) {// Rol true = admin
                log.info("creando token para " + User);
                existUser.setToken(generateToken());
                existUser.setFecha_token(getExpirationTime());
                userRepository.save(existUser);
                sendPasswordResetEmail(User, existUser.getToken());
                response.setMetada("Respuesta OK", "00", "Token enviado por correo");

            }

            if (existUser.getRol().equals(false)) {// Rol false = operador
                // Crear la petición para restablecer contraseña de operador
                PeticionesEntity peticion = new PeticionesEntity();
                peticion.setUser(existUser);
                peticion.setTipo_peticion("PASSWORD");
                peticion.setCategoria("OPERADOR");

                // LLamar servicio para crear peticion

                if (peticionService.create(peticion, null)) {
                    response.setMetada("Respuesta OK", "00", "Peticion enviada");
                } else {
                    response.setMetada("Respuesta FALLIDA", "-1", "La peticion no pudo ser creada");
                    log.error("hubo un error");
                    return new ResponseEntity<UserResponseRest>(response, HttpStatus.INTERNAL_SERVER_ERROR);
                }
            }
            UserEntity newUser = new UserEntity();
            newUser.setRol(existUser.getRol());
            list.add(newUser);
            response.getUserResponse().setUser(list);
        } catch (IllegalArgumentException e) {
            response.setMetada("Respuesta FALLIDA", "-1", e.getMessage());
            log.error("Error al crear token", e);
            return new ResponseEntity<UserResponseRest>(response, HttpStatus.CONFLICT);
        } catch (Exception e) {
            response.setMetada("Respuesta FALLIDA", "-1", e.getMessage());
            log.error("Error al crear token", e);
            return new ResponseEntity<UserResponseRest>(response, HttpStatus.INTERNAL_SERVER_ERROR);
        }
        return new ResponseEntity<UserResponseRest>(response, HttpStatus.CREATED);
    }

    @Override
    public ResponseEntity<UserResponseRest> checkToken(String token) {
        log.info("Comprobando token");
        UserResponseRest response = new UserResponseRest();
        try {
            if (token == null || token.isEmpty()) {
                response.setMetada("Respuesta FALLIDA", "-1", "No se envio ningun token");
                log.error("No se envio ningun token");
                return new ResponseEntity<UserResponseRest>(response, HttpStatus.NOT_FOUND);
            }

            Optional<UserEntity> userOptional = userRepository.findByToken(token);
            if (userOptional.isEmpty()) {
                response.setMetada("Respuesta FALLIDA", "-1",
                        "Token incorrecto");
                log.error("No se encontro ningun usuario");
                return new ResponseEntity<UserResponseRest>(response, HttpStatus.NOT_FOUND);
            }

            UserEntity existUser = userOptional.get();

            if (existUser.getFecha_token() == null) {
                response.setMetada("Respuesta FALLIDA", "-1",
                        "No se encontro la fecha de expiracion del token");
                log.error("No se encontro ninguna fecha");
                return new ResponseEntity<UserResponseRest>(response, HttpStatus.NOT_FOUND);
            }

            if (existUser.getFecha_token().before(new Date())) {
                response.setMetada("Respuesta FALLIDA", "-1", "El token ya expiro, vuelve a generar uno nuevo");
                log.error("Token expirado");
                return new ResponseEntity<UserResponseRest>(response, HttpStatus.CONFLICT);
            }
            // Encriptar la nueva contraseña
            BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
            String contrasenaEncriptada = passwordEncoder.encode("12345");
            existUser.setPassword(contrasenaEncriptada);

            userRepository.save(existUser);

            List<UserEntity> userList = new ArrayList<UserEntity>();
            userList.add(existUser);
            response.getUserResponse().setUser(userList);
            response.setMetada("Respuesta OK", "00", "Respuesta exitosa");
        } catch (Exception e) {
            response.setMetada("Respuesta FALLIDA", "-1", e.getMessage());
            log.error("Error al comprobar token", e);
            e.getStackTrace();
            return new ResponseEntity<UserResponseRest>(response, HttpStatus.INTERNAL_SERVER_ERROR);
        }
        return new ResponseEntity<UserResponseRest>(response, HttpStatus.CREATED);
    }

    public void sendPasswordResetEmail(String destinatario, String token) {
        try {
            if (!ConexionInternetUtil.hayConexionInternet()) {
                System.out.println("No hay conexión a internet. No se puede enviar el correo.");
                throw new IllegalArgumentException("Sin internet, no se puede enviar el correo");
            }

            MimeMessage mimeMessage = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, "UTF-8");

            helper.setTo(destinatario);
            helper.setSubject("Recupera tu contraseña");
            helper.setFrom("soporteti.gama@gmail.com");

            String htmlContent = "<p>Este es el token para recuperar tu contraseña: <strong>" + token + "</strong></p>" +
                    "<p>Recuerda que tienes <strong>5 minutos</strong> para ingresar el token antes de que caduque.</p>";

            helper.setText(htmlContent, true);

            mailSender.send(mimeMessage);
        } catch (MessagingException e) {
            log.error("Error al enviar correo de recuperación", e);
        }
    }

    @EventListener
    public void manejarPeticionCreada(PeticionCreadaEvent event) throws MessagingException {
        try {
            if (!ConexionInternetUtil.hayConexionInternet()) {
                System.out.println("No hay conexión a internet. No se puede enviar el correo.");
                throw new IllegalArgumentException("Sin internet, no se puede enviar el correo");
            }
            PeticionesEntity peticion = event.getPeticion();
            log.info(peticion.toString());
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            MimeMessage mimeMessage = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true);
            String html = "";
            String title = "";
            String Usuario = authentication.getName(); // El username del usuario autenticado

            if (peticion.getCategoria().equals("ASIGNACIONES")){
                AsignacionEntity existAsignaion = asignacionRespository.findById(peticion.getId_entidad()).get();
                String nombreEmpleado = existAsignaion.getEmpleado().getNombre() +" "+ existAsignaion.getEmpleado().getApellido_p() +" "+ existAsignaion.getEmpleado().getApellido_m();
                title = "Responsiva de " + nombreEmpleado;
                html= "<p>Esta es la responsiva de <strong>"+nombreEmpleado+"</strong></p>";
            }

            if (peticion.getCategoria().equals("LISTAS")){
                ListaEquiposEntity listaEquipos =  listaEquiposRepository.findById(peticion.getId_entidad()).get();
                title = "Lista de "+ listaEquipos.getTipo().toLowerCase();
                html= "<p>Esta es la lista de <strong>"+listaEquipos.getTipo().toLowerCase()+"</strong> creada el "+ listaEquipos.getFecha_registro() +"</p>";
            }

            helper.setTo(Usuario);
            helper.setSubject(title);
            helper.setText(html, true);
            helper.setFrom("soporteti.gama@gmail.com");

            FileSystemResource file = new FileSystemResource(new File(peticion.getAnexo()));
            helper.addAttachment(file.getFilename(), file);

            mailSender.send(mimeMessage);
            System.out.println("Enviando correo por nueva petición: " + peticion.getId());
        }catch (IllegalArgumentException e) {
            log.error("Error al enviar el archivo ", e.getMessage());
            throw new MessagingException("Error al enviar correo por nueva petición", e);
        }

    }

    public static String generateToken() {
        return UUID.randomUUID().toString();
    }

    public static Date getExpirationTime() {
        log.info(Instant.now().plus(5, ChronoUnit.MINUTES).toString());
        return Date.from(Instant.now().plus(5, ChronoUnit.MINUTES));
    }

}