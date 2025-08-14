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

    String url = "https://sipet-iama.onrender.com/upload/";
    String urlUpload = "upload/Responsivas/";

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
                String name = uploadService.saveUpload(file, urlUpload);
                responsiva.setPdf(name);

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

            log.info(encode, " ", file.getOriginalFilename(), " ", responsiva.getPdf() );
            // Verificar si el archivo es nulo o vacío
            if ((encode != null && !encode.isEmpty()) && (file != null && !file.isEmpty())) {
                // comprobar que la nueva responsiva no sea la misma
                if (encode.equals(responsiva.getPdf())) {
                    throw new IllegalArgumentException("La responsiva es la misma o tiene el mismo nombre");
                }

                // Si la responsiva tiene un archivo previo, se elimina del servidor
                String nombrePdfAnterior = responsiva.getPdf();
                if (nombrePdfAnterior != null && !nombrePdfAnterior.isEmpty()) {
                    uploadService.deleteUpload(nombrePdfAnterior, urlUpload);
                }

                // Guardar la nueva responsiva y actualizar el nombre del pdf en la responsiva
                String nuevoPdf = uploadService.saveUpload(file, urlUpload);
                responsiva.setPdf(nuevoPdf);
                String name = uploadService.saveUpload(file, urlUpload);
                responsiva.setPdf(name);
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


    @Override
    public ResponsivaEntity save(MultipartFile file) throws IOException {
        ResponsivaEntity responsiva= new ResponsivaEntity();
        String name = uploadService.saveUpload(file, urlUpload);
        responsiva.setPdf(name);
        responsiva.setEstado(false);
        responsiva.setFecha_registro(new Date());
        return responsivaRespository.save(responsiva);
    }

    @Override
    public ResponsivaEntity findById(Integer id) {
        ResponsivaEntity responsiva = responsivaRespository.findById(id).get();
        responsiva.setPdf(url + responsiva.getPdf());
        return responsiva;
       }

    @Override
    public List<ResponsivaEntity> findAll() {
        List<ResponsivaEntity> responsivas = responsivaRespository.findAll();
        responsivas = responsivas.stream()
                .map(responsiva -> {
                    responsiva.setPdf(url + responsiva.getPdf());
                    return responsiva;
                }).collect(Collectors.toList());
        return responsivas;
    }

    //Actualizar pdf
    @Override
    public ResponsivaEntity update(Integer id, MultipartFile file) throws IOException {
        ResponsivaEntity responsiva= new ResponsivaEntity();
        responsiva.setId(id);
        responsiva.setEstado(true);
        String nombre = uploadService.saveUpload(file, urlUpload);
        responsiva.setPdf(nombre);
        return responsivaRespository.save(responsiva);
    }

    @Override
    public void deleteById(Integer id) {
        ResponsivaEntity responsiva = responsivaRespository.findById(id).get();
        String nombre = responsiva.getPdf();
        uploadService.deleteUpload(nombre, urlUpload);
        responsivaRespository.delete(responsiva);

    }
}
