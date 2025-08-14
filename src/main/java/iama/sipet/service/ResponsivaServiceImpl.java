package iama.sipet.service;

import iama.sipet.entity.ResponsivaEntity;
import iama.sipet.repository.ResponsivaRespository;
import iama.sipet.response.ResponsivaResponseRest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class ResponsivaServiceImpl implements  IResponsivaService {
    private Logger log = LoggerFactory.getLogger(ResponsivaServiceImpl.class);

    @Autowired
    private UploadService uploadService;

    @Autowired
    private ResponsivaRespository responsivaRespository;

    @Override
    public ResponsivaEntity create(ResponsivaEntity responsiva, MultipartFile file) throws IOException {
        log.info("Creando Responsiva");
        String encode = URLEncoder.encode(Objects.requireNonNull(file.getOriginalFilename()), StandardCharsets.UTF_8).replace("+", "-");
        try {
            responsiva.setEstado(false);
            
            if (responsiva.getFecha_registro().toString().isEmpty()){
                responsiva.setFecha_registro(new Date());
            }
            // Verificar si el archivo es nulo o vacío
            if (file != null && !file.isEmpty()) {
                responsiva.setPdf(file.getBytes());
                responsiva.setNombrePDF(file.getOriginalFilename());
            }else {
                throw new IllegalArgumentException("No hay responsiva para subir");
            }

            ResponsivaEntity respons = responsivaRespository.save(responsiva);
            return respons;
        }catch (Exception e){
            log.info("Error interno para crear la responsiva " + e );
            throw new IllegalArgumentException("Error interno, no se pudo subir la responsiva");
        }
    }

    @Override
    public ResponsivaEntity updatee(Integer id, MultipartFile file) throws IOException {
        log.info("Actualizando Responsiva");
        String encode = URLEncoder.encode(Objects.requireNonNull(file.getOriginalFilename()), StandardCharsets.UTF_8).replace("+", "-");
        try {
            Optional<ResponsivaEntity> responsivaOptional = responsivaRespository.findById(id);
            if (responsivaOptional.isEmpty()) {
                throw new IllegalArgumentException("No se encontro una responsiva para actualizar");
            }

            ResponsivaEntity responsiva = responsivaOptional.get();

            if (!file.isEmpty() && file!=null){
                // comprobar que la nueva responsiva no sea la misma
                if (Arrays.equals(responsiva.getPDF(), file.getBytes())){
                    throw new IllegalArgumentException("La responsiva es la misma");
                }
                // Guardar la nueva responsiva y actualizar el nombre del pdf en la responsiva
                responsiva.setPdf(file.getBytes());
                responsiva.setNombrePDF(file.getOriginalFilename());
            }else {
                throw new IllegalArgumentException("No hay responsiva para subir");
            }

            ResponsivaEntity respons = responsivaRespository.save(responsiva);
            return respons;
        }catch (Exception e){
            log.info("Error interno para crear la responsiva " + e );
            throw new IllegalArgumentException(e.getMessage());
        }
    }
}
